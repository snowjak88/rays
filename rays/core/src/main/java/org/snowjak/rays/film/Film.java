package org.snowjak.rays.film;

import static org.apache.commons.math3.util.FastMath.floor;
import static org.apache.commons.math3.util.FastMath.max;
import static org.apache.commons.math3.util.FastMath.pow;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.snowjak.rays.RenderTask;
import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.annotations.UIType;
import org.snowjak.rays.filter.Filter;
import org.snowjak.rays.sample.EstimatedSample;
import org.snowjak.rays.sample.FixedSample;
import org.snowjak.rays.sampler.Sampler;
import org.snowjak.rays.spectrum.Spectrum;
import org.snowjak.rays.spectrum.colorspace.RGB;
import org.snowjak.rays.spectrum.colorspace.XYZ;

/**
 * A film object is responsible for accepting a series of
 * {@link EstimatedSample}s and converting them into an image.
 * <p>
 * <strong>Note</strong> that Film is <strong>thread-safe</strong>. This means
 * that multiple threads can all utilize the same Film instance without issue
 * (apart from waiting for any pertinent synchronization locks to be freed).
 * </p>
 * 
 * @author snowjak88
 *
 */
@UIType(fields = { @UIField(name = "width", type = Integer.class, defaultValue = "400"),
		@UIField(name = "height", type = Integer.class, defaultValue = "300"),
		@UIField(name = "aperture", type = Double.class, defaultValue = "16.0"),
		@UIField(name = "exposureTime", type = Double.class, defaultValue = "0.0333"),
		@UIField(name = "isoSensitivity", type = Double.class, defaultValue = "100"),
		@UIField(name = "calibrationConstant", type = Double.class, defaultValue = "815"),
		@UIField(name = "filter", type = Filter.class) })
public class Film {
	
	private int width, height;
	private int offsetX, offsetY;
	private double aperture;
	private double exposureTime;
	private double isoSensitivity;
	private double calibrationConstant;
	private Filter filter;
	
	private transient boolean initialized = false;
	private transient XYZ[][] receivedLuminance;
	private transient double[][] filterWeights;
	
	/**
	 * Construct a new Film instance with the given properties.
	 * <dl>
	 * <dt>width, height</dt>
	 * <dd>The dimensions in pixels of the film. This should match the associated
	 * {@link Sampler}'s defined {@code [xStart, yStart] - [xEnd, yEnd]} range.
	 * <dt>aperture</dt>
	 * <dd>Defines the camera's aperture (expressed as an f-stop #)</dd>
	 * <dt>exposureTime</dt>
	 * <dd>Expressed in seconds</dt>
	 * <dt>isoSensitivity</dt>
	 * <dd>i.e., the ISO speed rating of this film</dd>
	 * <dt>calibrationConstant</dt>
	 * <dd>A scaling factor used to modify the behavior of this Film to match the
	 * real-world performance of any digital camera you please</dd>
	 * <dt>filter</dt>
	 * <dd>A method to weigh each incoming {@link EstimatedSample}'s contribution to
	 * this Film's pixels. Most Filters will probably only affect the sample's
	 * actual pixel and its immediate neighbors.
	 * </dl>
	 * <p>
	 * <strong>Note</strong> that this Film assumes that the
	 * {@link EstimatedSample}s it receives will all have film-points in the range
	 * {@code [0, 0] - [width-1, height-1]}.
	 * </p>
	 * 
	 * @param width
	 * @param height
	 * @param aperture
	 * @param exposureTime
	 * @param isoSensitivity
	 * @param calibrationConstant
	 * @param filter
	 */
	public Film(int width, int height, double aperture, double exposureTime, double isoSensitivity,
			double calibrationConstant, Filter filter) {
		
		this(width, height, 0, 0, aperture, exposureTime, isoSensitivity, calibrationConstant, filter);
	}
	
	/**
	 * Construct a new Film instance with the given properties.
	 * <dl>
	 * <dt>width, height</dt>
	 * <dd>The dimensions in pixels of the film. This should match the associated
	 * {@link Sampler}'s defined {@code [xStart, yStart] - [xEnd, yEnd]} range.
	 * <dt>offsetX, offsetY</dt>
	 * <dd>You should set this to be equal to
	 * <code> [ {@link Sampler#getXStart()}, {@link
	 * Sampler#getYStart()} ]</code>.
	 * <dt>aperture</dt>
	 * <dd>Defines the camera's aperture (expressed as an f-stop #)</dd>
	 * <dt>exposureTime</dt>
	 * <dd>Expressed in seconds</dt>
	 * <dt>isoSensitivity</dt>
	 * <dd>i.e., the ISO speed rating of this film</dd>
	 * <dt>calibrationConstant</dt>
	 * <dd>A scaling factor used to modify the behavior of this Film to match the
	 * real-world performance of any digital camera you please</dd>
	 * <dt>filter</dt>
	 * <dd>A method to weigh each incoming {@link EstimatedSample}'s contribution to
	 * this Film's pixels. Most Filters will probably only affect the sample's
	 * actual pixel and its immediate neighbors.
	 * </dl>
	 * <p>
	 * <strong>Note</strong> that this Film assumes that the
	 * {@link EstimatedSample}s it receives will all have film-points in the range
	 * {@code [0, 0] - [width-1, height-1]}.
	 * </p>
	 * 
	 * @param width
	 * @param height
	 * @param offsetX
	 * @param offsetY
	 * @param aperture
	 * @param exposureTime
	 * @param isoSensitivity
	 * @param calibrationConstant
	 * @param filter
	 */
	public Film(int width, int height, int offsetX, int offsetY, double aperture, double exposureTime,
			double isoSensitivity, double calibrationConstant, Filter filter) {
		
		this.width = width;
		this.height = height;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
		this.aperture = aperture;
		this.exposureTime = exposureTime;
		this.isoSensitivity = isoSensitivity;
		this.calibrationConstant = calibrationConstant;
		this.filter = filter;
	}
	
	private void initialize() {
		
		this.receivedLuminance = new XYZ[width + filter.getExtentX() * 2][height + filter.getExtentY() * 2];
		this.filterWeights = new double[width + filter.getExtentX() * 2][height + filter.getExtentY() * 2];
		
		for (int x = 0; x < receivedLuminance.length; x++)
			for (int y = 0; y < receivedLuminance[x].length; y++) {
				receivedLuminance[x][y] = null;
				filterWeights[x][y] = 0;
			}
		
		this.aperture = max(aperture, 0d);
		this.exposureTime = max(exposureTime, 0d);
		this.isoSensitivity = max(isoSensitivity, 0d);
		this.calibrationConstant = max(calibrationConstant, 0d);
		
		this.initialized = true;
	}
	
	/**
	 * Receive the given {@link EstimatedSample}.
	 * <p>
	 * It is assumed that the flow-of-execution has already reported back this
	 * EstimatedSample to the appropriate Sampler (see
	 * {@link Sampler#reportSampleResult(FixedSample)}), and that this
	 * EstimatedSample is judged to be acceptable.
	 * </p>
	 * 
	 * @param estimate
	 */
	public void addSample(EstimatedSample estimate) {
		
		synchronized (this) {
			if (!initialized)
				initialize();
		}
		
		final int filmX = (int) floor(estimate.getSample().getFilmPoint().getX()),
				filmY = (int) floor(estimate.getSample().getFilmPoint().getY());
		
		for (int pixelX = filmX - filter.getExtentX(); pixelX <= filmX + filter.getExtentX(); pixelX++)
			for (int pixelY = filmY - filter.getExtentY(); pixelY <= filmY + filter.getExtentY(); pixelY++)
				if (filter.isContributing(estimate.getSample(), pixelX, pixelY)) {
					
					final Spectrum sampleRadiance = estimate.getRadiance();
					
					synchronized (this) {
						
						final var offsetPixelX = pixelX - offsetX;
						final var offsetPixelY = pixelY - offsetY;
						
						final var indexX = offsetPixelX + filter.getExtentX();
						final var indexY = offsetPixelY + filter.getExtentY();
						
						if (indexX < 0 || indexX >= receivedLuminance.length)
							continue;
						
						if (indexY < 0 || indexY >= receivedLuminance[indexX].length)
							continue;
						
						final var filterContribution = filter.getContribution(estimate.getSample(), pixelX, pixelY);
						final var newXyz = new XYZ(
								XYZ.fromSpectrum(sampleRadiance, true).get().multiply(filterContribution));
						
						filterWeights[indexX][indexY] += filterContribution;
						
						if (receivedLuminance[indexX][indexY] == null)
							receivedLuminance[indexX][indexY] = newXyz;
						else
							receivedLuminance[indexX][indexY] = new XYZ(
									receivedLuminance[indexX][indexY].get().add(newXyz.get()));
						
					}
					
				}
	}
	
	/**
	 * Get the luminance received so far at the given film (pixel) location.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private XYZ getReceivedLuminance(int x, int y) {
		
		final var indexX = x + filter.getExtentX();
		final var indexY = y + filter.getExtentY();
		
		if (indexX < 0 || indexX >= receivedLuminance.length)
			return new XYZ(0, 0, 0);
		
		if (indexY < 0 || indexY >= receivedLuminance[indexX].length)
			return new XYZ(0, 0, 0);
		
		if (receivedLuminance[indexX][indexY] == null || filterWeights[indexX][indexY] == 0d)
			return new XYZ(0, 0, 0);
		
		final var filterWeight = filterWeights[indexX][indexY];
		return new XYZ(receivedLuminance[indexX][indexY].get().multiply(1d / filterWeight));
	}
	
	/**
	 * Compile the {@link Image} gathered so far by this Film instance.
	 * 
	 * @return
	 */
	public Image getImage() {
		
		return getImage(null);
	}
	
	/**
	 * Compile the {@link Image} gathered so far by this Film instance. Tag it with
	 * the given {@link UUID} (e.g., to associate it with a certain
	 * {@link RenderTask}).
	 * 
	 * @param uuid
	 * @return
	 */
	public Image getImage(UUID uuid) {
		
		return getImage(uuid, 0, 0, width - 1, height - 1);
	}
	
	/**
	 * Compile that portion of the {@link Image} gathered so far by this Film
	 * instance, which lies within the bounds indicated by
	 * {@code [xStart,yStart]-[xEnd,yEnd]}. Tag it with the given {@link UUID}
	 * (e.g., to associate it with a certain {@link RenderTask}).
	 * 
	 * @return
	 */
	public Image getImage(UUID uuid, int xStart, int yStart, int xEnd, int yEnd) {
		
		synchronized (this) {
			if (!initialized)
				initialize();
		}
		
		final var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		synchronized (this) {
			
			for (int x = 0; x < width; x++)
				for (int y = 0; y < height; y++) {
					
					final var indexX = x + filter.getExtentX();
					final var indexY = y + filter.getExtentY();
					
					final var offsetX = x + this.offsetX;
					final var offsetY = y + this.offsetY;
					
					if ((offsetX < xStart || offsetX > xEnd) || (offsetY < yStart || offsetY > yEnd)
							|| receivedLuminance[indexX][indexY] == null || filterWeights[indexX][indexY] == 0d)
						image.setRGB(x, y, RGB.toPacked(RGB.BLACK, 0d));
					
					else {
						final var rgb = getExposureRGB(getReceivedLuminance(x, y));
						image.setRGB(x, y, rgb.toPacked());
					}
				}
			
		}
		
		return new Image(image, uuid);
		
	}
	
	/**
	 * Calculate the RGB triplet resulting from exposing this Film to a given XYZ
	 * triplet (assumed to express absolute luminance -- see
	 * {@link XYZ#fromSpectrum(Spectrum, boolean)}).
	 * 
	 * @param exposure
	 * @return
	 */
	private RGB getExposureRGB(XYZ exposure) {
		
		final var v = new XYZ(
				exposure.get().multiply(calibrationConstant * (exposureTime * isoSensitivity) / (pow(aperture, 2))));
		
		return v.to(RGB.class);
	}
	
	public int getWidth() {
		
		return width;
	}
	
	public int getHeight() {
		
		return height;
	}
	
	public Filter getFilter() {
		
		return filter;
	}
	
	/**
	 * Return a variant of this Film, with modified dimensions and offset to the
	 * given sample-window.
	 * 
	 * @return
	 */
	public Film partition(int xStart, int yStart, int xEnd, int yEnd) {
		
		final var newWidth = xEnd - xStart + 1;
		final var newHeight = yEnd - yStart + 1;
		return new Film(newWidth, newHeight, xStart, yStart, aperture, exposureTime, isoSensitivity,
				calibrationConstant, filter);
	}
	
	/**
	 * Packages a {@link RenderedImage} in a de/serializable format.
	 * 
	 * @author snowjak88
	 *
	 */
	public static class Image {
		
		private String png;
		private UUID uuid;
		
		public Image(RenderedImage img, UUID uuid) {
			
			final var buffer = new ByteArrayOutputStream();
			
			try {
				ImageIO.write(img, "png", ImageIO.createImageOutputStream(buffer));
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			this.png = Base64.getEncoder().encodeToString(buffer.toByteArray());
			this.uuid = uuid;
		}
		
		public BufferedImage getBufferedImage() {
			
			try {
				return ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(png)));
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		public String getPng() {
			
			return png;
		}
		
		public UUID getUuid() {
			
			return uuid;
		}
	}
	
}
