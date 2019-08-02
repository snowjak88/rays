package org.snowjak.rays.material;

import static org.apache.commons.math3.util.FastMath.PI;
import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.sin;
import static org.apache.commons.math3.util.FastMath.sqrt;

import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.annotations.UIType;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.interact.Interactable;
import org.snowjak.rays.interact.Interaction;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;
import org.snowjak.rays.texture.Texture;

/**
 * A material that has perfect Lambertian diffuse reflection, no transmission,
 * and no emission.
 * 
 * @author snowjak88
 *
 */
@UIType(type = "lambertian", fields = { @UIField(name = "texture", type = Texture.class) })
public class LambertianMaterial implements Material {
	
	private Texture texture;
	
	public LambertianMaterial(Texture texture) {
		
		this.texture = texture;
	}
	
	@Override
	public boolean isReflective() {
		
		return true;
	}
	
	@Override
	public <T extends Interactable<T>> MaterialSample getReflectionSample(Interaction<T> interaction, Sample sample) {
		
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
		return new MaterialSample(i.multiply(x).add(j.multiply(y)).add(k.multiply(z)).normalize(), 1d / (2d * PI),
				texture.getSpectrum(interaction).multiply(1d / PI));
	}
	
	@Override
	public <T extends Interactable<T>> MaterialSample getReflectionSample(Interaction<T> interaction,
			Vector3D direction) {
		
		return new MaterialSample(direction, 1 / (2d * PI), texture.getSpectrum(interaction));
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
		
		return false;
	}
	
	@Override
	public <T extends Interactable<T>> MaterialSample getEmissionSample(Interaction<T> interaction, Sample sample) {
		
		return new MaterialSample(interaction.getW_e(), 0.0, SpectralPowerDistribution.BLACK);
	}
	
	@Override
	public <T extends Interactable<T>> MaterialSample getEmissionP(Interaction<T> interaction, Vector3D direction) {
		
		return new MaterialSample(interaction.getW_e(), 0.0, SpectralPowerDistribution.BLACK);
	}
	
}
