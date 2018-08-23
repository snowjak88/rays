package org.snowjak.rays.bxdf;

import static org.apache.commons.math3.util.FastMath.*;
import org.snowjak.rays.geometry.Normal3D;
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
	
	/**
	 * Represents Fresnel terms for reflectance / transmission
	 * 
	 * @author snowjak88
	 *
	 */
	public static class Fresnel {
		
		private Vector3D w_e;
		private Vector3D n;
		private double n1, n2;
		
		private transient Vector3D reflection = null, transmission = null;
		private transient double reflectance = -1;
		private transient boolean isTIR = false;
		
		/**
		 * Construct a new Fresnel instance.
		 * 
		 * @param incident
		 *            direction of incident energy (pointing <em>toward</em> the
		 *            point-of-interaction
		 * @param normal
		 *            surface-normal at the point-of-interaction
		 * @param n1
		 *            index-of-refraction of the medium the incident-energy is coming
		 *            <em>from</em>
		 * @param n2
		 *            index-of-refraction of the medium the incident-energy is coming
		 *            <em>into</em>
		 */
		public Fresnel(Vector3D incident, Normal3D normal, double n1, double n2) {
			
			this.w_e = incident.negate().normalize();
			this.n = Vector3D.from(normal.normalize());
			this.n1 = n1;
			this.n2 = n2;
		}
		
		/**
		 * @return the reflection-direction away from the point-of-interaction
		 */
		public Vector3D getReflection() {
			
			if (reflection != null)
				return reflection;
			
			final var i = w_e;
			
			final var cos_i = n.dotProduct(i);
			reflection = i.negate().add(n.multiply(2d * cos_i)).normalize();
			
			return reflection;
		}
		
		/**
		 * @return the transmission-direction away from the point-of-interaction, or
		 *         <code>null</code> if no transmission occurs (because
		 *         Total-Internal-Reflection)
		 */
		public Vector3D getTransmission() {
			
			if (transmission != null || isTIR)
				return transmission;
			
			final var i = w_e;
			final var n0 = n1 / n2;
			final var cos_i = n.dotProduct(i);
			final var sin2_t = n0 * n0 * (1d - cos_i * cos_i);
			
			if (sin2_t > 1d) {
				//
				// This is a case of Total Internal Reflection -- no transmission occurs, and we
				// have perfect reflection!
				isTIR = true;
				transmission = null;
				reflectance = 1d;
				
				return transmission;
			}
			
			final var cos_t = sqrt(1d - sin2_t);
			transmission = i.negate().multiply(n0).add(n.multiply(n0 * cos_i - cos_t)).normalize();
			
			return transmission;
		}
		
		/**
		 * @return the reflectance at the point-of-interaction, expressed as a fraction
		 *         in {@code [0,1]}
		 */
		public double getReflectance() {
			
			if (reflectance >= 0d)
				return reflectance;
			
			final var i = w_e;
			final var n0 = n1 / n2;
			final var cos_i = n.dotProduct(i);
			final var sin2_t = n0 * n0 * (1d - cos_i * cos_i);
			
			if (sin2_t > 1d) {
				//
				// This is a case of Total Internal Reflection -- no transmission occurs, and we
				// have perfect reflection!
				isTIR = true;
				reflectance = 1d;
				return reflectance;
			}
			
			final var cos_t = sqrt(1d - sin2_t);
			final double r0_rth = (n1 * cos_i - n2 * cos_t) / (n1 * cos_i + n2 * cos_t);
			final double r_par = (n2 * cos_i - n1 * cos_t) / (n2 * cos_i + n1 * cos_t);
			reflectance = min(max(((r0_rth * r0_rth + r_par * r_par) / 2d), 0d), 1d);
			
			return reflectance;
		}
		
		/**
		 * @return the transmittance at the point-of-interaction, expressed as a
		 *         fraction in {@code [0,1]}
		 */
		public double getTransmittance() {
			
			return 1d - getReflectance();
		}
		
		public boolean isTotalInternalReflection() {
			
			if (isTIR != false)
				return isTIR;
			
			final var i = w_e;
			
			final double n0 = n1 / n2;
			final double cos_i = n.dotProduct(i);
			final double sin2_t = n0 * n0 * (1d - cos_i * cos_i);
			
			if (sin2_t > 1d) {
				//
				// This is a case of Total Internal Reflection -- no transmission occurs, and we
				// have perfect reflection!
				isTIR = true;
				reflectance = 1d;
				transmission = null;
				return isTIR;
			}
			
			return isTIR;
		}
	}
	
}
