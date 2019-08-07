/**
 * 
 */
package org.snowjak.rays.light;

import static org.apache.commons.math3.util.FastMath.PI;

import java.util.function.Function;

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
import org.snowjak.rays.util.Quad;
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
	public <T extends Interactable<T>> Quad<Vector3D, Double, Spectrum, Function<Scene, Boolean>> sample(
			Interaction<T> interaction, Sample sample) {
		
		final var v = Util.sampleHemisphere(interaction.getNormal(), sample);
		final var visibilityRay = new Ray(interaction.getPoint(), v);
		
		return new Quad<>(v, 1d / (2d * PI * PI), radiance, (scene) -> (scene.getInteraction(visibilityRay) == null));
		
	}
	
	@Override
	public <T extends Interactable<T>> double pdf_sample(Interaction<T> interaction, Vector3D w_i, Scene scene) {
		
		return 1d / (2d * PI * PI);
	}
	
}
