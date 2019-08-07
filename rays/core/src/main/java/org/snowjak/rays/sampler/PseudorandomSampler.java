package org.snowjak.rays.sampler;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.snowjak.rays.Settings;
import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.annotations.UIType;
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
@UIType(type = "pseudorandom", fields = { @UIField(name = "xStart", type = Double.class, defaultValue = "0"),
		@UIField(name = "yStart", type = Double.class, defaultValue = "0"),
		@UIField(name = "xEnd", type = Double.class, defaultValue = "399"),
		@UIField(name = "yEnd", type = Double.class, defaultValue = "299"),
		@UIField(name = "samplesPerPixel", type = Integer.class, defaultValue = "4"),
		@UIField(name = "additional1DSamples", type = Integer.class, defaultValue = "4"),
		@UIField(name = "additional2DSamples", type = Integer.class, defaultValue = "4") })
public class PseudorandomSampler extends Sampler {
	
	private transient boolean initialized = false;
	private transient int currentPixelX, currentPixelY;
	private transient int currentPixelSampleNumber;
	
	private transient long samplesGenerated = 0;
	
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
		
		initialize();
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
		
		initialized = true;
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
		
		final var filmPoint = new Point2D(((double) currentPixelX) + Settings.RND.nextDouble(),
				((double) currentPixelY) + Settings.RND.nextDouble());
		final var lensUV = new Point2D(Settings.RND.nextDouble(), Settings.RND.nextDouble());
		final var t = Settings.RND.nextDouble();
		final var additional1dSamples = IntStream.range(0, getAdditional1DSamples())
				.mapToObj(i -> Settings.RND.nextDouble()).collect(Collectors.toList());
		final var additional2dSamples = IntStream.range(0, getAdditional2DSamples())
				.mapToObj(i -> new Point2D(Settings.RND.nextDouble(), Settings.RND.nextDouble()))
				.collect(Collectors.toList());
		
		final var result = new FixedSample(filmPoint, lensUV, t, additional1dSamples, additional2dSamples);
		
		currentPixelSampleNumber++;
		if (currentPixelSampleNumber >= getSamplesPerPixel()) {
			
			currentPixelSampleNumber = 0;
			currentPixelX++;
			if (currentPixelX > getXEnd()) {
				currentPixelX = getXStart();
				currentPixelY++;
			}
		}
		
		samplesGenerated++;
		
		return result;
	}
	
	@Override
	public double getPercentComplete() {
		
		return ((double) samplesGenerated) / ((double) getTotalSamples());
	}
	
	@Override
	public Sampler partition(int xStart, int yStart, int xEnd, int yEnd) {
		
		return new PseudorandomSampler(xStart, yStart, xEnd, yEnd, getSamplesPerPixel(), getAdditional1DSamples(),
				getAdditional2DSamples());
	}
	
}
