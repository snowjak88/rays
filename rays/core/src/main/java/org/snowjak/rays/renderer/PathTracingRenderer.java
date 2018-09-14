package org.snowjak.rays.renderer;

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
@UIType(type = "path-tracing", fields = { @UIField(name = "maxDepth", type = Integer.class, defaultValue = "4") })
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
		Spectrum energy = new SpectralPowerDistribution();
		
		//
		// Add direct-illumination
		energy = energy.add(estimateDirectLighting(interaction, scene));
		
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
	
	protected Spectrum estimateDirectLighting(Interaction<Primitive> interaction, Scene scene) {
		
		final var mat = interaction.getInteracted().getMaterial();
		
		Spectrum result = new SpectralPowerDistribution();
		if (mat.isDirectLightable()) {
			
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
				
				result = result.add(lightIrradiance);
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
			
			final var reflectiveV = mat.getReflectionV(interaction, sample.getSample());
			
			final var reflectiveRay = new Ray(interaction.getPoint(), reflectiveV, 0,
					interaction.getInteractingRay().getDepth() + 1);
			final var reflectiveIncident = this.estimate(new TracedSample(sample.getSample(), reflectiveRay), scene);
			
			return mat.getReflection(interaction, reflectiveV, reflectiveIncident.getRadiance());
		}
		
		return new SpectralPowerDistribution();
	}
	
	protected Spectrum estimateTransmission(Interaction<Primitive> interaction, TracedSample sample, Scene scene) {
		
		final var mat = interaction.getInteracted().getMaterial();
		
		if (mat.isTransmissive()) {
			final var transmissiveV = mat.getTransmissionV(interaction, sample.getSample());
			
			final var transmissiveRay = new Ray(interaction.getPoint(), transmissiveV, 0,
					interaction.getInteractingRay().getDepth() + 1);
			final var transmissiveIncident = this.estimate(new TracedSample(sample.getSample(), transmissiveRay),
					scene);
			
			return mat.getReflection(interaction, transmissiveV, transmissiveIncident.getRadiance());
		}
		
		return new SpectralPowerDistribution();
	}
}
