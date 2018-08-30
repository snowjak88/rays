package org.snowjak.rays.material;

import org.snowjak.rays.Settings;
import org.snowjak.rays.annotations.UIType;
import org.snowjak.rays.annotations.UIField;
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

/**
 * A mirror is perfectly reflective and has an optional tint applied to all
 * reflected energy.
 * 
 * @author snowjak88
 *
 */
@UIType(type = "perfect-mirror", fields = { @UIField(name = "tint", type = Texture.class) })
public class PerfectMirrorMaterial implements Material {
	
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
	public boolean isDirectLightable() {
		
		return false;
	}
	
	@Override
	public <T extends Interactable<T>> Spectrum getDirectLightReflection(Interaction<T> interaction,
			Spectrum irradiance) {
		
		return new SpectralPowerDistribution();
	}
	
	@Override
	public boolean isReflective() {
		
		return true;
	}
	
	@Override
	public <T extends Interactable<T>> Vector3D getReflectionV(Interaction<T> interaction, Sample sample) {
		
		return getReflection(interaction.getW_e(), interaction.getNormal());
	}
	
	@Override
	public <T extends Interactable<T>> double getReflectionP(Interaction<T> interaction, Vector3D direction) {
		
		final var reflection = getReflection(interaction.getW_e(), interaction.getNormal()).normalize();
		direction = direction.normalize();
		
		if (!(Settings.getInstance().nearlyEqual(reflection.getX(), direction.getX())
				&& Settings.getInstance().nearlyEqual(reflection.getY(), direction.getY())
				&& Settings.getInstance().nearlyEqual(reflection.getZ(), direction.getZ())))
			return 0d;
		
		return 1d;
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
	
	@Override
	public <T extends Interactable<T>> Spectrum getReflection(Interaction<T> interaction, Vector3D direction,
			Spectrum incident) {
		
		return incident.multiply(SpectralPowerDistribution.fromRGB(getTint().getRGB(interaction)));
	}
	
	@Override
	public boolean isTransmissive() {
		
		return false;
	}
	
	@Override
	public <T extends Interactable<T>> Vector3D getTransmissionV(Interaction<T> interaction, Sample sample) {
		
		return null;
	}
	
	@Override
	public <T extends Interactable<T>> double getTransmissionP(Interaction<T> interaction, Vector3D direction) {
		
		return 0;
	}
	
	@Override
	public <T extends Interactable<T>> Spectrum getTransmission(Interaction<T> interaction, Vector3D direction,
			Spectrum incident) {
		
		return new SpectralPowerDistribution();
	}
	
	@Override
	public boolean isEmissive() {
		
		return false;
	}
	
	@Override
	public <T extends Interactable<T>> Vector3D getEmissionV(Interaction<T> interaction, Sample sample) {
		
		return null;
	}
	
	@Override
	public <T extends Interactable<T>> double getEmissionP(Interaction<T> interaction, Vector3D direction) {
		
		return 0;
	}
	
	@Override
	public <T extends Interactable<T>> Spectrum getEmission(Interaction<T> interaction, Vector3D direction) {
		
		return new SpectralPowerDistribution();
	}
	
}
