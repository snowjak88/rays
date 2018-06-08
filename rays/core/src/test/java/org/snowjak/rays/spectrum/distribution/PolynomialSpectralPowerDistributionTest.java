package org.snowjak.rays.spectrum.distribution;

import static org.apache.commons.math3.util.FastMath.*;
import static org.junit.Assert.*;

import org.junit.Test;

public class PolynomialSpectralPowerDistributionTest {
	
	private final SpectralPowerDistribution d = new PolynomialSpectralPowerDistribution(new double[] { 1d, 2d, 3d });
	
	@Test
	public void testGet_550nm() {
		
		final double lambda = 550d;
		final double fractionalLambda = (lambda - d.getLowestWavelength())
				/ (d.getHighestWavelength() / d.getLowestWavelength());
		final double expectedValue = 1d + 2d * pow(fractionalLambda, 1) + 3d * pow(fractionalLambda, 2);
		
		assertEquals(expectedValue, d.get(lambda), 0.00001);
		
	}
	
	@Test
	public void testGet_750nm() {
		
		final double lambda = 750d;
		final double fractionalLambda = (lambda - d.getLowestWavelength())
				/ (d.getHighestWavelength() / d.getLowestWavelength());
		final double expectedValue = 1d + 2d * pow(fractionalLambda, 1) + 3d * pow(fractionalLambda, 2);
		
		assertEquals(expectedValue, d.get(lambda), 0.00001);
		
	}
	
}
