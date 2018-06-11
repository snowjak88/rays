package org.snowjak.rays.spectrum;

import java.io.Serializable;

/**
 * Represents a measurement of radiant energy distributed across several
 * wavelengths.
 * 
 * @author snowjak88
 */
public interface Spectrum extends Serializable {

	/**
	 * @return <code>true</code> if this Spectrum has 0 (or even very close to
	 *         0) energy associated with it.
	 */
	public boolean isBlack();

	/**
	 * Compute the result of adding this Spectrum's energy with another.
	 */
	public Spectrum add(Spectrum addend);

	/**
	 * Compute the result of multiplying this Spectrum's energy with another.
	 * (Usually used to model filtering or fractional selection of radiant
	 * energy.)
	 */
	public Spectrum multiply(Spectrum multiplicand);

	/**
	 * Compute the result of multiplying this Spectrum's energy by a scalar
	 * factor -- essentially scaling this Spectrum's energy linearly.
	 * 
	 * @param scalar
	 * @return
	 */
	public Spectrum multiply(double scalar);

	/**
	 * Compute this Spectrum's amplitude -- a measure of its average intensity
	 * over time, across all wavelengths.
	 * 
	 * @return
	 */
	public double getAmplitude();

	/**
	 * Convert this Spectrum to a RGBColorspace-trio for subsequent display.
	 */
	public RGB toRGB();
}
