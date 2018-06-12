package org.snowjak.rays.spectrum.distribution;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.apache.commons.math3.util.Pair;
import org.junit.Test;
import org.snowjak.rays.TriFunction;

public class TabulatedDistributionTest {
	
	public static class TabulatedDistributionImpl extends TabulatedDistribution<Double> {
		
		public TabulatedDistributionImpl() {
			
			super();
		}
		
		public TabulatedDistributionImpl(Map<Double, Double> table) {
			
			super(table);
		}
		
		@Override
		public TriFunction<Double, Double, Double, Double> getLinearInterpolationFunction() {
			
			return (start, end, fraction) -> (start * (1d - fraction)) + (end * fraction);
		}
		
		@Override
		public Pair<Double, Double> parseEntry(String csvLine) {
			
			return null;
		}
		
		@Override
		public String writeEntry(Double key, Double entry) {
			
			return null;
		}
		
		@Override
		public Double averageOver(Double intervalStart, Double intervalEnd) {
			
			final double start = (intervalEnd > intervalStart) ? intervalStart : intervalEnd;
			final double end = (intervalEnd > intervalStart) ? intervalEnd : intervalStart;
			
			return getTable().entrySet().stream().filter(e -> e.getKey() >= start && e.getKey() <= end).reduce(0d,
					(d, e) -> d + e.getValue(), (d1, d2) -> d1 + d2) / (end - start);
		}
		
	}
	
	@Test
	public void testGet() {
		
		final var d = new TabulatedDistributionImpl();
		
		d.addEntry(0d, 1d);
		d.addEntry(1d, 2d);
		d.addEntry(2d, 1.5d);
		
		assertEquals(1d, d.get(0d), 0.00001);
		assertEquals(1.5d, d.get(0.5d), 0.00001);
		assertEquals(2d, d.get(1d), 0.00001);
		assertEquals(1.75d, d.get(1.5d), 0.00001);
		assertEquals(1.5d, d.get(2d), 0.00001);
	}
	
	@Test
	public void testGetLowKey() {
		
		final var d = new TabulatedDistributionImpl();
		
		d.addEntry(0d, 1d);
		d.addEntry(1d, 2d);
		d.addEntry(2d, 1.5d);
		
		assertEquals(0d, d.getLowKey(), 0.00001);
	}
	
	@Test
	public void testGetHighKey() {
		
		final var d = new TabulatedDistributionImpl();
		
		d.addEntry(0d, 1d);
		d.addEntry(1d, 2d);
		d.addEntry(2d, 1.5d);
		
		assertEquals(2d, d.getHighKey(), 0.00001);
	}
	
	@Test
	public void testNormalize() {
		
		final var d = new TabulatedDistributionImpl();
		
		d.addEntry(0d, 1d);
		d.addEntry(1d, 2d);
		d.addEntry(2d, 1.5d);
		
		final var n = d.normalize((map) -> new TabulatedDistributionImpl(map), (v) -> v, (v, f) -> v * f);
		
		assertEquals(3, n.getAll().size());
		assertEquals(0.5d, n.get(0d), 0.00001);
		assertEquals(1.0d, n.get(1d), 0.00001);
		assertEquals(0.75d, n.get(2d), 0.00001);
	}
	
}
