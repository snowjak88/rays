package org.snowjak.rays.filter;

import org.snowjak.rays.sample.Sample;

/**
 * A box filter gives all Samples within its extent equal contribution.
 * 
 * @author snowjak88
 *
 */
public class BoxFilter implements Filter {
	
	private final int extent;
	private final int pixelsInBox;
	
	/**
	 * @see BoxFilter
	 * @param extent
	 */
	public BoxFilter(int extent) {
		
		this.extent = extent;
		this.pixelsInBox = (extent + 1) * (extent + 1);
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
		
		if (pixelsInBox == 0)
			return 1d;
		
		return 1d / (double) pixelsInBox;
	}
	
}
