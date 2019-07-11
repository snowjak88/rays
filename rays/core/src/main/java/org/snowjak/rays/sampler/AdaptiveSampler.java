/**
 * 
 */
package org.snowjak.rays.sampler;

import static org.apache.commons.math3.util.FastMath.ceil;
import static org.apache.commons.math3.util.FastMath.pow;
import static org.apache.commons.math3.util.FastMath.sqrt;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.snowjak.rays.Settings;
import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.annotations.UIType;
import org.snowjak.rays.geometry.Point2D;
import org.snowjak.rays.renderer.Renderer;
import org.snowjak.rays.sample.EstimatedSample;
import org.snowjak.rays.sample.FixedSample;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.spectrum.colorspace.XYZ;

/**
 * Implements an adaptive {@link Sampler}.
 * <p>
 * This sampler resembles the {@link StratifiedSampler}, except that it is
 * capable of generating a variable number of {@link Sample}s in response to
 * sample-domains of greater or lesser complexity.
 * </p>
 * <p>
 * When a Sample is successfully estimated and the {@link Renderer} calls
 * {@link Sampler#reportSampleResult(org.snowjak.rays.sample.EstimatedSample)},
 * this AdaptiveSampler will capture the {@link EstimatedSample}. At the
 * conclusion of each pixel's worth of Samples, this AdaptiveSampler will decide
 * whether to move on to the next pixel, or to generate a further set of Samples
 * for the same pixel.
 * </p>
 * 
 * @author snowjak88
 *
 */
@UIType(type = "adaptive", fields = { @UIField(name = "xStart", type = Double.class, defaultValue = "0"),
		@UIField(name = "yStart", type = Double.class, defaultValue = "0"),
		@UIField(name = "xEnd", type = Double.class, defaultValue = "399"),
		@UIField(name = "yEnd", type = Double.class, defaultValue = "299"),
		@UIField(name = "minSamplesPerPixel", type = Integer.class, defaultValue = "4"),
		@UIField(name = "maxSamplesPerPixel", type = Integer.class, defaultValue = "32"),
		@UIField(name = "additional1DSamples", type = Integer.class, defaultValue = "4"),
		@UIField(name = "additional2DSamples", type = Integer.class, defaultValue = "4") })
public class AdaptiveSampler extends Sampler {
	
	private int minSamplesPerPixel;
	private int maxSamplesPerPixel;
	
	private transient boolean initialized = false;
	private transient int currentPixelX, currentPixelY;
	private transient int currentPixelSampleNumber;
	private transient int totalPixelSampleCount;
	
	private transient double blockSize1D, blockSize2D, additionalSize1D, additionalSize2D;
	private transient Point2D[][] film, lens, add2d;
	private transient double[] t, add1d;
	
	private transient ArrayList<XYZ> estimates;
	
	private transient long pixelsGenerated = 0;
	
	/**
	 * 
	 * @param xStart
	 * @param yStart
	 * @param xEnd
	 * @param yEnd
	 * @param minSamplesPerPixel
	 * @param maxSamplesPerPixel
	 */
	public AdaptiveSampler(int xStart, int yStart, int xEnd, int yEnd, int minSamplesPerPixel, int maxSamplesPerPixel) {
		
		this(xStart, yStart, xEnd, yEnd, minSamplesPerPixel, maxSamplesPerPixel, 0, 0);
	}
	
	/**
	 * @param xStart
	 * @param yStart
	 * @param xEnd
	 * @param yEnd
	 * @param minSamplesPerPixel
	 * @param maxSamplesPerPixel
	 * @param additional1dSamples
	 * @param additional2dSamples
	 */
	public AdaptiveSampler(int xStart, int yStart, int xEnd, int yEnd, int minSamplesPerPixel, int maxSamplesPerPixel,
			int additional1dSamples, int additional2dSamples) {
		
		super(xStart, yStart, xEnd, yEnd, minSamplesPerPixel, additional1dSamples, additional2dSamples);
		
		this.minSamplesPerPixel = minSamplesPerPixel;
		this.maxSamplesPerPixel = maxSamplesPerPixel;
	}
	
	/**
	 * Because this can be deserialized -- with only its startup parameters actually
	 * saved and restored -- we might need to initialize this outside of the
	 * constructor!
	 */
	private void initialize() {
		
		currentPixelX = getXStart();
		currentPixelY = getYStart();
		currentPixelSampleNumber = 0;
		totalPixelSampleCount = 0;
		
		pixelsGenerated = 0;
		setTotalSamples(-1);
		
		final int blockCount1D = getSamplesPerPixel(), blockCount2D = (int) ceil(sqrt((double) getSamplesPerPixel()));
		final int additionalBlockCount1D = getAdditional1DSamples(),
				additionalBlockCount2D = (int) ceil(sqrt((double) getAdditional2DSamples()));
		
		blockSize1D = 1d / (double) blockCount1D;
		blockSize2D = 1d / (double) blockCount2D;
		additionalSize1D = 1d / (double) additionalBlockCount1D;
		additionalSize2D = 1d / (double) additionalBlockCount2D;
		
		film = generate2D(getSamplesPerPixel(), blockCount2D, blockSize2D, blockSize2D);
		t = generate1D(getSamplesPerPixel(), blockCount1D, blockSize1D);
		lens = generate2D(getSamplesPerPixel(), blockCount2D, blockSize2D, blockSize2D);
		
		add1d = generate1D(getAdditional1DSamples(), additionalBlockCount1D, additionalSize1D);
		add2d = generate2D(getAdditional2DSamples(), additionalBlockCount2D, additionalSize2D, additionalSize2D);
		
		estimates = new ArrayList<>(maxSamplesPerPixel);
		
		initialized = true;
	}
	
	private double[] generate1D(int samples, int blockCount1D, double blockSize) {
		
		final double[] result = new double[blockCount1D];
		
		int sampleCount = 0;
		
		for (int i = 0; i < result.length; i++)
			if (sampleCount < samples) {
				final double jittered = Settings.RND.nextDouble() * blockSize;
				result[i] = jittered;
				sampleCount++;
			}
		
		return result;
	}
	
	private Point2D[][] generate2D(int samples, int blockCount2D, double blockSizeX, double blockSizeY) {
		
		final Point2D[][] result = new Point2D[blockCount2D][blockCount2D];
		
		int sampleCount = 0;
		
		for (int x = 0; x < result.length; x++)
			for (int y = 0; y < result[x].length; y++)
				if (sampleCount < samples) {
					final double jitteredX = Settings.RND.nextDouble() * blockSizeX,
							jitteredY = Settings.RND.nextDouble() * blockSizeY;
					result[x][y] = new Point2D(jitteredX, jitteredY);
					sampleCount++;
				}
			
		return result;
	}
	
	@Override
	public boolean hasNextSample() {
		
		if (!initialized)
			initialize();
		
		return currentPixelY <= getYEnd();
	}
	
	@Override
	public Sample getNextSample() {
		
		if (!initialized)
			initialize();
		
		if (!hasNextSample())
			return null;
		
		final int index1D = currentPixelSampleNumber, index2Dx = currentPixelSampleNumber / film.length,
				index2Dy = currentPixelSampleNumber % film.length;
		
		final Point2D filmPoint = new Point2D(
				currentPixelX + film[index2Dx][index2Dy].getX() + (double) index2Dx * blockSize2D,
				currentPixelY + film[index2Dx][index2Dy].getY() + (double) index2Dy * blockSize2D);
		final Point2D lensUV = new Point2D(lens[index2Dx][index2Dy].getX() + (double) index2Dx * blockSize2D,
				lens[index2Dx][index2Dy].getY() + (double) index2Dy * blockSize2D);
		final double tPoint = t[index1D];
		
		final List<Double> additional1DSamples = new LinkedList<>();
		for (int i = 0; i < add1d.length; i++) {
			final double si = (double) i * additionalSize1D;
			
			additional1DSamples.add(si + add1d[i]);
		}
		
		final List<Point2D> additional2DSamples = new LinkedList<>();
		for (int x = 0; x < add2d.length; x++) {
			final double sx = (double) x * additionalSize2D;
			for (int y = 0; y < add2d[x].length; y++) {
				final double sy = (double) y * additionalSize2D;
				
				additional2DSamples.add(new Point2D(sx + add2d[x][y].getX(), sy + add2d[x][y].getY()));
			}
		}
		
		final Sample result = new FixedSample(filmPoint, lensUV, tPoint, additional1DSamples, additional2DSamples);
		
		shuffle1D(add1d);
		shuffle2D(add2d);
		
		currentPixelSampleNumber++;
		totalPixelSampleCount++;
		
		if (currentPixelSampleNumber >= minSamplesPerPixel) {
			
			shuffle2D(film);
			shuffle2D(lens);
			shuffle1D(t);
			
			currentPixelSampleNumber = 0;
			
			if (!repeatCurrentPixel()) {
				
				estimates.clear();
				totalPixelSampleCount = 0;
				
				pixelsGenerated++;
				currentPixelX++;
				
				if (currentPixelX > getXEnd()) {
					
					currentPixelX = getXStart();
					currentPixelY++;
				}
			}
		}
		
		return result;
	}
	
	private void shuffle1D(double[] array) {
		
		for (int i = 0; i < array.length; i++) {
			
			int j = i;
			while (j == i)
				j = Settings.RND.nextInt(array.length);
			
			final double scratch = array[i];
			array[i] = array[j];
			array[j] = scratch;
		}
	}
	
	private void shuffle2D(Point2D[][] array) {
		
		for (int i = 0; i < array.length; i++) {
			
			int j = i;
			while (j == i)
				j = Settings.RND.nextInt(array.length);
			
			for (int y = 0; y < array[i].length; y++) {
				final Point2D scratch = array[i][y];
				array[i][y] = array[j][y];
				array[j][y] = scratch;
			}
		}
		
		for (int i = 0; i < array[0].length; i++) {
			
			int j = i;
			while (j == i)
				j = Settings.RND.nextInt(array[0].length);
			
			for (int x = 0; x < array.length; x++) {
				final Point2D scratch = array[x][i];
				array[x][i] = array[x][j];
				array[x][j] = scratch;
			}
		}
	}
	
	private boolean repeatCurrentPixel() {
		
		if (totalPixelSampleCount >= maxSamplesPerPixel)
			return false;
		
		final double averageLuminance = estimates.parallelStream().mapToDouble(XYZ::getY).average().orElse(0);
		
		final double stdDevLuminance = sqrt(
				estimates.parallelStream().mapToDouble(XYZ::getY).map(v -> pow(v - averageLuminance, 2)).sum());
		
		return (stdDevLuminance > 1d / (double) minSamplesPerPixel);
		
	}
	
	@Override
	public boolean reportSampleResult(EstimatedSample estimate) {
		
		this.estimates.add(XYZ.fromSpectrum(estimate.getRadiance(), true));
		
		return true;
	}
	
	@Override
	public double getPercentComplete() {
		
		return ((double) pixelsGenerated) / ((double) (getXEnd() - getXStart() + 1) * (getYEnd() - getYStart() + 1));
	}
	
	@Override
	public Sampler partition(int xStart, int yStart, int xEnd, int yEnd) {
		
		return new AdaptiveSampler(xStart, yStart, xEnd, yEnd, this.minSamplesPerPixel, this.maxSamplesPerPixel,
				getAdditional1DSamples(), getAdditional2DSamples());
	}
	
}
