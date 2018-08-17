package org.snowjak.rays.material;

import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.interact.Interactable;
import org.snowjak.rays.interact.Interaction;
import org.snowjak.rays.spectrum.Spectrum;

/**
 * Defines how an object appears. Defines how reflected energy is modified
 * (e.g., by surface color), how much energy (if any) is emitted, etc.
 * 
 * @author snowjak88
 *
 */
public interface Material {
	
	/**
	 * Given the specified {@link Interaction}, {@code reflection}-direction, and
	 * resulting {@code incident} energy-distribution, compute the resulting
	 * energy-distribution reflected back toward the observer.
	 * 
	 * @param interaction
	 * @param reflection
	 * @param incident
	 * @return
	 */
	public <T extends Interactable> Spectrum getReflected(Interaction<T> interaction, Vector3D reflection,
			Spectrum incident);
	
	/**
	 * Given the specified {@link Interaction}, {@code transmission}-direction, and
	 * resulting {@code incident} energy-distribution, compute the resulting
	 * energy-distribution transmitted back toward the observer.
	 * 
	 * @param interaction
	 * @param transmission
	 * @param incident
	 * @return
	 */
	public <T extends Interactable> Spectrum getTransmitted(Interaction<T> interaction, Vector3D transmission,
			Spectrum incident);
	
	/**
	 * Given the specified {@link Interaction} and {@code emission}-direction,
	 * compute the resulting energy-distribution emitted toward the observer.
	 * 
	 * @param interaction
	 * @param emission
	 * @return
	 */
	public <T extends Interactable> Spectrum getEmitted(Interaction<T> interaction, Vector3D emission);
	
}
