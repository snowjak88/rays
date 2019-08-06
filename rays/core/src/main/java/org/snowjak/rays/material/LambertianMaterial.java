package org.snowjak.rays.material;

import static org.apache.commons.math3.util.FastMath.PI;

import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.annotations.UIType;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.interact.Interactable;
import org.snowjak.rays.interact.Interaction;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.spectrum.Spectrum;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;
import org.snowjak.rays.texture.Texture;
import org.snowjak.rays.util.Duo;
import org.snowjak.rays.util.Trio;
import org.snowjak.rays.util.Util;

/**
 * A material that has perfect Lambertian diffuse reflection, no transmission,
 * and no emission.
 * 
 * @author snowjak88
 *
 */
@UIType(type = "lambertian", fields = { @UIField(name = "texture", type = Texture.class) })
public class LambertianMaterial implements Material {
	
	private static final Duo<Double, Spectrum> NO_REFLECTION_PDF = new Duo<Double, Spectrum>(0.0,
			SpectralPowerDistribution.BLACK);
	
	private static final Duo<Vector3D, Double> NO_TRANSMISSION_SAMPLE = new Duo<>(Vector3D.J, 0.0);
	
	private static final Duo<Double, Spectrum> NO_EMISSION_SAMPLE = new Duo<>(0.0, SpectralPowerDistribution.BLACK);
	
	private static final double BRDF_PDF = 1d / (2d * PI);
	
	private Texture texture;
	
	public LambertianMaterial(Texture texture) {
		
		this.texture = texture;
	}
	
	@Override
	public boolean isReflective() {
		
		return true;
	}
	
	@Override
	public <T extends Interactable<T>> Trio<Vector3D, Double, Spectrum> sampleReflectionW_i(Interaction<T> interaction,
			Sample sample) {
		
		return new Trio<>(Util.sampleHemisphere(interaction.getNormal(), sample), BRDF_PDF,
				texture.getSpectrum(interaction).multiply(1d / PI));
	}
	
	@Override
	public <T extends Interactable<T>> Duo<Double, Spectrum> pdfReflectionW_i(Interaction<T> interaction, Sample sample,
			Vector3D w_i) {
		
		final var cos_i = w_i.dotProduct(interaction.getNormal());
		
		if (cos_i <= 0)
			return NO_REFLECTION_PDF;
		
		return new Duo<Double, Spectrum>(BRDF_PDF, texture.getSpectrum(interaction).multiply(1d / PI));
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
		
		return false;
	}
	
	@Override
	public <T extends Interactable<T>> Duo<Double, Spectrum> sampleLe(Interaction<T> interaction, Sample sample) {
		
		return NO_EMISSION_SAMPLE;
	}
	
}
