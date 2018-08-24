package org.snowjak.rays.filter;

import org.snowjak.rays.sample.Sample;

/**
 * A box filter gives all Samples within its extent equal contribution.
 * 
 * @author snowjak88
 *
 */
public class BoxFilter implements Filter {
	
	private int extent;
	private transient int pixelsInBox = -1;
	
	/**
	 * @see BoxFilter
	 * @param extent
	 */
	public BoxFilter(int extent) {
		
		this.extent = extent;
	}
	
	@Override
	public int getExtentX() {
		
		return extent;
	}
	
	@Override
	public int getExtentY() {
		
		return extent;
	}
	
	public int getPixelsInBox() {
		
		if (pixelsInBox < 0)
			pixelsInBox = (extent + 1) * (extent + 1);
		
		return pixelsInBox;
	}
	
	@Override
	public double getContribution(Sample sample, int pixelX, int pixelY) {
		
		if (!isContributing(sample, pixelX, pixelY))
			return 0d;
		
		if (getPixelsInBox() == 0)
			return 1d;
		
		return 1d / (double) getPixelsInBox();
	}
	
}
