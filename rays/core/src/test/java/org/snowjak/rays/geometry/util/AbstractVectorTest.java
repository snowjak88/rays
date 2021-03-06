package org.snowjak.rays.geometry.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import org.apache.commons.math3.util.FastMath;
import org.junit.Test;

public class AbstractVectorTest {
	
	public static class AbstractVectorImpl extends AbstractVector<AbstractVectorImpl> {
		
		private static final long serialVersionUID = -2601655708396984774L;
		
		public AbstractVectorImpl(double... values) {
			
			super(values);
		}
		
		@Override
		public AbstractVectorImpl apply(UnaryOperator<Double> operator) {
			
			return new AbstractVectorImpl(AbstractVector.apply(getAll(), operator));
		}
		
		@Override
		public AbstractVectorImpl apply(AbstractVectorImpl other, BinaryOperator<Double> operator) {
			
			return new AbstractVectorImpl(AbstractVector.apply(getAll(), other.getAll(), operator));
		}
		
	}
	
	@Test
	public void testGetN() {
		
		final AbstractVectorImpl v = new AbstractVectorImpl(1, 2, 3);
		
		assertEquals(3, v.getN());
	}
	
	@Test
	public void testGet() {
		
		final AbstractVectorImpl v = new AbstractVectorImpl(1, 2, 3);
		
		assertEquals("v.get(0) not as expected!", 1d, v.get(0), 0.00001);
		assertEquals("v.get(1) not as expected!", 2d, v.get(1), 0.00001);
		assertEquals("v.get(2) not as expected!", 3d, v.get(2), 0.00001);
	}
	
	@Test
	public void testApplyUnaryOperatorOfDouble() {
		
		final AbstractVectorImpl v = new AbstractVectorImpl(1, 2, 3);
		
		final AbstractVectorImpl u = v.apply(d -> d + 2);
		assertEquals("u.get(0) not as expected!", 3d, u.get(0), 0.00001);
		assertEquals("u.get(1) not as expected!", 4d, u.get(1), 0.00001);
		assertEquals("u.get(2) not as expected!", 5d, u.get(2), 0.00001);
	}
	
	@Test
	public void testApplyNVectorBinaryOperatorOfDouble() {
		
		final AbstractVectorImpl v = new AbstractVectorImpl(1, 2, 3);
		final AbstractVectorImpl u = new AbstractVectorImpl(2, 4, 6);
		
		final AbstractVectorImpl w = v.apply(u, (d1, d2) -> (d1 > d2) ? d1 : d2);
		assertEquals("u.get(0) not as expected!", 2d, w.get(0), 0.00001);
		assertEquals("u.get(1) not as expected!", 4d, w.get(1), 0.00001);
		assertEquals("u.get(2) not as expected!", 6d, w.get(2), 0.00001);
	}
	
	@Test
	public void testReduce() {
		
		final AbstractVectorImpl v = new AbstractVectorImpl(1, 2, 3);
		
		final double u = FastMath
				.sqrt(v.reduce(ds -> Arrays.stream(ds).map(d -> d * d).reduce(0d, (d1, d2) -> d1 + d2)));
		assertEquals("v.reduce not as expected!", 3.74165d, u, 0.00001);
	}
	
	@Test
	public void testNegate() {
		
		final AbstractVectorImpl v = new AbstractVectorImpl(1, 2, 3);
		
		final AbstractVectorImpl u = v.negate();
		assertEquals("u.get(0) not as expected!", -1d, u.get(0), 0.00001);
		assertEquals("u.get(1) not as expected!", -2d, u.get(1), 0.00001);
		assertEquals("u.get(2) not as expected!", -3d, u.get(2), 0.00001);
	}
	
	@Test
	public void testReciprocal() {
		
		final AbstractVectorImpl v = new AbstractVectorImpl(1, 2, 3);
		
		final AbstractVectorImpl u = v.reciprocal();
		assertEquals("u.get(0) not as expected!", 1d / 1d, u.get(0), 0.00001);
		assertEquals("u.get(1) not as expected!", 1d / 2d, u.get(1), 0.00001);
		assertEquals("u.get(2) not as expected!", 1d / 3d, u.get(2), 0.00001);
	}
	
	@Test
	public void testNormalize() {
		
		final AbstractVectorImpl v = new AbstractVectorImpl(1, 2, 3);
		
		final AbstractVectorImpl u = v.normalize(vect -> Arrays.stream(vect.getAll()).reduce(0d, FastMath::max));
		assertEquals("u.get(0) not as expected!", 1d / 3d, u.get(0), 0.00001);
		assertEquals("u.get(1) not as expected!", 2d / 3d, u.get(1), 0.00001);
		assertEquals("u.get(2) not as expected!", 3d / 3d, u.get(2), 0.00001);
	}
	
	@Test
	public void testClamp() {
		
		final AbstractVectorImpl v = new AbstractVectorImpl(-1, 0, 1, 2, 3);
		
		final AbstractVectorImpl u = v.clamp(0, 2);
		assertEquals("u.get(0) not as expected!", 0d, u.get(0), 0.00001);
		assertEquals("u.get(1) not as expected!", 0d, u.get(1), 0.00001);
		assertEquals("u.get(2) not as expected!", 1d, u.get(2), 0.00001);
		assertEquals("u.get(3) not as expected!", 2d, u.get(3), 0.00001);
		assertEquals("u.get(4) not as expected!", 2d, u.get(4), 0.00001);
	}
	
	@Test
	public void testLinearInterpolateTo() {
		
		final AbstractVectorImpl u = new AbstractVectorImpl(1, 2, 3);
		final AbstractVectorImpl v = new AbstractVectorImpl(2, 2, 2);
		
		final AbstractVectorImpl w = u.linearInterpolateTo(v, 0.5);
		assertEquals("w.get(0) not as expected!", 1.5d, w.get(0), 0.00001);
		assertEquals("w.get(1) not as expected!", 2.0d, w.get(1), 0.00001);
		assertEquals("w.get(2) not as expected!", 2.5d, w.get(2), 0.00001);
	}
	
	@Test
	public void testAddNVector() {
		
		final AbstractVectorImpl v = new AbstractVectorImpl(1, 2, 3);
		final AbstractVectorImpl u = new AbstractVectorImpl(2, 4, 6);
		
		final AbstractVectorImpl w = v.add(u);
		assertEquals("u.get(0) not as expected!", 3d, w.get(0), 0.00001);
		assertEquals("u.get(1) not as expected!", 6d, w.get(1), 0.00001);
		assertEquals("u.get(2) not as expected!", 9d, w.get(2), 0.00001);
	}
	
	@Test
	public void testAddDouble() {
		
		final AbstractVectorImpl v = new AbstractVectorImpl(1, 2, 3);
		
		final AbstractVectorImpl u = v.add(-2);
		assertEquals("u.get(0) not as expected!", -1d, u.get(0), 0.00001);
		assertEquals("u.get(1) not as expected!", 0d, u.get(1), 0.00001);
		assertEquals("u.get(2) not as expected!", 1d, u.get(2), 0.00001);
	}
	
	@Test
	public void testSubtractNVector() {
		
		final AbstractVectorImpl v = new AbstractVectorImpl(1, 2, 3);
		final AbstractVectorImpl u = new AbstractVectorImpl(2, 4, 6);
		
		final AbstractVectorImpl w = v.subtract(u);
		assertEquals("w.get(0) not as expected!", -1d, w.get(0), 0.00001);
		assertEquals("w.get(1) not as expected!", -2d, w.get(1), 0.00001);
		assertEquals("w.get(2) not as expected!", -3d, w.get(2), 0.00001);
	}
	
	@Test
	public void testSubtractDouble() {
		
		final AbstractVectorImpl v = new AbstractVectorImpl(1, 2, 3);
		
		final AbstractVectorImpl u = v.subtract(-2);
		assertEquals("u.get(0) not as expected!", 3d, u.get(0), 0.00001);
		assertEquals("u.get(1) not as expected!", 4d, u.get(1), 0.00001);
		assertEquals("u.get(2) not as expected!", 5d, u.get(2), 0.00001);
	}
	
	@Test
	public void testMultiplyNVector() {
		
		final AbstractVectorImpl v = new AbstractVectorImpl(1, 2, 3);
		final AbstractVectorImpl u = new AbstractVectorImpl(2, 4, 6);
		
		final AbstractVectorImpl w = v.multiply(u);
		assertEquals("u.get(0) not as expected!", 2d, w.get(0), 0.00001);
		assertEquals("u.get(1) not as expected!", 8d, w.get(1), 0.00001);
		assertEquals("u.get(2) not as expected!", 18d, w.get(2), 0.00001);
	}
	
	@Test
	public void testMultiplyDouble() {
		
		final AbstractVectorImpl v = new AbstractVectorImpl(1, 2, 3);
		
		final AbstractVectorImpl u = v.multiply(-2);
		assertEquals("u.get(0) not as expected!", -2d, u.get(0), 0.00001);
		assertEquals("u.get(1) not as expected!", -4d, u.get(1), 0.00001);
		assertEquals("u.get(2) not as expected!", -6d, u.get(2), 0.00001);
	}
	
	@Test
	public void testDivideNVector() {
		
		final AbstractVectorImpl v = new AbstractVectorImpl(1, 2, 3);
		final AbstractVectorImpl u = new AbstractVectorImpl(2, 4, 6);
		
		final AbstractVectorImpl w = v.divide(u);
		assertEquals("u.get(0) not as expected!", 1d / 2d, w.get(0), 0.00001);
		assertEquals("u.get(1) not as expected!", 2d / 4d, w.get(1), 0.00001);
		assertEquals("u.get(2) not as expected!", 3d / 6d, w.get(2), 0.00001);
	}
	
	@Test
	public void testDivideDouble() {
		
		final AbstractVectorImpl v = new AbstractVectorImpl(1, 2, 3);
		
		final AbstractVectorImpl u = v.divide(-2);
		assertEquals("u.get(0) not as expected!", 1d / -2d, u.get(0), 0.00001);
		assertEquals("u.get(1) not as expected!", 2d / -2d, u.get(1), 0.00001);
		assertEquals("u.get(2) not as expected!", 3d / -2d, u.get(2), 0.00001);
	}
	
	@Test
	public void testNearlyEqual() {
		
		final AbstractVectorImpl v = new AbstractVectorImpl(1, 2, 3), u1 = new AbstractVectorImpl(1, 2, 3),
				u2 = new AbstractVectorImpl(0, 2, 3);
		
		assertTrue("u1 should be nearly equals v!", v.nearlyEquals(u1));
		assertFalse("u2 should not be nearly equals v!", v.nearlyEquals(u2));
	}
	
	@Test
	public void testSummarize() {
		
		final AbstractVectorImpl v = new AbstractVectorImpl(1, 2, 3);
		
		assertEquals("v.summarize() is not as expected!", 6d, v.summarize(), 0.00001);
		
	}
	
}
