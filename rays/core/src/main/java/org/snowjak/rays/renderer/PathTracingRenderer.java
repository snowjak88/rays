package org.snowjak.rays.renderer;

import static org.apache.commons.math3.util.FastMath.abs;

import org.snowjak.rays.Primitive;
import org.snowjak.rays.Scene;
import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.annotations.UIType;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.interact.Interaction;
import org.snowjak.rays.light.Light;
import org.snowjak.rays.sample.EstimatedSample;
import org.snowjak.rays.sample.TracedSample;
import org.snowjak.rays.spectrum.Spectrum;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;

/**
 * A path-tracing renderer will recursively spawn rays at each
 * reflection/transmission event until a certain number of reflections ("depth")
 * has been reached, or no further interactions are found.
 * 
 * @author snowjak88
 *
 */
@UIType(type = "path-tracing", fields = { @UIField(name = "maxDepth", type = Integer.class, defaultValue = "4"),
		@UIField(name = "n", type = Integer.class, defaultValue = "1"),
		@UIField(name = "lightSamples", type = Integer.class, defaultValue = "1") })
public class PathTracingRenderer extends Renderer {
	
	private int maxDepth = 4;
	private int lightSamples = 1;
	private int n = 1;
	
	public PathTracingRenderer() {
		
		this(4, 1, 1);
	}
	
	public PathTracingRenderer(int maxDepth, int n, int lightSamples) {
		
		this.maxDepth = maxDepth;
		this.n = n;
		this.lightSamples = lightSamples;
	}
	
	@Override
	public EstimatedSample estimate(TracedSample sample, Scene scene) {
		
		if (maxDepth < 1)
			maxDepth = 1;
		if (lightSamples < 1)
			lightSamples = 1;
		if (n < 1)
			n = 1;
			
		//
		// If we've exceeded our allowed depth, return.
		//
		if (sample.getRay().getDepth() >= maxDepth)
			return EstimatedSample.zero(sample);
			
		//
		// Attempt to find an Interaction between the given ray and the scene.
		//
		final var interaction = scene.getInteraction(sample.getRay());
		
		if (interaction == null)
			return EstimatedSample.zero(sample);
			
		//
		//
		//
		Spectrum irradiance = SpectralPowerDistribution.BLACK;
		
		//
		// Gather emission.
		//
		irradiance = irradiance.add(estimateEmission(interaction, sample));
		
		//
		// Gather reflection.
		//
		irradiance = irradiance.add(estimateReflection(interaction, sample, scene));
		
		//
		// Gather transmission.
		//
		irradiance = irradiance.add(estimateTransmissiveRadiance(interaction, sample, scene));
		
		//
		//
		return new EstimatedSample(sample.getSample(), interaction, irradiance);
	}
	
	protected Spectrum estimateEmission(Interaction<Primitive> interaction, TracedSample sample) {
		
		final var mat = interaction.getInteracted().getMaterial();
		
		if (mat.isEmissive())
			return mat.sampleLe(interaction, sample.getSample()).getB();
		
		return SpectralPowerDistribution.BLACK;
	}
	
	protected Spectrum estimateReflection(Interaction<Primitive> interaction, TracedSample sample, Scene scene) {
		
		final var mat = interaction.getInteracted().getMaterial();
		Spectrum totalRadiance = SpectralPowerDistribution.BLACK;
		
		if (mat.isReflective()) {
			
			//
			// Gather direct lighting.
			//
			totalRadiance = totalRadiance.add(estimateDirectLighting(interaction, sample, scene));
			
			//
			// Gather indirect lighting.
			//
			totalRadiance = totalRadiance.add(estimateIndirectLighting(interaction, sample, scene));
		}
		
		return totalRadiance;
	}
	
	protected Spectrum estimateDirectLighting(Interaction<Primitive> interaction, TracedSample sample, Scene scene) {
		
		//
		// We estimate direct lighting by computing the rendering equation for
		// direct-lighting in the area-form. For a sketch, see Light.sample()
		//
		
		final var mat = interaction.getInteracted().getMaterial();
		
		Spectrum result = SpectralPowerDistribution.BLACK;
		if (mat.isReflective() && !mat.isDelta()) {
			
			for (Light light : scene.getLights()) {
				
				Spectrum totalIrradiance = SpectralPowerDistribution.BLACK;
				
				//
				// We'll sample this light's solid-angle a number of times.
				//
				final var lightSampleCount = light.isDelta() ? 1 : lightSamples;
				for (int i = 0; i < lightSampleCount; i++) {
					
					//
					// Sample the light's solid-angle.
					//
					final var lightSample = light.sample(interaction, sample.getSample());
					final var lightV = lightSample.getA();
					final var lightPDF = lightSample.getB();
					final var lightRadiance = lightSample.getC();
					
					//
					// Compute (w` .dot. n)
					//
					final var cos_i = lightV.dotProduct(interaction.getNormal());
					
					if (cos_i <= 0d)
						continue;
						
					//
					// Compute g(X,X`)
					//
					final var visible = lightSample.getD().apply(scene);
					if (!visible)
						continue;
					
					final var matSample = mat.pdfReflectionW_i(interaction, sample.getSample(), lightV);
					if (matSample.getA() <= 0d)
						continue;
					
					final var matAlbedo = matSample.getB();
					
					//
					// Light.sample() already handles computing the terms for:
					// ( w` .dot. n` )
					// || X -> X` ||^2
					//
					// So we complete the rest of the equation:
					//
					// L_d( X, w ) = ( g(X, X`) p(X, w, w`) Le(X`, w`) (-w` .dot. n) ) / pdf( X` )
					//
					
					final var irradiance = lightRadiance.multiply(cos_i / lightPDF).multiply(matAlbedo);
					totalIrradiance = totalIrradiance.add(irradiance);
				}
				
				totalIrradiance = totalIrradiance.multiply(1d / (double) lightSampleCount);
				
				result = result.add(totalIrradiance);
			}
			
		}
		
		return result;
	}
	
	protected Spectrum estimateIndirectLighting(Interaction<Primitive> interaction, TracedSample sample, Scene scene) {
		
		//
		// We estimate indirect-lighting using the rendering equation in
		// directional-form
		//
		
		final var mat = interaction.getInteracted().getMaterial();
		
		Spectrum totalIrradiance = SpectralPowerDistribution.BLACK;
		
		final var reflectionSamples = mat.isDelta() ? 1 : n;
		for (int i = 0; i < reflectionSamples; i++) {
			
			final var reflection = mat.sampleReflectionW_i(interaction, sample.getSample());
			final var reflectiveV = reflection.getA().normalize();
			final var reflectivePdf = reflection.getB();
			final var reflectionAlbedo = reflection.getC();
			
			if (reflectivePdf <= 0d)
				continue;
			
			final var cos_i = reflectiveV.dotProduct(interaction.getNormal());
			if (cos_i <= 0d)
				continue;
			
			final var reflectiveRay = new Ray(interaction.getPoint(), reflectiveV,
					interaction.getInteractingRay().getDepth() + 1);
			
			final var reflectedEstimate = this.estimate(new TracedSample(sample.getSample(), reflectiveRay), scene);
			
			final double distanceSq;
			if (reflectedEstimate.getInteraction() == null || reflectedEstimate.getInteraction().getPoint() == null
					|| reflectedEstimate.getInteraction().getPoint().nearlyEquals(interaction.getPoint()))
				distanceSq = 1d;
			else
				distanceSq = Vector3D.from(interaction.getPoint(), reflectedEstimate.getInteraction().getPoint())
						.getMagnitudeSq();
			
			final var irradiance = reflectedEstimate.getRadiance().multiply(cos_i / (reflectivePdf * distanceSq))
					.multiply(reflectionAlbedo);
			
			totalIrradiance = totalIrradiance.add(irradiance);
		}
		
		totalIrradiance = totalIrradiance.multiply(1d / (double) reflectionSamples);
		
		return totalIrradiance;
	}
	
	protected Spectrum estimateTransmissiveRadiance(Interaction<Primitive> interaction, TracedSample sample,
			Scene scene) {
		
		final var mat = interaction.getInteracted().getMaterial();
		Spectrum totalRadiance = SpectralPowerDistribution.BLACK;
		
		if (mat.isTransmissive()) {
			final var transmission = mat.sampleTransmissionW_i(interaction, sample.getSample());
			final var transmissiveV = transmission.getA();
			final var transmissivePdf = transmission.getB();
			
			if (transmissivePdf <= 0d)
				return SpectralPowerDistribution.BLACK;
			
			final var cos_i = abs(transmissiveV.dotProduct(interaction.getNormal()));
			
			final var transmissiveRay = new Ray(interaction.getPoint(), transmissiveV,
					interaction.getInteractingRay().getDepth() + 1);
			final var transmissiveIncident = this.estimate(new TracedSample(sample.getSample(), transmissiveRay), scene)
					.getRadiance().multiply(cos_i / transmissivePdf);
			
			totalRadiance = transmissiveIncident;
		}
		
		return totalRadiance;
	}
	
	protected double getBalanceHeuristic(int nf, double pf, int ng, double pg) {
		
		return (nf * pf) / (nf * pf + ng * pg);
	}
	
	protected double getPowerHeuristic(int nf, double pf, int ng, double pg) {
		
		final var f = nf * pf;
		final var g = ng * pg;
		return (f * f) / (f * f + g * g);
	}
}
