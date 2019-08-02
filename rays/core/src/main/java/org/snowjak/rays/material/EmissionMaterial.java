/**
 * 
 */
package org.snowjak.rays.material;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.interact.Interactable;
import org.snowjak.rays.interact.Interaction;
import org.snowjak.rays.light.Light;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;

/**
 * {@link Material} which is purely emissive. If a {@link Light} has physical
 * form, this can be used to represent its physical appearance.
 * 
 * @author snowjak88
 *
 */
public class EmissionMaterial implements Material {
	
	private transient SpectralPowerDistribution radiantIntensity;
	
	/**
	 * Construct a new EmissionMaterial using the given {@code specificPower} W m^-2
	 * sr^-1.
	 * 
	 * @param specificPower
	 */
	public EmissionMaterial(SpectralPowerDistribution radiantIntensity) {
		
		this.radiantIntensity = radiantIntensity;
	}
	
	@Override
	public boolean isReflective() {
		
		return false;
	}
	
	@Override
	public <T extends Interactable<T>> MaterialSample getReflectionSample(Interaction<T> interaction, Sample sample) {
		
		return new MaterialSample(interaction.getW_e(), 0.0, SpectralPowerDistribution.BLACK);
	}
	
	@Override
	public <T extends Interactable<T>> MaterialSample getReflectionSample(Interaction<T> interaction, Vector3D direction) {
		
		return new MaterialSample(interaction.getW_e(), 0.0, SpectralPowerDistribution.BLACK);
	}
	
	@Override
	public boolean isTransmissive() {
		
		return false;
	}
	
	@Override
	public <T extends Interactable<T>> MaterialSample getTransmissionSample(Interaction<T> interaction, Sample sample) {
		
		return new MaterialSample(interaction.getW_e(), 0.0, SpectralPowerDistribution.BLACK);
	}
	
	@Override
	public <T extends Interactable<T>> MaterialSample getTransmissionP(Interaction<T> interaction, Vector3D direction) {
		
		return new MaterialSample(interaction.getW_e(), 0.0, SpectralPowerDistribution.BLACK);
	}
	
	@Override
	public boolean isEmissive() {
		
		return true;
	}
	
	@Override
	public <T extends Interactable<T>> MaterialSample getEmissionSample(Interaction<T> interaction, Sample sample) {
		
		return new MaterialSample(interaction.getW_e(), 1d / (2d * FastMath.PI), radiantIntensity);
	}
	
	@Override
	public <T extends Interactable<T>> MaterialSample getEmissionP(Interaction<T> interaction, Vector3D direction) {
		
		return new MaterialSample(direction, 1d / (2d * FastMath.PI), radiantIntensity);
	}
	
}
