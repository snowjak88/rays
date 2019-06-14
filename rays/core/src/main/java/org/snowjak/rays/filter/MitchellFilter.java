/**
 * 
 */
package org.snowjak.rays.filter;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.annotations.UIType;
import org.snowjak.rays.sample.Sample;

/**
 * Implements a Mitchel-Netravali filter.
 * 
 * @author snowjak88
 */
@UIType(type = "mitchell", fields = { @UIField(name = "extent", type = Integer.class, defaultValue = "0"),
		@UIField(name = "b", type = Double.class, defaultValue = "0.5"),
		@UIField(name = "c", type = Double.class, defaultValue = "0.25") })
public class MitchellFilter implements Filter {
	
	private int extent;
	private double b, c;
	
	/**
	 * 
	 * @param extent
	 * @param b
	 * @param c
	 */
	public MitchellFilter(int extent, double b, double c) {
		
		this.extent = extent;
		this.b = b;
		this.c = c;
	}
	
	@Override
	public int getExtentX() {
		
		return extent;
	}
	
	@Override
	public int getExtentY() {
		
		return extent;
	}
	
	@Override
	public double getContribution(Sample sample, int pixelX, int pixelY) {
		
		if (!isContributing(sample, pixelX, pixelY))
			return 0d;
			
		//
		// Calculate the sample's position relative to the filter's center-point,
		// on the interval [-1,+1].
		//
		final double filterCenterX = (double) pixelX + 0.5, filterCenterY = (double) pixelY + 0.5;
		
		final double x = 2d * (sample.getFilmPoint().getX() - filterCenterX) / ((double) (extent + 1)),
				y = 2d * (sample.getFilmPoint().getY() - filterCenterY) / ((double) (extent + 1));
		
		return getMitchell1D(x) * getMitchell1D(y);
	}
	
	private double getMitchell1D(double x) {
		
		//
		// Convert x from the interval [-1,+1] to [0,2], reflected about 0.
		final double ax = 2d * FastMath.abs(x);
		
		if (ax > 1.0)
			return ((-b - 6.0 * c) * FastMath.pow(x, 3) + (6.0 * b + 30.0 * c) * FastMath.pow(x, 2)
					+ (-12.0 * b - 48.0 * c) * x + (8.0 * b + 24.0 * c)) * (1.0 / 6.0);
		
		return ((12.0 - 9.0 * b - 6.0 * c) * FastMath.pow(x, 3) + (-18.0 + 12.0 * b + 6.0 * c) * FastMath.pow(x, 2)
				+ (6.0 - 2.0 * b)) * (1.0 / 6.0);
		
	}
	
}
