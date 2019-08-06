package org.snowjak.rays.sample;

import java.io.Serializable;

import org.snowjak.rays.interact.Interactable;
import org.snowjak.rays.interact.Interaction;
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
	private final Interaction<? extends Interactable<?>> interaction;
	private final Spectrum radiance;
	
	/**
	 * Construct a 0-energy EstimatedSample.
	 * 
	 * @param sample
	 * @return
	 */
	public static EstimatedSample zero(TracedSample sample) {
		
		return new EstimatedSample(sample.getSample(), null, SpectralPowerDistribution.BLACK);
	}
	
	public EstimatedSample(Sample sample, Spectrum radiance) {
		
		this(sample, null, radiance);
	}
	
	public EstimatedSample(Sample sample, Interaction<?> interaction, Spectrum radiance) {
		
		this.sample = sample;
		this.interaction = interaction;
		this.radiance = radiance;
	}
	
	/**
	 * @return the {@link Sample} associated with this estimate
	 */
	public Sample getSample() {
		
		return sample;
	}
	
	/**
	 * @return the {@link Interaction} associated with this estimate (may be
	 *         {@code null})
	 */
	public Interaction<?> getInteraction() {
		
		return interaction;
	}
	
	/**
	 * @return the radiant-estimate associated with the sample
	 */
	public Spectrum getRadiance() {
		
		return radiance;
	}
}