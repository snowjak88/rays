package org.snowjak.rays.sampler;

import org.snowjak.rays.sample.Sample;

/**
 * A sampler is something that generates {@link Sample}s.
 * <p>
 * Advanced implementations may override {@link #reportSampleResult(Sample)}
 * (e.g., to implement supersampling on regions with highly-contrasting
 * samples).
 * </p>
 * 
 * @author snowjak88
 *
 */
public abstract class Sampler {
	
	private final long renderId;
	private final int xStart, yStart;
	private final int xEnd, yEnd;
	private final int samplesPerPixel;
	private final int additional1DSamples;
	private final int additional2DSamples;
	
	public Sampler(long renderId, int xStart, int yStart, int xEnd, int yEnd, int samplesPerPixel,
			int additional1DSamples, int additional2DSamples) {
		
		this.renderId = renderId;
		this.xStart = xStart;
		this.yStart = yStart;
		this.xEnd = xEnd;
		this.yEnd = yEnd;
		this.samplesPerPixel = samplesPerPixel;
		this.additional1DSamples = additional1DSamples;
		this.additional2DSamples = additional2DSamples;
	}
	
	/**
	 * Return <code>true</code> if any {@link Sample}s can still be generated in
	 * this sampler's domain, otherwise <code>false</code>.
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
	 * After using a {@link Sample} to compute a radiance estimate, report that
	 * estimate back. This sampler will return <code>true</code> if the computed
	 * estimate is acceptable. As a side effect, this sampler may also generate more
	 * {@link Sample}s for that region.
	 * <p>
	 * This trivial implementation will always return <code>true</code>, and have no
	 * side-effects.
	 * </p>
	 * 
	 * @param sample
	 * @return
	 */
	public boolean reportSampleResult(Sample sample) {
		
		return true;
	}
	
	public long getRenderId() {
		
		return renderId;
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
