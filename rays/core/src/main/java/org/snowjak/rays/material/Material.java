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
	 * Indicates whether this Material has only 1 reflection and 1 transmission
	 * vector -- i.e., if the renderer should only bother doing 1 sample each for
	 * reflection and transmission.
	 * 
	 * @return
	 */
	public default boolean isDelta() {
		
		return false;
	}
	
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
	 * @return {@link MaterialSample#getPdf()} == 0.0 if no reflection is possible
	 */
	public <T extends Interactable<T>> MaterialSample getReflectionSample(Interaction<T> interaction, Sample sample);
	
	/**
	 * For the given {@link Interaction} and previously-selected
	 * reflection-direction, compute the probability (in {@code [0,1]}) that this
	 * direction would have been selected out of the whole reflection-direction
	 * space.
	 * 
	 * @param interaction
	 * @param direction
	 * @return {@link MaterialSample#getPdf()} == 0.0 if this direction would never
	 *         have been chosen
	 */
	public <T extends Interactable<T>> MaterialSample getReflectionSample(Interaction<T> interaction, Vector3D direction);
	
	/**
	 * Indicates whether this Material should be queried for transmission.
	 * 
	 * @return
	 */
	public boolean isTransmissive();
	
	/**
	 * For the given {@link Interaction}, select a transmission-direction from this
	 * Material's transmission-direction space. (We need the {@link Sample}
	 * associated with the current interaction, too, as that can help us to select
	 * suitably-distributed directions.)
	 * 
	 * @param interaction
	 * @param sample
	 * @return {@link MaterialSample#getPdf()} == 0.0 if no transmission is possible
	 */
	public <T extends Interactable<T>> MaterialSample getTransmissionSample(Interaction<T> interaction, Sample sample);
	
	/**
	 * For the given {@link Interaction} and previously-selected
	 * transmission-direction, compute the probability (in {@code [0,1]}) that this
	 * direction would have been selected out of the whole transmission-direction
	 * space.
	 * 
	 * @param interaction
	 * @param direction
	 * @return {@link MaterialSample#getPdf()} == 0.0 if this direction would never
	 *         have been chosen
	 */
	public <T extends Interactable<T>> MaterialSample getTransmissionP(Interaction<T> interaction, Vector3D direction);
	
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
	 * <p>
	 * Note that {@link MaterialSample#getAlbedo()} will return the Material's
	 * <strong>radiance</strong>, not its albedo.
	 * </p>
	 * 
	 * @param interaction
	 * @param sample
	 * @return {@link MaterialSample#getPdf()} == 0.0 if no emission is possible
	 */
	public <T extends Interactable<T>> MaterialSample getEmissionSample(Interaction<T> interaction, Sample sample);
	
	/**
	 * For the given {@link Interaction} and previously-selected emission-direction,
	 * compute the probability (in {@code [0,1]}) that this direction would have
	 * been selected out of the whole emission-direction space.
	 * <p>
	 * Note that {@link MaterialSample#getAlbedo()} will return the Material's
	 * <strong>radiance</strong>, not its albedo.
	 * </p>
	 * 
	 * @param interaction
	 * @param direction
	 * @return {@link MaterialSample#getPdf()} == 0.0 if this direction would never
	 *         have been chosen
	 */
	public <T extends Interactable<T>> MaterialSample getEmissionP(Interaction<T> interaction, Vector3D direction);
	
	/**
	 * Data bean holding a sampled direction and its associated PDF for this
	 * Material.
	 * 
	 * @author snowjak88
	 *
	 */
	public static class MaterialSample {
		
		private final Vector3D direction;
		private final double pdf;
		private final Spectrum albedo;
		
		public MaterialSample(Vector3D direction, double pdf, Spectrum albedo) {
			
			this.direction = direction.normalize();
			this.pdf = pdf;
			this.albedo = albedo;
		}
		
		/**
		 * Get the normalized direction, extending away from the Material at the sampled
		 * point.
		 * 
		 * @return
		 */
		public Vector3D getDirection() {
			
			return direction;
		}
		
		/**
		 * Get the PDF that this particular direction would have been chosen.
		 * 
		 * @return
		 */
		public double getPdf() {
			
			return pdf;
		}
		
		/**
		 * Get the absorption Spectrum associated with this sample.
		 * 
		 * @return
		 */
		public Spectrum getAlbedo() {
			
			return albedo;
		}
		
	}
}
