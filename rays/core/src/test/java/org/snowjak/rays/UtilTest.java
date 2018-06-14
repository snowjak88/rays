package org.snowjak.rays;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.apache.commons.math3.util.FastMath;
import org.junit.Test;
import org.snowjak.rays.geometry.util.Triplet;

public class UtilTest {
	
	@Test
	public void test_integrateSine() {
		
		assertEquals(1d, Util.integrate(0d, FastMath.PI / 2d, 1024, (x) -> FastMath.sin(x)), 0.00001);
	}
	
	@Test
	public void test_integrateLinear() {
		
		assertEquals(0.5d, Util.integrate(0d, 1d, 1024, (x) -> x), 0.00001);
	}
	
	@Test
	public void test_integrateTripletSine() {
		
		final var t = Util.integrateTriplet(0d, FastMath.PI / 2d, 1024,
				(x) -> new Triplet(FastMath.sin(x), FastMath.cos(x), FastMath.sin(x)));
		assertEquals(1d, t.get(0), 0.00001);
		assertEquals(1d, t.get(1), 0.00001);
		assertEquals(1d, t.get(2), 0.00001);
	}
	
	@Test
	public void test_integrateTripletLinear() {
		
		final var t = Util.integrateTriplet(0d, 1d, 1024, (x) -> new Triplet(x, -x, x));
		assertEquals(0.5d, t.get(0), 0.00001);
		assertEquals(-0.5d, t.get(1), 0.00001);
		assertEquals(0.5d, t.get(2), 0.00001);
	}
	
	@Test
	public void test_shuffle() {
		
		final var array = new Integer[] { 1, 2, 3, 4 };
		final var originalArray = Arrays.copyOf(array, array.length);
		
		Util.shuffle(array);
		
		assertFalse(Arrays.equals(array, originalArray));
		
		Arrays.sort(array);
		assertTrue(Arrays.equals(array, originalArray));
		
	}
	
}
