package org.snowjak.rays.geometry;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.snowjak.rays.geometry.Normal3D;

public class Normal3DTest {
	
	@Test
	public void testGetX() {
		
		final Normal3D n = new Normal3D(3, -6, 5);
		
		assertEquals(3d, n.getX(), 0.00001);
	}
	
	@Test
	public void testGetY() {
		
		final Normal3D n = new Normal3D(3, -6, 5);
		
		assertEquals(-6d, n.getY(), 0.00001);
	}
	
	@Test
	public void testGetZ() {
		
		final Normal3D n = new Normal3D(3, -6, 5);
		
		assertEquals(5d, n.getZ(), 0.00001);
	}
	
}
