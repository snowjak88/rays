package org.snowjak.rays.spectrum.distribution;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.math3.util.Pair;
import org.junit.Test;
import org.snowjak.rays.geometry.util.Point;

public class TabulatedDistributionTest {
	
	@Test
	public void testZeroed() {
		
		final var t = new TabulatedDistributionImpl(0d, 2d, 3);
		
		assertEquals(3, t.getEntries().length);
		
		assertTrue(Arrays.stream(t.getEntries()).allMatch(p -> p.get(0) == 0d));
		
		assertTrue(t.isBounded());
		assertTrue(t.getBounds().isPresent());
		assertEquals(0d, t.getBounds().get().getFirst(), 0.00001d);
		assertEquals(2d, t.getBounds().get().getSecond(), 0.00001d);
	}
	
	@Test
	public void test() {
		
		final var t = new TabulatedDistributionImpl(0d, 2d, new Point[] { new Point(0), new Point(1), new Point(2) });
		
		assertEquals(3, t.getEntries().length);
		
		assertTrue(t.isBounded());
		assertTrue(t.getBounds().isPresent());
		assertEquals(0d, t.getBounds().get().getFirst(), 0.00001d);
		assertEquals(2d, t.getBounds().get().getSecond(), 0.00001d);
		
		assertEquals(0.0d, t.get(0.0d).get(0), 0.00001d);
		assertEquals(0.5d, t.get(0.5d).get(0), 0.00001d);
		assertEquals(1.0d, t.get(1.0d).get(0), 0.00001d);
		assertEquals(1.5d, t.get(1.5d).get(0), 0.00001d);
		assertEquals(2.0d, t.get(2.0d).get(0), 0.00001d);
		
		try {
			t.get(-1d);
			fail("Expected exception was not thrown.");
		} catch (RuntimeException e) {
			// do nothing
		}
		
		try {
			t.get(3d);
			fail("Expected exception was not thrown.");
		} catch (RuntimeException e) {
			// do nothing
		}
	}
	
	@Test
	public void testLoadFromCSV() {
		
		final var testInputStream = new ByteArrayInputStream(
				("0.0,1.0" + System.lineSeparator() + "1.0,2.0" + System.lineSeparator() + "2.0,0.0").getBytes());
		
		try {
			final var t = TabulatedDistribution.loadFromCSV(testInputStream,
					(bounds, values) -> new TabulatedDistributionImpl(bounds.getFirst(), bounds.getSecond(), values),
					SpectralPowerDistribution::parseCSVLine, (len) -> new Point[len]);
			
			assertEquals(3, t.getEntries().length);
			
			assertTrue(t.getBounds().isPresent());
			assertEquals(0d, t.getBounds().get().getFirst(), 0.00001);
			assertEquals(2d, t.getBounds().get().getSecond(), 0.00001);
			
			assertEquals(1d, t.get(0d).get(0), 0.00001);
			assertEquals(2d, t.get(1d).get(0), 0.00001);
			assertEquals(0d, t.get(2d).get(0), 0.00001);
			
		} catch (IOException e) {
			fail("Unexpected exception! " + e.getClass().getSimpleName() + ": \"" + e.getMessage() + "\"");
		}
	}
	
	@Test
	public void testSaveToCSV() {
		
		final var testOutputStream = new ByteArrayOutputStream();
		
		final var t = new TabulatedDistributionImpl(0.0, 2.0,
				new Point[] { new Point(1.0), new Point(2.0), new Point(0.0) });
		
		try {
			t.saveToCSV(testOutputStream, SpectralPowerDistribution::buildCSVLine);
			
			assertEquals("0.0,1.0" + System.lineSeparator() + "1.0,2.0" + System.lineSeparator() + "2.0,0.0"
					+ System.lineSeparator(), testOutputStream.toString());
		} catch (IOException e) {
			fail("Unexpected exception! " + e.getClass().getSimpleName() + ": \"" + e.getMessage() + "\"");
		}
		
	}
	
	public static class TabulatedDistributionImpl extends TabulatedDistribution<TabulatedDistributionImpl, Point> {
		
		public TabulatedDistributionImpl(double lowerBound, double upperBound, int entryCount) {
			
			super(lowerBound, upperBound, entryCount);
		}
		
		public TabulatedDistributionImpl(double lowerBound, double upperBound, Point[] values) {
			
			super(lowerBound, upperBound, values);
		}
		
		@Override
		protected Point getZero() {
			
			return new Point();
		}
		
		@Override
		protected Point[] getArray(int len) {
			
			return new Point[len];
		}
		
		@Override
		protected TabulatedDistributionImpl getNewInstance(Pair<Double, Double> bounds, Point[] values) {
			
			return new TabulatedDistributionImpl(bounds.getFirst(), bounds.getSecond(), values);
		}
		
	}
	
}
