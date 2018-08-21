package org.snowjak.rays.bxdf;

import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.interact.Interactable;
import org.snowjak.rays.interact.Interaction;
import org.snowjak.rays.sample.Sample;

/**
 * Represents a generic Bidirectional (Reflectance/Scattering) Distribution
 * Function. Governs how reflection, transmittance, scattering, etc. behave.
 * 
 * @author snowjak88
 *
 */
public interface BxDF {
	
	/**
	 * Indicates whether this BxDF should be queried for reflection.
	 * 
	 * @return
	 */
	public boolean isReflective();
	
	/**
	 * For the given {@link Interaction}, select a reflection-direction from this
	 * BxDF's reflection-direction space. (We need the {@link Sample} associated
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
	 * Indicates whether this BxDF should be queried for transmission.
	 * 
	 * @return
	 */
	public boolean isTransmittive();
	
	/**
	 * For the given {@link Interaction}, select a transmission-direction from this
	 * BxDF's transmission-direction space. (We need the {@link Sample} associated
	 * with the current interaction, too, as that can help us to select
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
	 * Indicates whether this BxDF should be queried for emission.
	 * 
	 * @return
	 */
	public boolean isEmissive();
	
	/**
	 * For the given {@link Interaction}, select a emission-direction from this
	 * BxDF's emission-direction space. (We need the {@link Sample} associated with
	 * the current interaction, too, as that can help us to select
	 * suitably-distributed directions.)
	 * 
	 * @param interaction
	 * @param sample
	 * @return
	 */
	public <T extends Interactable<T>> Vector3D getEmission(Interaction<T> interaction, Sample sample);
	
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
	
}
