package org.snowjak.rays.light;

import org.snowjak.rays.Scene;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.interact.Interactable;
import org.snowjak.rays.interact.Interaction;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.spectrum.Spectrum;

/**
 * Defines a light-source.
 * 
 * @author snowjak88
 *
 */
public interface Light {
	
	/**
	 * Given an {@link Interaction} somewhere in the {@link Scene}, sample a point
	 * on this Light that may possibly illuminate that Interaction.
	 * 
	 * @param interaction
	 * @return
	 */
	public <T extends Interactable<T>> Point3D sampleSurface(Interaction<T> interaction, Sample sample);
	
	/**
	 * Determines if the given {@code surface} point on this Light is visible from
	 * the given {@link Interaction} -- i.e., if anything in this {@link Scene}
	 * interferes with direct line-of-sight.
	 * 
	 * @param interaction
	 * @param scene
	 * @return
	 */
	public default <T extends Interactable<T>> boolean isVisible(Point3D surface, Interaction<T> interaction,
			Scene scene) {
		
		//
		// Set up a Ray from the Interaction to the surface-point, and see if we have
		// any interactions along that Ray.
		//
		final var iPoint = interaction.getPoint();
		final var dirInteractionToLight = Vector3D.from(surface).subtract(iPoint);
		
		final var lightDistance = dirInteractionToLight.getMagnitude();
		final var lightRay = new Ray(iPoint, Vector3D.from(surface).subtract(iPoint).normalize());
		
		final var lightInteraction = scene.getInteraction(lightRay);
		//
		// If there was an interaction along that ray,
		// and the distance to the interaction is less than the distance to the light,
		// then the interaction is occluding the light.
		//
		if (lightInteraction != null && lightDistance > lightInteraction.getInteractingRay().getT())
			return false;
		
		return true;
	}
	
	/**
	 * Computes the total radiance emitted by this Light from the given
	 * {@code surface}-point, toward the given {@link Interaction}. (Does not
	 * account for fall-off due to distance.)
	 * 
	 * @param surface
	 * @param interaction
	 * @return
	 */
	public <T extends Interactable<T>> Spectrum getRadiance(Point3D surface, Interaction<T> interaction);
	
}
