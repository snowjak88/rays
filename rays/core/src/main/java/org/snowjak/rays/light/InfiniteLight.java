/**
 * 
 */
package org.snowjak.rays.light;

import static org.apache.commons.math3.util.FastMath.PI;

import org.snowjak.rays.Scene;
import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.annotations.UIType;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.interact.Interactable;
import org.snowjak.rays.interact.Interaction;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.spectrum.Spectrum;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;
import org.snowjak.rays.util.Trio;
import org.snowjak.rays.util.Util;

/**
 * Represents an "infinite" environmental light -- light that contributes the
 * same radiance across the whole {@link Scene} from all directions.
 * 
 * @author snowjak88
 *
 */
@UIType(type = "infinite", fields = { @UIField(name = "radiance", type = SpectralPowerDistribution.class) })
public class InfiniteLight implements Light {
	
	private SpectralPowerDistribution radiance;
	
	public InfiniteLight(SpectralPowerDistribution radiance) {
		
		this.radiance = radiance;
	}
	
	@Override
	public boolean isInfinite() {
		
		return true;
	}
	
	@Override
	public <T extends Interactable<T>> Trio<Vector3D, Double, Spectrum> sample(Interaction<T> interaction,
			Sample sample) {
		
		return new Trio<Vector3D, Double, Spectrum>(Util.sampleHemisphere(interaction.getNormal(), sample),
				1d / (2d * PI), radiance);
	}
	
	@Override
	public <T extends Interactable<T>> double pdf_sample(Interaction<T> interaction, Vector3D w_i, Scene scene) {
		
		final double pdf;
		final var worldInteraction = scene.getInteraction(new Ray(interaction.getPoint(), w_i.normalize()));
		if (worldInteraction != null)
			pdf = 0d;
		else
			pdf = 1d / (2d * PI);
		
		return pdf;
	}
	
}
