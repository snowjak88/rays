package org.snowjak.rays.material;

import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.annotations.UIType;
import org.snowjak.rays.geometry.Normal3D;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.interact.Interactable;
import org.snowjak.rays.interact.Interaction;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.spectrum.Spectrum;
import org.snowjak.rays.spectrum.colorspace.RGB;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;
import org.snowjak.rays.texture.ConstantTexture;
import org.snowjak.rays.texture.Texture;
import org.snowjak.rays.util.Duo;
import org.snowjak.rays.util.Trio;

/**
 * A mirror is perfectly reflective and has an optional tint applied to all
 * reflected energy.
 * 
 * @author snowjak88
 *
 */
@UIType(type = "perfect-mirror", fields = { @UIField(name = "tint", type = Texture.class) })
public class PerfectMirrorMaterial implements Material {
	
	private static final Duo<Double, Spectrum> NO_REFLECTION_PDF = new Duo<Double, Spectrum>(0.0,
			SpectralPowerDistribution.BLACK);
	
	private static final Duo<Vector3D, Double> NO_TRANSMISSION_SAMPLE = new Duo<>(Vector3D.J, 0.0);
	
	private static final Duo<Double, Spectrum> NO_EMISSION_SAMPLE = new Duo<>(0.0, SpectralPowerDistribution.BLACK);
	
	private Texture tint = null;
	
	public PerfectMirrorMaterial() {
		
		this(new ConstantTexture(RGB.WHITE));
	}
	
	public PerfectMirrorMaterial(Texture tint) {
		
		this.tint = tint;
	}
	
	public Texture getTint() {
		
		if (tint == null)
			tint = new ConstantTexture(RGB.WHITE);
		
		return tint;
	}
	
	@Override
	public boolean isReflective() {
		
		return true;
	}
	
	@Override
	public <T extends Interactable<T>> Trio<Vector3D, Double, Spectrum> sampleReflectionW_i(Interaction<T> interaction,
			Sample sample) {
		
		return new Trio<>(getReflection(interaction.getW_e(), interaction.getNormal()), 1d,
				(Spectrum) getTint().getSpectrum(interaction));
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
		
		return false;
	}
	
	@Override
	public <T extends Interactable<T>> Duo<Double, Spectrum> sampleLe(Interaction<T> interaction, Sample sample) {
		
		return NO_EMISSION_SAMPLE;
	}
	
	/**
	 * Calculate the reflection of {@code v} about {@code n}.
	 * <p>
	 * Computes:
	 * 
	 * <pre>
	 * r = -v + 2(v . n)n
	 * </pre>
	 * 
	 * where {@code .} is the dot-product
	 * </p>
	 * 
	 * @param v
	 * @param n
	 * @return
	 */
	private Vector3D getReflection(Vector3D v, Normal3D n) {
		
		final var nv = Vector3D.from(n).normalize();
		
		return v.negate().add(nv.multiply(2d * v.dotProduct(nv)));
	}
	
}
