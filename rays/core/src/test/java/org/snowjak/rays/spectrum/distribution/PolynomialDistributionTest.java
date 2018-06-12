package org.snowjak.rays.spectrum.distribution;

import static org.junit.Assert.*;

import org.apache.commons.math3.util.Pair;
import org.junit.Test;
import org.snowjak.rays.TriFunction;

public class PolynomialDistributionTest {
	
	@Test
	public void testGet() {
		
		final var p = new PolynomialDistribution(new double[] { 1d, 2d, 3d });
		
		assertEquals("p(0) is not as expected!", 1d, p.get(0d), 0.00001);
		assertEquals("p(1) is not as expected!", 6d, p.get(1d), 0.00001);
		assertEquals("p(2) is not as expected!", 17d, p.get(2d), 0.00001);
	}
	
	@Test
	public void testToTabulatedForm() {
		
		final var p = new PolynomialDistribution(new double[] { 1d, 2d, 3d });
		
		final var t = p.toTabulatedForm(TableForm::new, 0d, 2d, 3);
		
		assertEquals(3, t.getAll().size());
		
		assertNotNull(t.get(0d));
		assertEquals(1d, t.get(0d), 0.00001);
		
		assertNotNull(t.get(1d));
		assertEquals(6d, t.get(1d), 0.00001);
		
		assertNotNull(t.get(2d));
		assertEquals(17d, t.get(2d), 0.00001);
	}
	
	public static class TableForm extends TabulatedDistribution<Double> {
		
		@Override
		public Double averageOver(Double intervalStart, Double intervalEnd) {
			
			final double start = (intervalEnd > intervalStart) ? intervalStart : intervalEnd;
			final double end = (intervalEnd > intervalStart) ? intervalEnd : intervalStart;
			return getTable().entrySet().stream().filter(e -> e.getKey() >= start && e.getKey() <= end).reduce(0d,
					(d, e) -> d + e.getValue(), (d1, d2) -> d1 + d2) / (end - start);
		}
		
		@Override
		public TriFunction<Double, Double, Double, Double> getLinearInterpolationFunction() {
			
			return (s, e, f) -> (1d - f) * s + (f) * e;
		}
		
		@Override
		public Pair<Double, Double> parseEntry(String csvLine) {
			
			return null;
		}
		
		@Override
		public String writeEntry(Double key, Double entry) {
			
			return null;
		}
		
	}
}
