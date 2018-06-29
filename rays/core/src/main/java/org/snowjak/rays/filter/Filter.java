package org.snowjak.rays.filter;

import org.snowjak.rays.sample.Sample;

/**
 * Represents a filter of {@link Sample}s with respect to image-locations.
 * <p>
 * Raytracing ultimately is all about estimating an integral using a set of
 * samples. Because each pixel-location is discrete, we need a way of
 * determining how much each Sample film-point contributes to each pixel in the
 * resulting image.
 * </p>
 * <p>
 * Filter implementations are responsible for defining this contribution. Each
 * Filter must define:
 * <ul>
 * <li>its extent (for any given pixel-location, how many neighboring pixels can
 * be contributed to)</li>
 * <li>for a given Sample and pixel, a method for calculating that Sample's
 * contribution-fraction to that pixel</li>
 * </ul>
 * </p>
 * 
 * @author snowjak88
 *
 */
public interface Filter {
	
	/**
	 * This filter will affect neighboring pixels +/-{@link #getExtentX()} from any
	 * given pixel-location. (An extent of "0" signifies that any FixedSample will
	 * only ever contribute to 1 pixel.)
	 * 
	 */
	public int getExtentX();
	
	/**
	 * This filter will affect neighboring pixels +/-{@link #getExtentY()} from any
	 * given pixel-location. (An extent of "0" signifies that any FixedSample will
	 * only ever contribute to 1 pixel.)
	 * 
	 */
	public int getExtentY();
	
	/**
	 * Get a fraction representing this {@link Sample}'s contribution to the given
	 * pixel (located at <code>pixelX</code>,<code>pixelY</code>).
	 * 
	 * @param sample
	 * @param pixelX
	 * @param pixelY
	 * @return
	 */
	public double getContribution(Sample sample, int pixelX, int pixelY);
	
	/**
	 * Returns <code>true</code> if the given {@link Sample} is within this filter's
	 * extents or not.
	 */
	public default boolean isContributing(Sample sample, int pixelX, int pixelY) {
		
		final int filmX = (int) sample.getFilmPoint().getX(), filmY = (int) sample.getFilmPoint().getY();
		
		return (filmX >= pixelX - getExtentX() && filmX <= pixelX + getExtentX())
				&& (filmY >= pixelY - getExtentY() && filmY <= pixelY + getExtentY());
	}
	
}
