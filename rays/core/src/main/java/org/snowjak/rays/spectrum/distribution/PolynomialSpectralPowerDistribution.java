package org.snowjak.rays.spectrum.distribution;

import java.util.List;

/**
 * Represents a {@link SpectralPowerDistribution} backed by a
 * {@link PolynomialDistribution}.
 * <p>
 * <strong>Note:</strong> when computing spectral-power, the given wavelength is
 * converted to a decimal fraction on the interval [360nm, 830nm].
 * </p>
 * 
 * @author snowjak88
 *
 */
public class PolynomialSpectralPowerDistribution extends PolynomialDistribution implements SpectralPowerDistribution {
	
	/**
	 * {@inheritDoc}
	 */
	public PolynomialSpectralPowerDistribution(double... coefficients) {
		
		super(coefficients);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public PolynomialSpectralPowerDistribution(Double... coefficients) {
		
		super(coefficients);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public PolynomialSpectralPowerDistribution(List<Double> coefficients) {
		
		super(coefficients);
	}
	
	@Override
	public Double getLowKey() {
		
		return 360d;
	}
	
	@Override
	public Double getHighKey() {
		
		return 830d;
	}
	
	@Override
	public Double get(Double key) {
		
		return super.get(fractionizeWavelength(key));
	}
	
	private double fractionizeWavelength(double lambda) {
		
		return (lambda - getLowestWavelength()) / (getHighestWavelength() / getLowestWavelength());
	}
	
}
