package org.snowjak.rays.spectrum;

import java.io.Serializable;

import org.snowjak.rays.spectrum.colorspace.RGB;
import org.snowjak.rays.spectrum.colorspace.XYZ;

/**
 * Represents a measurement of radiant energy distributed across several
 * wavelengths.
 * <p>
 * <strong>Units:</strong> -- {@code W}
 * </p>
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
	 * Get this Spectrum's power ({@code W / m^2 nm}) at the given
	 * wavelength ({@code nm}).
	 * 
	 * @param lambda
	 * @return
	 */
	public double getPower(double lambda);
	
	/**
	 * Rescale this Spectrum's total power ({@code W}) to the given power-level.
	 * 
	 * @param power
	 * @return
	 */
	public default Spectrum rescale(double power) {
		
		final var thisPower = getTotalPower();
		if (thisPower == 0d)
			return this;
		
		final var multiplier = power / thisPower;
		
		return multiply(multiplier);
	}
	
	/**
	 * Get this Spectrum's total power ({@code W}).
	 * <p>
	 * By default, this simply calls {@link #integrate()}.
	 * </p>
	 * 
	 * @return
	 * @see #integrate()
	 */
	public default double getTotalPower() {
		
		return integrate();
	}
	
	/**
	 * Divide this Spectrum by its integral -- effectively converting it to a
	 * unitless value (equivalent to an RGB triplet functioning as a fraction).
	 * 
	 * @return
	 */
	public Spectrum normalizePower();
	
	/**
	 * Compute this Spectrum's total power -- i.e., computing:
	 * 
	 * <pre>
	 * power (W) = integral( f(l) dl )
	 * </pre>
	 * 
	 * where {@code f(l)} = {@link #getPower(double)}
	 * 
	 * @return
	 */
	public double integrate();
	
	/**
	 * Convert this Spectrum to a RGB-trio for subsequent display.
	 * <p>
	 * <strong>Note</strong> that it is assumed that this Spectrum represents a
	 * relative energy-distribution. (see {@link #toRGB(boolean)})
	 * </p>
	 */
	public default RGB toRGB() {
		
		return this.toRGB(true);
	}
	
	/**
	 * Convert this Spectrum to a RGB-trio for subsequent display. You must specify
	 * whether this Spectrum should be rescaled or not -- i.e., so that it expresses
	 * <em>relative</em> rather than <em>absolute</em> luminance
	 * {@link XYZ#fromSpectrum(Spectrum, boolean)}
	 * 
	 * @param isRelative
	 * @return
	 */
	public RGB toRGB(boolean isRelative);
}
