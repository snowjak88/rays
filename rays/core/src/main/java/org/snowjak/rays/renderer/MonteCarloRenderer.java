package org.snowjak.rays.renderer;

import static org.apache.commons.math3.util.FastMath.abs;
import static org.apache.commons.math3.util.FastMath.max;

import java.util.function.Function;
import java.util.function.Supplier;

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
			return estimate(interaction, () -> material.getEmissionSample(interaction, sample.getSample()),
					(s) -> material.getEmission(interaction, s.getDirection()));
		else
			return new SpectralPowerDistribution();
	}
	
	@Override
	protected Spectrum estimateIndirectLightingRadiance(Interaction<Primitive> interaction, TracedSample sample,
			Scene scene) {
		
		final var material = interaction.getInteracted().getMaterial();
		
		if (material.isReflective())
			
			return estimate(interaction, () -> material.getReflectionSample(interaction, sample.getSample()), (s) -> {
				if (s.getPdf() <= 0d)
					return new SpectralPowerDistribution();
				
				final var cos_i = Vector3D.from(interaction.getNormal()).normalize().dotProduct(s.getDirection());
				if (cos_i <= 0d)
					return new SpectralPowerDistribution();
				
				final var radianceEstimate = estimate(
						new TracedSample(sample.getSample(),
								new Ray(interaction.getPoint(), s.getDirection(), sample.getRay().getDepth() + 1)),
						scene).getRadiance();
				
				return radianceEstimate.multiply(cos_i / s.getPdf());
			}, (material.isDelta() ? 1 : this.n));
		
		else
			return new SpectralPowerDistribution();
	}
	
	@Override
	protected Spectrum estimateTransmissiveRadiance(Interaction<Primitive> interaction, TracedSample sample,
			Scene scene) {
		
		final var material = interaction.getInteracted().getMaterial();
		
		if (material.isTransmissive())
			return estimate(interaction, () -> material.getTransmissionSample(interaction, sample.getSample()), (s) -> {
				final var cos_i = abs(Vector3D.from(interaction.getNormal()).normalize().dotProduct(s.getDirection()));
				final var radianceEstimate = estimate(
						new TracedSample(sample.getSample(),
								new Ray(interaction.getPoint(), s.getDirection(), sample.getRay().getDepth() + 1)),
						scene).getRadiance();
				return radianceEstimate.multiply(cos_i / s.getPdf());
			});
		
		else
			return new SpectralPowerDistribution();
	}
	
	private Spectrum estimate(Interaction<Primitive> interaction, Supplier<MaterialSample> sampler,
			Function<MaterialSample, Spectrum> indirectRadianceFunction) {
		
		return estimate(interaction, sampler, indirectRadianceFunction, this.n);
	}
	
	private Spectrum estimate(Interaction<Primitive> interaction, Supplier<MaterialSample> sampler,
			Function<MaterialSample, Spectrum> indirectRadianceFunction, int n) {
		
		Spectrum totalIndirectEnergy = new SpectralPowerDistribution();
		
		if (n > 0) {
			for (int i = 0; i < n; i++) {
				
				final var sample = sampler.get();
				final var cos_i = max(0d,
						Vector3D.from(interaction.getNormal()).normalize().dotProduct(sample.getDirection()));
				final var indirectRadiance = indirectRadianceFunction.apply(sample).multiply(cos_i / sample.getPdf());
				
				totalIndirectEnergy = totalIndirectEnergy.add(indirectRadiance);
			}
			
			totalIndirectEnergy = totalIndirectEnergy.multiply(1d / (double) n);
		}
		
		return totalIndirectEnergy;
	}
	
}
