package org.snowjak.rays.sampler;

import org.snowjak.rays.sample.EstimatedSample;
import org.snowjak.rays.sample.FixedSample;
import org.snowjak.rays.sample.Sample;

/**
 * A sampler is something that generates {@link FixedSample}s.
 * <p>
 * Advanced implementations may override
 * {@link #reportSampleResult(FixedSample)} (e.g., to implement supersampling on
 * regions with highly-contrasting samples).
 * </p>
 * 
 * @author snowjak88
 *
 */
public abstract class Sampler {
	
	private final int xStart, yStart;
	private final int xEnd, yEnd;
	private final int samplesPerPixel;
	private final int additional1DSamples;
	private final int additional2DSamples;
	
	public Sampler(int xStart, int yStart, int xEnd, int yEnd, int samplesPerPixel, int additional1DSamples,
			int additional2DSamples) {
		
		this.xStart = xStart;
		this.yStart = yStart;
		this.xEnd = xEnd;
		this.yEnd = yEnd;
		this.samplesPerPixel = samplesPerPixel;
		this.additional1DSamples = additional1DSamples;
		this.additional2DSamples = additional2DSamples;
	}
	
	/**
	 * Return <code>true</code> if any {@link FixedSample}s can still be generated
	 * in this sampler's domain, otherwise <code>false</code>.
	 */
	public abstract boolean hasNextSample();
	
	/**
	 * Generate the next {@link Sample} from this sampler's domain, or
	 * <code>null</code> if no more Samples remain.
	 * 
	 * @return
	 */
	public abstract Sample getNextSample();
	
	/**
	 * After computing an {@link EstimatedSample}, report that estimate back. This
	 * sampler will return <code>true</code> if the computed estimate is acceptable.
	 * As a side effect, this sampler may also generate more {@link Sample}s for
	 * that region.
	 * <p>
	 * This trivial implementation will always return <code>true</code>, and have no
	 * side-effects.
	 * </p>
	 * 
	 * @param estimate
	 * @return
	 */
	public boolean reportSampleResult(EstimatedSample estimate) {
		
		return true;
	}
	
	public int getXStart() {
		
		return xStart;
	}
	
	public int getYStart() {
		
		return yStart;
	}
	
	public int getXEnd() {
		
		return xEnd;
	}
	
	public int getYEnd() {
		
		return yEnd;
	}
	
	public int getSamplesPerPixel() {
		
		return samplesPerPixel;
	}
	
	public int getAdditional1DSamples() {
		
		return additional1DSamples;
	}
	
	public int getAdditional2DSamples() {
		
		return additional2DSamples;
	}
	
}
