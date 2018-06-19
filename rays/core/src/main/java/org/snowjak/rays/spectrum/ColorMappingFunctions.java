package org.snowjak.rays.spectrum;

import org.snowjak.rays.geometry.util.Triplet;

public interface ColorMappingFunctions {
	
	/**
	 * Get a {@link Triplet} representing the CIE XYZ color-mapping-functions
	 * evaulated for the given wavelength (nm).
	 * 
	 * @param wavelength
	 * @return
	 */
	public Triplet get(double wavelength);
	
}
