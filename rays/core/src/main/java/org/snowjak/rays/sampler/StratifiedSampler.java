/**
 * 
 */
package org.snowjak.rays.sampler;

import static org.apache.commons.math3.util.FastMath.ceil;
import static org.apache.commons.math3.util.FastMath.sqrt;

import java.util.LinkedList;
import java.util.List;

import org.snowjak.rays.Settings;
import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.annotations.UIType;
import org.snowjak.rays.geometry.Point2D;
import org.snowjak.rays.sample.FixedSample;
import org.snowjak.rays.sample.Sample;

/**
 * Implements a stratified {@link Sampler}.
 * <p>
 * This Sampler works by <em>stratification</em> -- i.e., by breaking the
 * sample-domain up into <em>strata</em> or blocks of equal size, and picking a
 * sample point for each block. This helps ensure that the picked samples are
 * relatively well-distributed.
 * </p>
 * 
 * @author snowjak88
 *
 */
@UIType(type = "stratified", fields = { @UIField(name = "xStart", type = Double.class, defaultValue = "0"),
		@UIField(name = "yStart", type = Double.class, defaultValue = "0"),
		@UIField(name = "xEnd", type = Double.class, defaultValue = "399"),
		@UIField(name = "yEnd", type = Double.class, defaultValue = "299"),
		@UIField(name = "samplesPerPixel", type = Integer.class, defaultValue = "4"),
		@UIField(name = "additional1DSamples", type = Integer.class, defaultValue = "4"),
		@UIField(name = "additional2DSamples", type = Integer.class, defaultValue = "4") })
public class StratifiedSampler extends Sampler {
	
	private transient boolean initialized = false;
	private transient int currentPixelX, currentPixelY;
	private transient int currentPixelSampleNumber;
	
	private transient double blockSize1D, blockSize2D, additionalSize1D, additionalSize2D;
	private transient Point2D[][] film, lens, add2d;
	private transient double[] t, add1d;
	
	private transient long samplesGenerated = 0;
	
	/**
	 * 
	 * @param xStart
	 * @param yStart
	 * @param xEnd
	 * @param yEnd
	 * @param samplesPerPixel
	 */
	public StratifiedSampler(int xStart, int yStart, int xEnd, int yEnd, int samplesPerPixel) {
		
		this(xStart, yStart, xEnd, yEnd, samplesPerPixel, 0, 0);
	}
	
	/**
	 * @param xStart
	 * @param yStart
	 * @param xEnd
	 * @param yEnd
	 * @param samplesPerPixel
	 * @param additional1dSamples
	 * @param additional2dSamples
	 */
	public StratifiedSampler(int xStart, int yStart, int xEnd, int yEnd, int samplesPerPixel, int additional1dSamples,
			int additional2dSamples) {
		
		super(xStart, yStart, xEnd, yEnd, samplesPerPixel, additional1dSamples, additional2dSamples);
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
		
		samplesGenerated = 0;
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
		samplesGenerated++;
		
		shuffle1D(add1d);
		shuffle2D(add2d);
		
		currentPixelSampleNumber++;
		
		if (currentPixelSampleNumber >= getSamplesPerPixel()) {
			
			shuffle2D(film);
			shuffle2D(lens);
			shuffle1D(t);
			
			currentPixelSampleNumber = 0;
			currentPixelX++;
			
			if (currentPixelX > getXEnd()) {
				
				currentPixelX = getXStart();
				currentPixelY++;
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
	
	@Override
	public double getPercentComplete() {
		
		return ((double) samplesGenerated) / ((double) getTotalSamples());
	}
	
	@Override
	public Sampler partition(int xStart, int yStart, int xEnd, int yEnd) {
		
		return new StratifiedSampler(xStart, yStart, xEnd, yEnd, getSamplesPerPixel(), getAdditional1DSamples(),
				getAdditional2DSamples());
	}
	
}
