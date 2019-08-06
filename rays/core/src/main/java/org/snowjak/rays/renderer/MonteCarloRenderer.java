package org.snowjak.rays.renderer;

import static org.apache.commons.math3.util.FastMath.abs;

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
			return material.sampleLe(interaction, sample.getSample()).getB();
		else
			return SpectralPowerDistribution.BLACK;
	}
	
	@Override
	protected Spectrum estimateIndirectLighting(Interaction<Primitive> interaction, TracedSample sample, Scene scene) {
		
		final var material = interaction.getInteracted().getMaterial();
		
		if (material.isReflective()) {
			if (material.isDelta()) {
				final var matSample = material.sampleReflectionW_i(interaction, sample.getSample());
				final var matV = matSample.getA();
				final var matPDF = matSample.getB();
				final var matAlbedo = matSample.getC();
				
				final var cos_i = Vector3D.from(interaction.getNormal()).normalize().dotProduct(matV.normalize());
				
				final var reflectedRay = new Ray(interaction.getPoint(), matV, sample.getRay().getDepth() + 1);
				
				final var radiance = estimate(new TracedSample(sample.getSample(), reflectedRay), scene).getRadiance();
				return radiance.multiply(cos_i / matPDF).multiply(matAlbedo);
				
			} else {
				
				Spectrum irradiance = SpectralPowerDistribution.BLACK;
				for (int i = 0; i < n; i++) {
					final var matSample = material.sampleReflectionW_i(interaction, sample.getSample());
					final var matV = matSample.getA();
					final var matPDF = matSample.getB();
					final var matAlbedo = matSample.getC();
					
					final var cos_i = Vector3D.from(interaction.getNormal()).normalize().dotProduct(matV.normalize());
					
					final var reflectedRay = new Ray(interaction.getPoint(), matV, sample.getRay().getDepth() + 1);
					
					final var radiance = estimate(new TracedSample(sample.getSample(), reflectedRay), scene)
							.getRadiance();
					irradiance = irradiance.add(radiance.multiply(cos_i / matPDF).multiply(matAlbedo));
				}
				
				return irradiance.multiply(1d / n);
				
			}
		} else
			return SpectralPowerDistribution.BLACK;
	}
	
	@Override
	protected Spectrum estimateTransmissiveRadiance(Interaction<Primitive> interaction, TracedSample sample,
			Scene scene) {
		
		final var material = interaction.getInteracted().getMaterial();
		
		if (material.isTransmissive()) {
			if (material.isDelta()) {
				final var matSample = material.sampleTransmissionW_i(interaction, sample.getSample());
				final var matV = matSample.getA();
				final var matPDF = matSample.getB();
				
				final var cos_i = abs(Vector3D.from(interaction.getNormal()).normalize().dotProduct(matV.normalize()));
				
				final var reflectedRay = new Ray(interaction.getPoint(), matV, sample.getRay().getDepth() + 1);
				
				final var radiance = estimate(new TracedSample(sample.getSample(), reflectedRay), scene).getRadiance();
				return radiance.multiply(cos_i / matPDF);
				
			} else {
				
				Spectrum irradiance = SpectralPowerDistribution.BLACK;
				for (int i = 0; i < n; i++) {
					final var matSample = material.sampleTransmissionW_i(interaction, sample.getSample());
					final var matV = matSample.getA();
					final var matPDF = matSample.getB();
					
					final var cos_i = abs(
							Vector3D.from(interaction.getNormal()).normalize().dotProduct(matV.normalize()));
					
					final var reflectedRay = new Ray(interaction.getPoint(), matV, sample.getRay().getDepth() + 1);
					
					final var radiance = estimate(new TracedSample(sample.getSample(), reflectedRay), scene)
							.getRadiance();
					irradiance = irradiance.add(radiance.multiply(cos_i / matPDF));
				}
				
				return irradiance.multiply(1d / n);
				
			}
		} else
			return SpectralPowerDistribution.BLACK;
	}
	
}
