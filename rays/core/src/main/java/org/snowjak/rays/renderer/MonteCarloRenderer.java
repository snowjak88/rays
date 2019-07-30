package org.snowjak.rays.renderer;

import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.Primitive;
import org.snowjak.rays.Scene;
import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.annotations.UIType;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.interact.Interaction;
import org.snowjak.rays.material.Material.MaterialSample;
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
		@UIField(name = "n", type = Integer.class, defaultValue = "8"),
		@UIField(name = "lightSamples", type = Integer.class, defaultValue = "1") })
public class MonteCarloRenderer extends PathTracingRenderer {
	
	private int n;
	
	/**
	 * Construct a new MonteCarloRenderer.
	 * 
	 * @param n
	 *            the number of sample-points to use for each radiance estimate
	 * @param lightSamples
	 *            the number of sample-points to use for each direct-lighting
	 *            estimate
	 */
	public MonteCarloRenderer(int maxDepth, int n, int lightSamples) {
		
		super(maxDepth, lightSamples);
		this.n = n;
	}
	
	@Override
	protected Spectrum estimateEmission(Interaction<Primitive> interaction, TracedSample sample) {
		
		final var material = interaction.getInteracted().getMaterial();
		
		if (material.isEmissive())
			return estimate(() -> material.getEmissionSample(interaction, sample.getSample()),
					(s) -> material.getEmission(interaction, s.getDirection()));
		else
			return new SpectralPowerDistribution();
	}
	
	@Override
	protected Spectrum estimateReflection(Interaction<Primitive> interaction, TracedSample sample, Scene scene) {
		
		final var material = interaction.getInteracted().getMaterial();
		
		if (material.isReflective())
			return estimate(
					() -> material.getReflectionSample(interaction, sample
							.getSample()),
					(s) -> material.getReflection(interaction, s.getDirection(),
							estimate(new TracedSample(sample.getSample(),
									new Ray(interaction.getPoint(), s.getDirection(), sample.getRay().getDepth() + 1)),
									scene).getRadiance().multiply(
											Vector3D.from(interaction.getNormal()).dotProduct(s.getDirection()))));
		else
			return new SpectralPowerDistribution();
	}
	
	@Override
	protected Spectrum estimateTransmission(Interaction<Primitive> interaction, TracedSample sample, Scene scene) {
		
		final var material = interaction.getInteracted().getMaterial();
		
		if (material.isTransmissive())
			return estimate(
					() -> material.getTransmissionSample(interaction, sample
							.getSample()),
					(s) -> material.getTransmission(interaction, s.getDirection(),
							estimate(new TracedSample(sample.getSample(),
									new Ray(interaction.getPoint(), s.getDirection(), sample.getRay().getDepth() + 1)),
									scene).getRadiance()
											.multiply(FastMath.abs(Vector3D.from(interaction.getNormal())
													.dotProduct(s.getDirection())))));
		
		else
			return new SpectralPowerDistribution();
	}
	
	private Spectrum estimate(Supplier<MaterialSample> sampler, Function<MaterialSample, Spectrum> function) {
		
		Spectrum energy = new SpectralPowerDistribution();
		
		if (n > 0) {
			for (int i = 0; i < n; i++) {
				
				final var sample = sampler.get();
				final var addend = function.apply(sample).multiply(1d / sample.getPdf());
				
				energy = energy.add(addend);
				
			}
			
			energy = energy.multiply(1d / (double) n);
		}
		
		return energy;
		
	}
	
}
