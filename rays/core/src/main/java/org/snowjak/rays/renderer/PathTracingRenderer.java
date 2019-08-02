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
import org.snowjak.rays.light.Light.LightSample;
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
			return mat.getEmissionSample(interaction, sample.getSample()).getAlbedo();
		
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
		
		final var mat = interaction.getInteracted().getMaterial();
		
		Spectrum result = SpectralPowerDistribution.BLACK;
		if (mat.isReflective() && !mat.isDelta()) {
			
			for (Light light : scene.getLights()) {
				
				//
				// If this is a delta (i.e., dimensionless) light, then we'll only
				// bother doing 1 sample.
				//
				final var lightSampleCount = (light.isDelta() ? 1 : lightSamples);
				
				Spectrum totalIrradiance = SpectralPowerDistribution.BLACK;
				
				for (int n = 0; n < lightSampleCount; n++) {
					
					//
					// First, try to get a contribution from the light by sampling the light's
					// surface.
					//
					
					//
					// Sample the light source with multiple-importance sampling.
					//
					
					final var fromlightSample = light.sample(interaction, sample.getSample());
					if (fromlightSample.getPdf() > 0d && !fromlightSample.getRadiance().isBlack()) {
						
						Spectrum irradiance = SpectralPowerDistribution.BLACK;
						
						final var cos_i = fromlightSample.getDirection().negate().normalize()
								.dotProduct(Vector3D.from(interaction.getNormal()));
						
						final var matSample = mat.getReflectionSample(interaction,
								fromlightSample.getDirection().negate());
						final var albedo = matSample.getAlbedo().multiply(cos_i);
						
						final Spectrum lightRadiance;
						
						if (!albedo.isBlack() && light.isVisible(fromlightSample.getPoint(), interaction, scene))
							lightRadiance = fromlightSample.getRadiance();
						else
							lightRadiance = SpectralPowerDistribution.BLACK;
						
						if (!lightRadiance.isBlack())
							if (light.isDelta())
								irradiance = albedo.multiply(lightRadiance).multiply(1d / fromlightSample.getPdf());
							else {
								final var weight = getPowerHeuristic(1, fromlightSample.getPdf(), 1,
										matSample.getPdf());
								irradiance = albedo.multiply(lightRadiance).multiply(weight / fromlightSample.getPdf());
							}
						
						totalIrradiance = totalIrradiance.add(irradiance);
					}
					
					//
					// Sample the material with multiple-importance sampling.
					//
					
					if (!light.isDelta()) {
						final var matSample = mat.getReflectionSample(interaction, sample.getSample());
						
						final var cos_i = matSample.getDirection().normalize()
								.dotProduct(Vector3D.from(interaction.getNormal()));
						
						final var albedo = matSample.getAlbedo().multiply(cos_i);
						
						if (!albedo.isBlack() && matSample.getPdf() > 0) {
							
							final LightSample toLightSample;
							final double weight;
							
							if (mat.isDelta()) {
								toLightSample = null;
								weight = 1d;
							} else {
								
								final var fromMaterialRay = new Ray(interaction.getPoint(), matSample.getDirection());
								toLightSample = light.sample(interaction, fromMaterialRay, scene);
								
								if (toLightSample.getPdf() <= 0d)
									continue;
								weight = getPowerHeuristic(1, matSample.getPdf(), 1, toLightSample.getPdf());
							}
							
							boolean isHidden = false;
							if (toLightSample != null && !light.isVisible(toLightSample.getPoint(), interaction, scene))
								isHidden = true;
							
							final var radiance = toLightSample.getRadiance();
							if (!isHidden && !radiance.isBlack()) {
								
								final var irradiance = albedo.multiply(radiance).multiply(weight / matSample.getPdf());
								
								totalIrradiance = totalIrradiance.add(irradiance);
							}
						}
					}
				}
				
				totalIrradiance = totalIrradiance.multiply(1d / (double) lightSampleCount);
				result = result.add(totalIrradiance);
			}
			
		}
		
		return result;
	}
	
	protected Spectrum estimateIndirectLighting(Interaction<Primitive> interaction, TracedSample sample, Scene scene) {
		
		final var mat = interaction.getInteracted().getMaterial();
		final var reflection = mat.getReflectionSample(interaction, sample.getSample());
		final var reflectiveV = reflection.getDirection().normalize();
		final var cos_i = reflectiveV.dotProduct(Vector3D.from(interaction.getNormal()));
		
		if (cos_i <= 0d)
			return SpectralPowerDistribution.BLACK;
		
		final var reflectiveRay = new Ray(interaction.getPoint(), reflectiveV, 0,
				interaction.getInteractingRay().getDepth() + 1);
		return this.estimate(new TracedSample(sample.getSample(), reflectiveRay), scene).getRadiance().multiply(cos_i);
	}
	
	protected Spectrum estimateTransmissiveRadiance(Interaction<Primitive> interaction, TracedSample sample,
			Scene scene) {
		
		final var mat = interaction.getInteracted().getMaterial();
		Spectrum totalRadiance = SpectralPowerDistribution.BLACK;
		
		if (mat.isTransmissive()) {
			final var transmission = mat.getTransmissionSample(interaction, sample.getSample());
			final var transmissiveV = transmission.getDirection();
			final var cos_i = abs(transmissiveV.dotProduct(Vector3D.from(interaction.getNormal())));
			
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
