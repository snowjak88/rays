package org.snowjak.rays.renderer;

import org.snowjak.rays.Scene;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.geometry.Vector3D;
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
public class PathTracingRenderer extends Renderer {
	
	private int maxDepth;
	
	public PathTracingRenderer(int maxDepth) {
		
		this.maxDepth = maxDepth;
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
		final var primitive = interaction.getInteracted();
		final var mat = primitive.getMaterial();
		
		Spectrum energy = new SpectralPowerDistribution();
		
		//
		// Add direct-illumination
		if (mat.isDirectLightable()) {
			Spectrum totalDirectLightingIrradiance = new SpectralPowerDistribution();
			
			for (Light light : scene.getLights()) {
				
				final var lightP = light.sampleSurface(interaction);
				final var lightV = Vector3D.from(lightP.subtract(interaction.getPoint()));
				
				//
				// If the direction to the light is on the other side of the surface from the
				// normal, then we won't be able to see it and it doesn't count.
				final var cos_i = lightV.normalize().dotProduct(Vector3D.from(interaction.getNormal()));
				if (cos_i < 0d)
					continue;
					
				//
				// Determine if we can see the light-source at all.
				if (!light.isVisible(lightP, interaction, scene))
					continue;
					
				//
				// Calculate the total energy available after falloff.
				final var falloff = 1d / lightV.getMagnitudeSq();
				final var lightIrradiance = light.getRadiance(lightP, interaction).multiply(falloff).multiply(cos_i);
				
				totalDirectLightingIrradiance = totalDirectLightingIrradiance.add(lightIrradiance);
			}
			
			energy = energy.add(mat.getDirectLightReflection(interaction, totalDirectLightingIrradiance));
		}
		
		//
		// Add path-traced emission
		if (mat.isEmissive()) {
			final var emissiveV = mat.getEmissionV(interaction, sample.getSample());
			final var emissiveE = mat.getEmission(interaction, emissiveV);
			energy = energy.add(emissiveE);
		}
		
		//
		// Add path-traced reflection
		if (mat.isReflective()) {
			
			final var reflectiveV = mat.getReflectionV(interaction, sample.getSample());
			
			final var reflectiveRay = new Ray(interaction.getPoint(), reflectiveV, 0,
					interaction.getInteractingRay().getDepth() + 1);
			final var reflectiveIncident = this.estimate(new TracedSample(sample.getSample(), reflectiveRay), scene);
			
			final var reflectiveE = mat.getReflection(interaction, reflectiveV, reflectiveIncident.getRadiance());
			energy = energy.add(reflectiveE);
		}
		
		//
		// Add path-traced transmission
		if (mat.isTransmissive()) {
			final var transmissiveV = mat.getTransmissionV(interaction, sample.getSample());
			
			final var transmissiveRay = new Ray(interaction.getPoint(), transmissiveV, 0,
					interaction.getInteractingRay().getDepth() + 1);
			final var transmissiveIncident = this.estimate(new TracedSample(sample.getSample(), transmissiveRay),
					scene);
			
			final var transmissiveE = mat.getReflection(interaction, transmissiveV, transmissiveIncident.getRadiance());
			energy = energy.add(transmissiveE);
		}
		
		return new EstimatedSample(sample.getSample(), energy);
	}
	
}
