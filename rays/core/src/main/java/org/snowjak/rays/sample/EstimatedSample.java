package org.snowjak.rays.sample;

import java.io.Serializable;

import org.snowjak.rays.spectrum.Spectrum;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;

/**
 * Represents the result of estimating a single {@link Sample}.
 * 
 * @author snowjak88
 *
 */
public class EstimatedSample implements Serializable {
	
	private static final long serialVersionUID = -2042013448563306365L;
	private final Sample sample;
	private final Spectrum radiance;
	
	/**
	 * Construct a 0-energy EstimatedSample.
	 * 
	 * @param sample
	 * @return
	 */
	public static EstimatedSample zero(TracedSample sample) {
		
		return new EstimatedSample(sample.getSample(), SpectralPowerDistribution.BLACK);
	}
	
	public EstimatedSample(Sample sample, Spectrum radiance) {
		
		this.sample = sample;
		this.radiance = radiance;
	}
	
	/**
	 * @return the {@link Sample} associated with this estimate
	 */
	public Sample getSample() {
		
		return sample;
	}
	
	/**
	 * @return the radiant-estimate associated with the sample
	 */
	public Spectrum getRadiance() {
		
		return radiance;
	}
}