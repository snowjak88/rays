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
	
	@Test
	public void testToTable() {
		
		final var p = new Polynomial1DDistribution(1d, 2d, 3d);
		final var t = p.toTable(0.0, 2.0, 1.0);
		
		assertNotNull(t);
		assertEquals(3, t.getAll().size());
		assertEquals(1.d, t.get(0.0), 0.00001);
		assertEquals(6.d, t.get(1.0), 0.00001);
		assertEquals(17.d, t.get(2.0), 0.00001);
		
	}
}
