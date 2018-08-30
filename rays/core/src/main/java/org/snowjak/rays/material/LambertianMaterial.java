package org.snowjak.rays.material;

import static org.apache.commons.math3.util.FastMath.*;

import org.snowjak.rays.annotations.UIType;
import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.interact.Interactable;
import org.snowjak.rays.interact.Interaction;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.spectrum.Spectrum;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;
import org.snowjak.rays.texture.Texture;

/**
 * A material that has perfect Lambertian diffuse reflection, no transmission,
 * and no emission.
 * 
 * @author snowjak88
 *
 */
@UIType(fields = { @UIField(name = "texture", type = Texture.class) })
public class LambertianMaterial implements Material {
	
	private Texture texture;
	
	public LambertianMaterial(Texture texture) {
		
		this.texture = texture;
	}
	
	@Override
	public boolean isDirectLightable() {
		
		return true;
	}
	
	@Override
	public <T extends Interactable<T>> Spectrum getDirectLightReflection(Interaction<T> interaction,
			Spectrum irradiance) {
		
		return SpectralPowerDistribution.fromRGB(texture.getRGB(interaction)).multiply(irradiance);
	}
	
	@Override
	public boolean isReflective() {
		
		return true;
	}
	
	@Override
	public <T extends Interactable<T>> Vector3D getReflectionV(Interaction<T> interaction, Sample sample) {
		
		final var sphericalPoint = sample.getAdditional2DSample();
		
		final var sin2_theta = sphericalPoint.getX();
		final var cos2_theta = 1d - sin2_theta;
		final var sin_theta = sqrt(sin2_theta);
		final var cos_theta = sqrt(cos2_theta);
		
		final var orientation = sphericalPoint.getY() * 2d * PI;
		//
		//
		//
		final var x = sin_theta * cos(orientation);
		final var y = cos_theta;
		final var z = sin_theta * sin(orientation);
		
		//
		//
		// Construct a coordinate system centered around the surface-normal.
		final var j = Vector3D.from(interaction.getNormal()).normalize();
		final var i = j.orthogonal();
		final var k = i.crossProduct(j);
		//
		//
		// Convert the Cartesian coordinates to a Vector in the constructed
		// coordinate system.
		return i.multiply(x).add(j.multiply(y)).add(k.multiply(z)).normalize();
	}
	
	@Override
	public <T extends Interactable<T>> double getReflectionP(Interaction<T> interaction, Vector3D direction) {
		
		return 1d / (2d * PI);
	}
	
	@Override
	public <T extends Interactable<T>> Spectrum getReflection(Interaction<T> interaction, Vector3D direction,
			Spectrum incident) {
		
		final var cos_i = Vector3D.from(interaction.getNormal()).normalize().dotProduct(direction.normalize());
		
		return SpectralPowerDistribution.fromRGB(texture.getRGB(interaction)).multiply(incident.multiply(cos_i));
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
