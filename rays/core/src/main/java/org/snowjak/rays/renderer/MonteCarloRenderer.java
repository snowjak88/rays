package org.snowjak.rays.renderer;

import java.util.function.Function;
import java.util.function.Supplier;

import org.snowjak.rays.Primitive;
import org.snowjak.rays.Scene;
import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.annotations.UIType;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.interact.Interaction;
import org.snowjak.rays.sample.TracedSample;
import org.snowjak.rays.spectrum.Spectrum;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;

/**
 * A {@link Renderer} that implements Monte-Carlo estimation.
 * 
 * @author snowjak88
 *
 */
@UIType(type = "monte-carlo", fields = { @UIField(name = "maxDepth", type = Integer.class, defaultValue = "4"),
		@UIField(name = "n", type = Integer.class, defaultValue = "8") })
public class MonteCarloRenderer extends PathTracingRenderer {
	
	private int n;
	
	/**
	 * Construct a new MonteCarloRenderer.
	 * 
	 * @param n
	 *            the number of sample-points to use for each radiance estimate
	 */
	public MonteCarloRenderer(int maxDepth, int n) {
		
		super(maxDepth);
		this.n = n;
	}
	
	@Override
	protected Spectrum estimateEmission(Interaction<Primitive> interaction, TracedSample sample) {
		
		final var material = interaction.getInteracted().getMaterial();
		
		if (material.isEmissive())
			return estimate(() -> material.getEmissionV(interaction, sample.getSample()),
					(v) -> material.getEmission(interaction, v), (v) -> material.getEmissionP(interaction, v));
		else
			return new SpectralPowerDistribution();
	}
	
	@Override
	protected Spectrum estimateReflection(Interaction<Primitive> interaction, TracedSample sample, Scene scene) {
		
		final var material = interaction.getInteracted().getMaterial();
		
		return estimate(() -> material.getReflectionV(interaction, sample.getSample()), (v) -> material.getReflection(
				interaction, v,
				estimate(new TracedSample(sample.getSample(),
						new Ray(interaction.getPoint(), v, sample.getRay().getDepth() + 1)), scene).getRadiance()),
				(v) -> material.getReflectionP(interaction, v));
	}
	
	@Override
	protected Spectrum estimateTransmission(Interaction<Primitive> interaction, TracedSample sample, Scene scene) {
		
		final var material = interaction.getInteracted().getMaterial();
		
		if (material.isTransmissive())
			return estimate(() -> material.getTransmissionV(interaction, sample.getSample()),
					(v) -> material.getTransmission(interaction, v,
							estimate(new TracedSample(sample.getSample(),
									new Ray(interaction.getPoint(), v, sample.getRay().getDepth() + 1)), scene)
											.getRadiance()),
					(v) -> material.getTransmissionP(interaction, v));
		
		else
			return new SpectralPowerDistribution();
	}
	
	private Spectrum estimate(Supplier<Vector3D> sampler, Function<Vector3D, Spectrum> function,
			Function<Vector3D, Double> probability) {
		
		Spectrum energy = new SpectralPowerDistribution();
		
		if (n > 0) {
			for (int i = 0; i < n; i++) {
				
				final var direction = sampler.get();
				final var addend = function.apply(direction).multiply(1d / probability.apply(direction));
				
				energy = energy.add(addend);
				
			}
			
			energy = energy.multiply(1d / (double) n);
		}
		
		return energy;
		
	}
	
}
