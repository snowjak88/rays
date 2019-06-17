package org.snowjak.rays.film;

import static org.apache.commons.math3.util.FastMath.floor;

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
		@UIField(name = "filter", type = Filter.class) })
public class Film {
	
	private int width, height;
	private Filter filter;
	
	private transient boolean initialized = false;
	private transient Spectrum[][] receivedSpectra;
	private transient int[][] receivedSpectraCounts;
	
	public Film(int width, int height, Filter filter) {
		
		this.width = width;
		this.height = height;
		this.filter = filter;
	}
	
	private void initialize() {
		
		this.receivedSpectra = new Spectrum[width + filter.getExtentX() * 2][height + filter.getExtentY() * 2];
		this.receivedSpectraCounts = new int[width + filter.getExtentX() * 2][height + filter.getExtentY() * 2];
		
		for (int x = 0; x < receivedSpectra.length; x++)
			for (int y = 0; y < receivedSpectra[x].length; y++) {
				receivedSpectra[x][y] = null;
				receivedSpectraCounts[x][y] = 0;
			}
		
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
					
					final Spectrum sampleRadiance = estimate.getRadiance()
							.multiply(filter.getContribution(estimate.getSample(), pixelX, pixelY));
					
					synchronized (this) {
						
						final var indexX = pixelX + filter.getExtentX();
						final var indexY = pixelY + filter.getExtentY();
						
						if (indexX < 0 || indexX >= receivedSpectra.length)
							continue;
						
						if (indexY < 0 || indexY >= receivedSpectra[indexX].length)
							continue;
						
						if (receivedSpectra[indexX][indexY] == null) {
							receivedSpectra[indexX][indexY] = sampleRadiance;
							receivedSpectraCounts[indexX][indexY] = 1;
						} else {
							receivedSpectra[indexX][indexY] = receivedSpectra[indexX][indexY].add(sampleRadiance);
							receivedSpectraCounts[indexX][indexY]++;
						}
						
					}
					
				}
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
					
					if ((x < xStart || x > xEnd) || (y < yStart || y > yEnd) || receivedSpectra[indexX][indexY] == null
							|| receivedSpectraCounts[indexX][indexY] < 1)
						image.setRGB(x, y, RGB.toPacked(RGB.BLACK, 0d));
					
					else {
						final var rgb = receivedSpectra[indexX][indexY]
								.multiply(1d / (double) receivedSpectraCounts[indexX][indexY]).toRGB();
						image.setRGB(x, y, rgb.toPacked());
					}
				}
			
		}
		
		return new Image(image, uuid);
		
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
