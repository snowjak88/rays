package org.snowjak.rays.geometry.util;

import static org.junit.Assert.assertEquals;

import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import org.junit.Test;
import org.snowjak.rays.geometry.util.NVector;

public class NVectorTest {
	
	public static class NVectorImpl extends NVector<NVectorImpl> {
		
		private static final long serialVersionUID = -2601655708396984774L;
		
		public NVectorImpl(double... values) {
			
			super(values);
		}
		
		@Override
		public NVectorImpl apply(UnaryOperator<Double> operator) {
			
			return new NVectorImpl(NVector.apply(getAll(), operator));
		}
		
		@Override
		public NVectorImpl apply(NVectorImpl other, BinaryOperator<Double> operator) {
			
			return new NVectorImpl(NVector.apply(getAll(), other.getAll(), operator));
		}
		
	}
	
	@Test
	public void testGetN() {
		
		final NVectorImpl v = new NVectorImpl(1, 2, 3);
		
		assertEquals(3, v.getN());
	}
	
	@Test
	public void testGet() {
		
		final NVectorImpl v = new NVectorImpl(1, 2, 3);
		
		assertEquals("v.get(0) not as expected!", 1d, v.get(0), 0.00001);
		assertEquals("v.get(1) not as expected!", 2d, v.get(1), 0.00001);
		assertEquals("v.get(2) not as expected!", 3d, v.get(2), 0.00001);
	}
	
	@Test
	public void testApplyUnaryOperatorOfDouble() {
		
		final NVectorImpl v = new NVectorImpl(1, 2, 3);
		
		final NVectorImpl u = v.apply(d -> d + 2);
		assertEquals("u.get(0) not as expected!", 3d, u.get(0), 0.00001);
		assertEquals("u.get(1) not as expected!", 4d, u.get(1), 0.00001);
		assertEquals("u.get(2) not as expected!", 5d, u.get(2), 0.00001);
	}
	
	@Test
	public void testApplyNVectorBinaryOperatorOfDouble() {
		
		final NVectorImpl v = new NVectorImpl(1, 2, 3);
		final NVectorImpl u = new NVectorImpl(2, 4, 6);
		
		final NVectorImpl w = v.apply(u, (d1, d2) -> (d1 > d2) ? d1 : d2);
		assertEquals("u.get(0) not as expected!", 2d, w.get(0), 0.00001);
		assertEquals("u.get(1) not as expected!", 4d, w.get(1), 0.00001);
		assertEquals("u.get(2) not as expected!", 6d, w.get(2), 0.00001);
	}
	
	@Test
	public void testNegate() {
		
		final NVectorImpl v = new NVectorImpl(1, 2, 3);
		
		final NVectorImpl u = v.negate();
		assertEquals("u.get(0) not as expected!", -1d, u.get(0), 0.00001);
		assertEquals("u.get(1) not as expected!", -2d, u.get(1), 0.00001);
		assertEquals("u.get(2) not as expected!", -3d, u.get(2), 0.00001);
	}
	
	@Test
	public void testReciprocal() {
		
		final NVectorImpl v = new NVectorImpl(1, 2, 3);
		
		final NVectorImpl u = v.reciprocal();
		assertEquals("u.get(0) not as expected!", 1d / 1d, u.get(0), 0.00001);
		assertEquals("u.get(1) not as expected!", 1d / 2d, u.get(1), 0.00001);
		assertEquals("u.get(2) not as expected!", 1d / 3d, u.get(2), 0.00001);
	}
	
	@Test
	public void testAddNVector() {
		
		final NVectorImpl v = new NVectorImpl(1, 2, 3);
		final NVectorImpl u = new NVectorImpl(2, 4, 6);
		
		final NVectorImpl w = v.add(u);
		assertEquals("u.get(0) not as expected!", 3d, w.get(0), 0.00001);
		assertEquals("u.get(1) not as expected!", 6d, w.get(1), 0.00001);
		assertEquals("u.get(2) not as expected!", 9d, w.get(2), 0.00001);
	}
	
	@Test
	public void testAddDouble() {
		
		final NVectorImpl v = new NVectorImpl(1, 2, 3);
		
		final NVectorImpl u = v.add(-2);
		assertEquals("u.get(0) not as expected!", -1d, u.get(0), 0.00001);
		assertEquals("u.get(1) not as expected!", 0d, u.get(1), 0.00001);
		assertEquals("u.get(2) not as expected!", 1d, u.get(2), 0.00001);
	}
	
	@Test
	public void testSubtractNVector() {
		
		final NVectorImpl v = new NVectorImpl(1, 2, 3);
		final NVectorImpl u = new NVectorImpl(2, 4, 6);
		
		final NVectorImpl w = v.subtract(u);
		assertEquals("w.get(0) not as expected!", -1d, w.get(0), 0.00001);
		assertEquals("w.get(1) not as expected!", -2d, w.get(1), 0.00001);
		assertEquals("w.get(2) not as expected!", -3d, w.get(2), 0.00001);
	}
	
	@Test
	public void testSubtractDouble() {
		
		final NVectorImpl v = new NVectorImpl(1, 2, 3);
		
		final NVectorImpl u = v.subtract(-2);
		assertEquals("u.get(0) not as expected!", 3d, u.get(0), 0.00001);
		assertEquals("u.get(1) not as expected!", 4d, u.get(1), 0.00001);
		assertEquals("u.get(2) not as expected!", 5d, u.get(2), 0.00001);
	}
	
	@Test
	public void testMultiplyNVector() {
		
		final NVectorImpl v = new NVectorImpl(1, 2, 3);
		final NVectorImpl u = new NVectorImpl(2, 4, 6);
		
		final NVectorImpl w = v.multiply(u);
		assertEquals("u.get(0) not as expected!", 2d, w.get(0), 0.00001);
		assertEquals("u.get(1) not as expected!", 8d, w.get(1), 0.00001);
		assertEquals("u.get(2) not as expected!", 18d, w.get(2), 0.00001);
	}
	
	@Test
	public void testMultiplyDouble() {
		
		final NVectorImpl v = new NVectorImpl(1, 2, 3);
		
		final NVectorImpl u = v.multiply(-2);
		assertEquals("u.get(0) not as expected!", -2d, u.get(0), 0.00001);
		assertEquals("u.get(1) not as expected!", -4d, u.get(1), 0.00001);
		assertEquals("u.get(2) not as expected!", -6d, u.get(2), 0.00001);
	}
	
	@Test
	public void testDivideNVector() {
		
		final NVectorImpl v = new NVectorImpl(1, 2, 3);
		final NVectorImpl u = new NVectorImpl(2, 4, 6);
		
		final NVectorImpl w = v.divide(u);
		assertEquals("u.get(0) not as expected!", 1d / 2d, w.get(0), 0.00001);
		assertEquals("u.get(1) not as expected!", 2d / 4d, w.get(1), 0.00001);
		assertEquals("u.get(2) not as expected!", 3d / 6d, w.get(2), 0.00001);
	}
	
	@Test
	public void testDivideDouble() {
		
		final NVectorImpl v = new NVectorImpl(1, 2, 3);
		
		final NVectorImpl u = v.divide(-2);
		assertEquals("u.get(0) not as expected!", 1d / -2d, u.get(0), 0.00001);
		assertEquals("u.get(1) not as expected!", 2d / -2d, u.get(1), 0.00001);
		assertEquals("u.get(2) not as expected!", 3d / -2d, u.get(2), 0.00001);
	}
	
}
