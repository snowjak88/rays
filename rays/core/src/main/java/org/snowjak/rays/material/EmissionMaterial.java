/**
 * 
 */
package org.snowjak.rays.material;

import static org.apache.commons.math3.util.FastMath.PI;

import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.interact.Interactable;
import org.snowjak.rays.interact.Interaction;
import org.snowjak.rays.light.Light;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.spectrum.Spectrum;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;
import org.snowjak.rays.util.Duo;
import org.snowjak.rays.util.Trio;

/**
 * {@link Material} which is purely emissive. If a {@link Light} has physical
 * form, this can be used to represent its physical appearance.
 * 
 * @author snowjak88
 *
 */
public class EmissionMaterial implements Material {
	
	private static final Trio<Vector3D, Double, Spectrum> NO_REFLECTION_SAMPLE = new Trio<>(Vector3D.J, 0.0,
			SpectralPowerDistribution.BLACK);
	
	private static final Duo<Double, Spectrum> NO_REFLECTION_PDF = new Duo<Double, Spectrum>(0.0,
			SpectralPowerDistribution.BLACK);
	
	private static final Duo<Vector3D, Double> NO_TRANSMISSION_SAMPLE = new Duo<>(Vector3D.J, 0.0);
	
	private transient SpectralPowerDistribution radiantIntensity;
	
	/**
	 * Construct a new EmissionMaterial using the given {@code specificPower} W m^-2
	 * sr^-1.
	 * 
	 * @param specificPower
	 */
	public EmissionMaterial(SpectralPowerDistribution radiantIntensity) {
		
		this.radiantIntensity = (SpectralPowerDistribution) radiantIntensity.multiply(2d * PI);
	}
	
	@Override
	public boolean isReflective() {
		
		return false;
	}
	
	@Override
	public <T extends Interactable<T>> Trio<Vector3D, Double, Spectrum> sampleReflectionW_i(Interaction<T> interaction,
			Sample sample) {
		
		return NO_REFLECTION_SAMPLE;
	}
	
	@Override
	public <T extends Interactable<T>> Duo<Double, Spectrum> pdfReflectionW_i(Interaction<T> interaction, Sample sample,
			Vector3D w_i) {
		
		return NO_REFLECTION_PDF;
	}
	
	@Override
	public boolean isTransmissive() {
		
		return false;
	}
	
	@Override
	public <T extends Interactable<T>> Duo<Vector3D, Double> sampleTransmissionW_i(Interaction<T> interaction,
			Sample sample) {
		
		return NO_TRANSMISSION_SAMPLE;
	}
	
	@Override
	public <T extends Interactable<T>> double pdfTransmissionW_i(Interaction<T> interaction, Sample sample,
			Vector3D w_i) {
		
		return 0;
	}
	
	@Override
	public boolean isEmissive() {
		
		return true;
	}
	
	@Override
	public <T extends Interactable<T>> Duo<Double, Spectrum> sampleLe(Interaction<T> interaction, Sample sample) {
		
		return new Duo<>(1d / (2d * PI), radiantIntensity);
	}
	
}
