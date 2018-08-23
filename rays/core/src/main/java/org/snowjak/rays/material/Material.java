package org.snowjak.rays.material;

import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.interact.Interactable;
import org.snowjak.rays.interact.Interaction;
import org.snowjak.rays.sample.Sample;
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
	 * Indicates whether this Material should be queried for reflection.
	 * 
	 * @return
	 */
	public boolean isReflective();
	
	/**
	 * For the given {@link Interaction}, select a reflection-direction from this
	 * Material's reflection-direction space. (We need the {@link Sample} associated
	 * with the current interaction, too, as that can help us to select
	 * suitably-distributed directions.)
	 * 
	 * @param interaction
	 * @param sample
	 * @return
	 */
	public <T extends Interactable<T>> Vector3D getReflectionV(Interaction<T> interaction, Sample sample);
	
	/**
	 * For the given {@link Interaction} and previously-selected
	 * reflection-direction, compute the probability (in {@code [0,1]}) that this
	 * direction would have been selected out of the whole reflection-direction
	 * space.
	 * 
	 * @param interaction
	 * @param direction
	 * @return
	 */
	public <T extends Interactable<T>> double getReflectionP(Interaction<T> interaction, Vector3D direction);
	
	/**
	 * Given that reflection is occuring at the given {@link Interaction}, in the
	 * specified {@code direction}, and the given estimated {@link Spectrum} as the
	 * incident energy for the reflection -- what energy-distribution is reflected
	 * back toward the eye-point?
	 * 
	 * @param interaction
	 * @param direction
	 * @param incident
	 * @return
	 */
	public <T extends Interactable<T>> Spectrum getReflection(Interaction<T> interaction, Vector3D direction,
			Spectrum incident);
	
	/**
	 * Indicates whether this Material should be queried for transmission.
	 * 
	 * @return
	 */
	public boolean isTransmittive();
	
	/**
	 * For the given {@link Interaction}, select a transmission-direction from this
	 * Material's transmission-direction space. (We need the {@link Sample}
	 * associated with the current interaction, too, as that can help us to select
	 * suitably-distributed directions.)
	 * 
	 * @param interaction
	 * @param sample
	 * @return
	 */
	public <T extends Interactable<T>> Vector3D getTransmissionV(Interaction<T> interaction, Sample sample);
	
	/**
	 * For the given {@link Interaction} and previously-selected
	 * transmission-direction, compute the probability (in {@code [0,1]}) that this
	 * direction would have been selected out of the whole transmission-direction
	 * space.
	 * 
	 * @param interaction
	 * @param direction
	 * @return
	 */
	public <T extends Interactable<T>> double getTransmissionP(Interaction<T> interaction, Vector3D direction);
	
	/**
	 * Given that transmission is occuring at the given {@link Interaction}, in the
	 * specified {@code direction}, and the given estimated {@link Spectrum} as the
	 * incident energy for the transmission -- what energy-distribution is
	 * transmitted back toward the eye-point?
	 * 
	 * @param interaction
	 * @param direction
	 * @param incident
	 * @return
	 */
	public <T extends Interactable<T>> Spectrum getTransmission(Interaction<T> interaction, Vector3D direction,
			Spectrum incident);
	
	/**
	 * Indicates whether this Material should be queried for emission.
	 * 
	 * @return
	 */
	public boolean isEmissive();
	
	/**
	 * For the given {@link Interaction}, select a emission-direction from this
	 * Material's emission-direction space. (We need the {@link Sample} associated
	 * with the current interaction, too, as that can help us to select
	 * suitably-distributed directions.)
	 * 
	 * @param interaction
	 * @param sample
	 * @return
	 */
	public <T extends Interactable<T>> Vector3D getEmissionV(Interaction<T> interaction, Sample sample);
	
	/**
	 * For the given {@link Interaction} and previously-selected emission-direction,
	 * compute the probability (in {@code [0,1]}) that this direction would have
	 * been selected out of the whole emission-direction space.
	 * 
	 * @param interaction
	 * @param direction
	 * @return
	 */
	public <T extends Interactable<T>> double getEmissionP(Interaction<T> interaction, Vector3D direction);
	
	/**
	 * Given that emission is occuring at the given {@link Interaction}, in the
	 * specified {@code direction} -- what energy-distribution is emitted toward the
	 * eye-point?
	 * 
	 * @param interaction
	 * @param direction
	 * @return
	 */
	public <T extends Interactable<T>> Spectrum getEmission(Interaction<T> interaction, Vector3D direction);
	
}
