package org.snowjak.rays.spectrum;

import java.io.Serializable;

import org.snowjak.rays.spectrum.colorspace.RGB;

/**
 * Represents a measurement of radiant energy distributed across several
 * wavelengths.
 * 
 * @author snowjak88
 */
public interface Spectrum extends Serializable {
	
	/**
	 * @return <code>true</code> if this Spectrum has 0 (or even very close to 0)
	 *         energy associated with it.
	 */
	public boolean isBlack();
	
	/**
	 * Compute the result of adding this Spectrum's energy with another.
	 */
	public Spectrum add(Spectrum addend);
	
	/**
	 * Compute the result of multiplying this Spectrum's energy with another.
	 * (Usually used to model filtering or fractional selection of radiant energy.)
	 */
	public Spectrum multiply(Spectrum multiplicand);
	
	/**
	 * Compute the result of multiplying this Spectrum's energy by a scalar factor
	 * -- essentially scaling this Spectrum's energy linearly.
	 * 
	 * @param scalar
	 * @return
	 */
	public Spectrum multiply(double scalar);
	
	/**
	 * Convert this Spectrum to a RGB-trio for subsequent display.
	 * <p>
	 * <strong>Note</strong> that it is assumed that this Spectrum represents a
	 * non-emissive energy-distribution.
	 * </p>
	 */
	public default RGB toRGB() {
		
		return this.toRGB(false);
	}
	
	/**
	 * Convert this Spectrum to a RGB-trio for subsequent display. You must specify
	 * whether this Spectrum represents an emissive energy-distribution or not.
	 * 
	 * @param isEmissive
	 * @return
	 */
	public RGB toRGB(boolean isEmissive);
}
