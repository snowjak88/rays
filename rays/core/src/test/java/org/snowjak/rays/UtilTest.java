package org.snowjak.rays;

import static org.junit.Assert.*;

import org.apache.commons.math3.util.FastMath;
import org.junit.Test;

public class UtilTest {
	
	@Test
	public void test_integrateSine() {
		
		assertEquals(1d, Util.integrate(0d, FastMath.PI / 2d, 1024, (x) -> FastMath.sin(x)), 0.00001);
	}
	
	@Test
	public void test_linear() {
		
		assertEquals(0.5d, Util.integrate(0d, 1d, 1024, (x) -> x), 0.00001);
	}
	
}
