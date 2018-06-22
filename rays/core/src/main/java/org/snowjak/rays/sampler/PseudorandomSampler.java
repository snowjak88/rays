package org.snowjak.rays.sampler;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.snowjak.rays.Settings;
import org.snowjak.rays.geometry.Point2D;
import org.snowjak.rays.sample.FixedSample;
import org.snowjak.rays.sample.Sample;

/**
 * Implements a simple pseudo-random {@link Sampler}, where all sample-points
 * for each pixel are randomly selected within (+/-0.5, +/-0.5) of the pixel's
 * coordinates.
 * <p>
 * Note: pixels are considered according to their "continuous" coordinates, not
 * "discrete". For any given pixel (x,y), it's discrete coordinates are integers
 * and identify the left-top corner of the pixel. The continuous coordinates are
 * equal to (x+0.5, y+0.5) and identify the center of the pixel.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class PseudorandomSampler extends Sampler {
	
	private int currentPixelX, currentPixelY;
	private int currentPixelSampleNumber;
	
	/**
	 * Construct a new {@link PseudorandomSampler} across the given interval
	 * [<code>(xStart,yStart)</code>, <code>(xEnd,yEnd)</code>], with no additional
	 * points requested.
	 * 
	 * @param xStart
	 * @param yStart
	 * @param xEnd
	 * @param yEnd
	 * @param samplesPerPixel
	 * @param additional1dSamples
	 * @param additional2dSamples
	 */
	public PseudorandomSampler(int xStart, int yStart, int xEnd, int yEnd, int samplesPerPixel) {
		
		this(xStart, yStart, xEnd, yEnd, samplesPerPixel, 0, 0);
	}
	
	/**
	 * Construct a new {@link PseudorandomSampler} across the given interval
	 * [<code>(xStart,yStart)</code>, <code>(xEnd,yEnd)</code>].
	 * 
	 * @param xStart
	 * @param yStart
	 * @param xEnd
	 * @param yEnd
	 * @param samplesPerPixel
	 * @param additional1dSamples
	 * @param additional2dSamples
	 */
	public PseudorandomSampler(int xStart, int yStart, int xEnd, int yEnd, int samplesPerPixel, int additional1dSamples,
			int additional2dSamples) {
		
		super(xStart, yStart, xEnd, yEnd, samplesPerPixel, additional1dSamples, additional2dSamples);
		
		currentPixelX = xStart;
		currentPixelY = yStart;
		currentPixelSampleNumber = 0;
	}
	
	@Override
	public boolean hasNextSample() {
		
		return currentPixelY <= getYEnd();
	}
	
	@Override
	public Sample getNextSample() {
		
		if (!hasNextSample())
			return null;
		
		final var filmPoint = new Point2D(((double) currentPixelX) + Settings.RND.nextDouble(),
				((double) currentPixelY) + Settings.RND.nextDouble());
		final var lensUV = new Point2D(Settings.RND.nextDouble(), Settings.RND.nextDouble());
		final var t = Settings.RND.nextDouble();
		final var additional1dSamples = IntStream.range(0, getAdditional1DSamples())
				.mapToObj(i -> Settings.RND.nextDouble()).collect(Collectors.toList());
		final var additional2dSamples = IntStream.range(0, getAdditional2DSamples())
				.mapToObj(i -> new Point2D(Settings.RND.nextDouble(), Settings.RND.nextDouble()))
				.collect(Collectors.toList());
		
		final var result = new FixedSample(this, filmPoint, lensUV, t, additional1dSamples, additional2dSamples);
		
		currentPixelSampleNumber++;
		if (currentPixelSampleNumber >= getSamplesPerPixel()) {
			
			currentPixelSampleNumber = 0;
			currentPixelX++;
			if (currentPixelX > getXEnd()) {
				currentPixelX = getXStart();
				currentPixelY++;
			}
		}
		
		return result;
	}
	
}
