package org.snowjak.rays.spectrum.distribution;

/**
 * Represents a spectral power-distribution -- an association between wavelength
 * (nm) and power (W * sr^-1 * m^-2).
 * 
 * @author snowjak88
 *
 */
public interface SpectralPowerDistribution extends Distribution<Double> {
	
	/**
	 * Get the spectral radiance (W/sr*m^2) associated with the given wavelength
	 * (nm).
	 * 
	 * @param wavelength
	 * @return the associated power-level, or 0 if no matching power-level found
	 */
	public default Double getPower(Double wavelength) {
		
		return this.get(wavelength);
	}
	
	/**
	 * @return the smallest wavelength (nm) held in this distribution
	 */
	public default double getLowestWavelength() {
		
		return this.getLowKey();
	}
	
	/**
	 * @return the largest wavelength (nm) held in this distribution
	 */
	public default double getHighestWavelength() {
		
		return this.getHighKey();
	}
}
