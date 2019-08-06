package org.snowjak.rays.renderer;

import static org.apache.commons.math3.util.FastMath.abs;

import org.snowjak.rays.Primitive;
import org.snowjak.rays.Scene;
import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.annotations.UIType;
import org.snowjak.rays.geometry.Ray;
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
		@UIField(name = "lightSamples", type = Integer.class, defaultValue = "1") })
public class PathTracingRenderer extends Renderer {
	
	private int maxDepth;
	private int lightSamples;
	
	public PathTracingRenderer(int maxDepth, int lightSamples) {
		
		this.maxDepth = maxDepth;
		this.lightSamples = lightSamples;
	}
	
	@Override
	public EstimatedSample estimate(TracedSample sample, Scene scene) {
		
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
		return new EstimatedSample(sample.getSample(), irradiance);
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
					
					final var cos_i = lightV.dotProduct(interaction.getNormal());
					
					//
					// We clamp (w` .dot. n) to [0,)
					// It follows that if we clamped it to 0,
					// we don't need to bother computing the rest. We know it will
					// all multiply out to 0.
					//
					if (cos_i <= 0d)
						continue;
						
					//
					// Compute the g(X,X`) term.
					//
					if (!light.isVisible(interaction, lightV, scene))
						continue;
					
					final var matSample = mat.pdfReflectionW_i(interaction, sample.getSample(), lightV);
					if (matSample.getA() <= 0d)
						continue;
					
					final var matAlbedo = matSample.getB();
					
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
		
		final var mat = interaction.getInteracted().getMaterial();
		
		final var reflection = mat.sampleReflectionW_i(interaction, sample.getSample());
		final var reflectiveV = reflection.getA().normalize();
		final var reflectivePdf = reflection.getB();
		final var reflectionAlbedo = reflection.getC();
		
		if (reflectivePdf <= 0d)
			return SpectralPowerDistribution.BLACK;
		
		final var cos_i = reflectiveV.dotProduct(interaction.getNormal());
		if (cos_i <= 0d)
			return SpectralPowerDistribution.BLACK;
		
		final var reflectiveRay = new Ray(interaction.getPoint(), reflectiveV, 0,
				interaction.getInteractingRay().getDepth() + 1);
		
		final var reflectedRadiance = this.estimate(new TracedSample(sample.getSample(), reflectiveRay), scene)
				.getRadiance();
		
		return reflectedRadiance.multiply(cos_i).multiply(reflectionAlbedo);
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
			
			final var transmissiveRay = new Ray(interaction.getPoint(), transmissiveV, 0,
					interaction.getInteractingRay().getDepth() + 1);
			final var transmissiveIncident = this.estimate(new TracedSample(sample.getSample(), transmissiveRay), scene)
					.getRadiance().multiply(cos_i);
			
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
