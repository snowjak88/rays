package org.snowjak.rays.material;

import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.interact.Interactable;
import org.snowjak.rays.interact.Interaction;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.spectrum.Spectrum;
import org.snowjak.rays.util.Duo;
import org.snowjak.rays.util.Trio;

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
	 * For the given {@link Interaction}, select a reflection-direction {@code w_i}
	 * from this Material's reflection-direction space. (We need the {@link Sample}
	 * associated with the current interaction, too, as that can help us to select
	 * suitably-distributed directions.)
	 * 
	 * @param interaction
	 * @param sample
	 * @return {@link Trio} ({@code w_i}, PDF, albedo)
	 */
	public <T extends Interactable<T>> Trio<Vector3D, Double, Spectrum> sampleReflectionW_i(Interaction<T> interaction,
			Sample sample);
	
	/**
	 * For the given {@link Interaction} and previously-selected
	 * reflection-direction {@code w_i}, compute the probability (in {@code [0,1]})
	 * that this direction would have been selected out of the whole
	 * reflection-direction space.
	 * 
	 * @param interaction
	 * @param w_i
	 * @return {@link Duo}(PDF, albedo)
	 */
	public <T extends Interactable<T>> Duo<Double, Spectrum> pdfReflectionW_i(Interaction<T> interaction, Sample sample,
			Vector3D w_i);
	
	/**
	 * Indicates whether this Material should be queried for transmission.
	 * 
	 * @return
	 */
	public boolean isTransmissive();
	
	/**
	 * For the given {@link Interaction}, select a transmission-direction
	 * {@code w_i} from this Material's transmission-direction space. (We need the
	 * {@link Sample} associated with the current interaction, too, as that can help
	 * us to select suitably-distributed directions.)
	 * 
	 * @param interaction
	 * @param sample
	 * @return {@link Duo} ({@code w_i}, PDF)
	 */
	public <T extends Interactable<T>> Duo<Vector3D, Double> sampleTransmissionW_i(Interaction<T> interaction,
			Sample sample);
	
	/**
	 * For the given {@link Interaction} and previously-selected
	 * transmission-direction {@code w_i}, compute the probability (in
	 * {@code [0,1]}) that this direction would have been selected out of the whole
	 * transmission-direction space.
	 * 
	 * @param interaction
	 * @param w_i
	 * @return PDF
	 */
	public <T extends Interactable<T>> double pdfTransmissionW_i(Interaction<T> interaction, Sample sample,
			Vector3D w_i);
	
	/**
	 * Indicates whether this Material should be queried for emission.
	 * 
	 * @return
	 */
	public boolean isEmissive();
	
	/**
	 * For the given {@link Interaction}, sample this Material's emission back along
	 * that to-eye vector, along with that sample's PDF.
	 * 
	 * @param point
	 * @param w_o
	 * @param sample
	 * @return
	 */
	public <T extends Interactable<T>> Duo<Double, Spectrum> sampleLe(Interaction<T> interaction, Sample sample);
	
}
