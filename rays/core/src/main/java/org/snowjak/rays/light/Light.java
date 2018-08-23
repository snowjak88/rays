package org.snowjak.rays.light;

import org.snowjak.rays.Scene;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.interact.Interactable;
import org.snowjak.rays.interact.Interaction;
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
	public <T extends Interactable<T>> Point3D sampleSurface(Interaction<T> interaction);
	
	/**
	 * Determines if the given {@code surface} point on this Light is visible from
	 * the given {@link Interaction} -- i.e., if anything in this {@link Scene}
	 * interferes with direct line-of-sight.
	 * 
	 * @param interaction
	 * @param scene
	 * @return
	 */
	public <T extends Interactable<T>> boolean isVisible(Point3D surface, Interaction<T> interaction, Scene scene);
	
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
