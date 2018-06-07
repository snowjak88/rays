package org.snowjak.rays.spectrum.distribution;

import org.snowjak.rays.geometry.util.Triplet;

/**
 * Represents a distribution of CIE XYZ color-matching functions.
 * 
 * @author snowjak88
 *
 */
public interface ColorMappingFunctionDistribution extends Distribution<Triplet> {
	
	/**
	 * Get the color-matching function values (represented as a {@link Triplet})
	 * corresponding to the given wavelength (nm), or <code>null</code> if no such
	 * color-matching function values could be found.
	 * 
	 * @param wavelength
	 * @return
	 */
	public default Triplet getCMF(double wavelength) {
		
		return this.get(wavelength);
	}
	
	/**
	 * @return the lowest wavelength these color-matching functions can
	 *         realistically handle
	 */
	public default double getLowestWavelength() {
		
		return this.getLowKey();
	}
	
	@Override
	default Double getLowKey() {
		
		return 360.0d;
	}
	
	/**
	 * @return the highest wavelength these color-matching functions can
	 *         realistically handle
	 */
	public default double getHighestWavelength() {
		
		return this.getHighKey();
	}
	
	@Override
	default Double getHighKey() {
		
		return 830.0d;
	}
	
}
