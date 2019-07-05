/**
 * 
 */
package org.snowjak.rays.material;

import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.interact.Interactable;
import org.snowjak.rays.interact.Interaction;
import org.snowjak.rays.light.Light;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.spectrum.Spectrum;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;

/**
 * {@link Material} which is purely emissive. If a {@link Light} has physical
 * form, this can be used to represent its physical appearance.
 * 
 * @author snowjak88
 *
 */
public class EmissionMaterial implements Material {
	
	private transient SpectralPowerDistribution specificPower;
	
	/**
	 * Construct a new EmissionMaterial using the given {@code specificPower} (in
	 * candela, being luminous power per steradian).
	 * 
	 * @param specificPower
	 */
	public EmissionMaterial(SpectralPowerDistribution specificPower) {
		
		this.specificPower = specificPower;
	}
	
	@Override
	public boolean isDirectLightable() {
		
		return false;
	}
	
	@Override
	public <T extends Interactable<T>> Spectrum getDirectLightReflection(Interaction<T> interaction,
			Spectrum irradiance) {
		
		return SpectralPowerDistribution.BLACK;
	}
	
	@Override
	public boolean isReflective() {
		
		return false;
	}
	
	@Override
	public <T extends Interactable<T>> Vector3D getReflectionV(Interaction<T> interaction, Sample sample) {
		
		return interaction.getW_e();
	}
	
	@Override
	public <T extends Interactable<T>> double getReflectionP(Interaction<T> interaction, Vector3D direction) {
		
		return 1d;
	}
	
	@Override
	public <T extends Interactable<T>> Spectrum getReflection(Interaction<T> interaction, Vector3D direction,
			Spectrum incident) {
		
		return SpectralPowerDistribution.BLACK;
	}
	
	@Override
	public boolean isTransmissive() {
		
		return false;
	}
	
	@Override
	public <T extends Interactable<T>> Vector3D getTransmissionV(Interaction<T> interaction, Sample sample) {
		
		return interaction.getW_e();
	}
	
	@Override
	public <T extends Interactable<T>> double getTransmissionP(Interaction<T> interaction, Vector3D direction) {
		
		return 1d;
	}
	
	@Override
	public <T extends Interactable<T>> Spectrum getTransmission(Interaction<T> interaction, Vector3D direction,
			Spectrum incident) {
		
		return SpectralPowerDistribution.BLACK;
	}
	
	@Override
	public boolean isEmissive() {
		
		return true;
	}
	
	@Override
	public <T extends Interactable<T>> Vector3D getEmissionV(Interaction<T> interaction, Sample sample) {
		
		return interaction.getW_e();
	}
	
	@Override
	public <T extends Interactable<T>> double getEmissionP(Interaction<T> interaction, Vector3D direction) {
		
		return 1d;
	}
	
	@Override
	public <T extends Interactable<T>> Spectrum getEmission(Interaction<T> interaction, Vector3D direction) {
		
		return specificPower;
	}
	
}