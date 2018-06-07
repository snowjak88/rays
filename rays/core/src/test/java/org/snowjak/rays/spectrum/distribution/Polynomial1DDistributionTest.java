package org.snowjak.rays.spectrum.distribution;

import static org.junit.Assert.*;

import org.junit.Test;

public class Polynomial1DDistributionTest {
	
	@Test
	public void testGet() {
		
		final var p = new Polynomial1DDistribution(1d, 2d, 3d);
		
		assertEquals("p(0) is not as expected!", 1d, p.get(0d), 0.00001);
		assertEquals("p(1) is not as expected!", 6d, p.get(1d), 0.00001);
		assertEquals("p(2) is not as expected!", 17d, p.get(2d), 0.00001);
	}
	
}
