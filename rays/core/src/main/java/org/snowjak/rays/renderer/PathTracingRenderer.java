package org.snowjak.rays.renderer;

import org.apache.commons.math3.util.FastMath;
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
		Spectrum energy = new SpectralPowerDistribution();
		
		//
		// Add direct-illumination
		energy = energy.add(estimateDirectLighting(interaction, sample, scene));
		
		//
		// Add path-traced emission
		energy = energy.add(estimateEmission(interaction, sample));
		
		//
		// Add path-traced reflection
		energy = energy.add(estimateReflection(interaction, sample, scene));
		
		//
		// Add path-traced transmission
		energy = energy.add(estimateTransmission(interaction, sample, scene));
		
		//
		//
		return new EstimatedSample(sample.getSample(), energy);
	}
	
	protected Spectrum estimateDirectLighting(Interaction<Primitive> interaction, TracedSample sample, Scene scene) {
		
		final var mat = interaction.getInteracted().getMaterial();
		
		Spectrum result = new SpectralPowerDistribution();
		if (mat.isDirectLightable()) {
			
			for (Light light : scene.getLights()) {
				
				//
				// If this is a delta (i.e., dimensionless) light, then we'll only
				// bother doing 1 sample.
				//
				final var lightSampleCount = (light.isDelta() ? 1 : lightSamples);
				
				Spectrum totalLightIrradiance = SpectralPowerDistribution.BLACK;
				
				for (int n = 0; n < lightSampleCount; n++) {
					
					final var lightSample = light.sampleSurface(interaction, sample.getSample());
					final var lightP = lightSample.getPoint();
					final var lightV = lightSample.getDirection();
					final var lightPDF = lightSample.getPdf();
					
					final var scatteringPDF = mat.getReflectionP(interaction, lightV);
					
					//
					// If the direction to the light is on the other side of the surface from the
					// normal, then we won't be able to see it and it doesn't count.
					final var cos_i = lightV.normalize().negate().dotProduct(Vector3D.from(interaction.getNormal()));
					if (cos_i < 0d)
						continue;
						
					//
					// Determine if we can see the light-source at all.
					if (!light.isVisible(lightP, interaction, scene))
						continue;
						
					//
					// If the light has no probability of being sampled, then we won't get any
					// illumination from it.
					if (lightPDF <= 0d)
						continue;
					
					final var falloff = 1d / Vector3D.from(interaction.getPoint(), lightP).getMagnitudeSq();
					
					final var weight = getPowerHeuristic(lightSampleCount, lightPDF, 1, scatteringPDF);
					
					//
					// Calculate the total energy available after falloff.
					final Spectrum lightIrradiance;
					if (light.isDelta())
						lightIrradiance = light.getRadiance(lightP, interaction).multiply(falloff).multiply(cos_i);
					else
						lightIrradiance = light.getRadiance(lightP, interaction).multiply(falloff).multiply(cos_i)
								.multiply(weight / scatteringPDF);
					
					totalLightIrradiance = totalLightIrradiance.add(lightIrradiance);
					
				}
				
				totalLightIrradiance = totalLightIrradiance.multiply(1d / (double) lightSampleCount);
				result = result.add(totalLightIrradiance);
			}
			
		}
		
		return mat.getDirectLightReflection(interaction, result);
	}
	
	protected Spectrum estimateEmission(Interaction<Primitive> interaction, TracedSample sample) {
		
		final var mat = interaction.getInteracted().getMaterial();
		
		if (mat.isEmissive()) {
			final var emissiveV = sample.getRay().getDirection().negate();
			return mat.getEmission(interaction, emissiveV);
		}
		
		return new SpectralPowerDistribution();
	}
	
	protected Spectrum estimateReflection(Interaction<Primitive> interaction, TracedSample sample, Scene scene) {
		
		final var mat = interaction.getInteracted().getMaterial();
		
		if (mat.isReflective()) {
			
			final var reflection = mat.getReflectionSample(interaction, sample.getSample());
			final var reflectiveV = reflection.getDirection();
			final var cos_i = FastMath.abs(reflectiveV.dotProduct(Vector3D.from(interaction.getNormal())));
			
			final var reflectiveRay = new Ray(interaction.getPoint(), reflectiveV, 0,
					interaction.getInteractingRay().getDepth() + 1);
			final var reflectiveIncident = this.estimate(new TracedSample(sample.getSample(), reflectiveRay), scene);
			
			return mat.getReflection(interaction, reflectiveV, reflectiveIncident.getRadiance()).multiply(cos_i);
		}
		
		return new SpectralPowerDistribution();
	}
	
	protected Spectrum estimateTransmission(Interaction<Primitive> interaction, TracedSample sample, Scene scene) {
		
		final var mat = interaction.getInteracted().getMaterial();
		
		if (mat.isTransmissive()) {
			final var transmission = mat.getTransmissionSample(interaction, sample.getSample());
			final var transmissiveV = transmission.getDirection();
			final var cos_i = FastMath.abs(transmissiveV.dotProduct(Vector3D.from(interaction.getNormal())));
			
			final var transmissiveRay = new Ray(interaction.getPoint(), transmissiveV, 0,
					interaction.getInteractingRay().getDepth() + 1);
			final var transmissiveIncident = this.estimate(new TracedSample(sample.getSample(), transmissiveRay),
					scene);
			
			return mat.getTransmission(interaction, transmissiveV, transmissiveIncident.getRadiance()).multiply(cos_i);
		}
		
		return new SpectralPowerDistribution();
	}
	
	protected double getPowerHeuristic(int nf, double pf, int ng, double pg) {
		
		final var f = nf * pf;
		final var g = ng * pg;
		return (f * f) / (f * f + g * g);
	}
}
