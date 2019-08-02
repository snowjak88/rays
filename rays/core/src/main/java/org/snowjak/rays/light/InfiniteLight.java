/**
 * 
 */
package org.snowjak.rays.light;

import static org.apache.commons.math3.util.FastMath.*;

import org.snowjak.rays.Scene;
import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.annotations.UIType;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.interact.Interactable;
import org.snowjak.rays.interact.Interaction;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;

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
	public double getFalloff(double distanceSq) {
		
		return 1d;
	}
	
	@Override
	public <T extends Interactable<T>> LightSample sample(Interaction<T> interaction, Sample sample) {
		
		return new LightSample(interaction.getPoint(), interaction.getW_e(), 0d, radiance);
	}
	
	@Override
	public <T extends Interactable<T>> LightSample sample(Interaction<T> interaction, Ray sampleDirection,
			Scene scene) {
		
		final double pdf;
		final var worldInteraction = scene.getInteraction(sampleDirection);
		if (worldInteraction != null)
			pdf = 0d;
		else
			pdf = 1d / (4d * PI);
		
		return new LightSample(interaction.getPoint(), sampleDirection.getDirection().negate(), pdf, radiance);
	}
	
}
