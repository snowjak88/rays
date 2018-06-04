package org.snowjak.rays.geometry;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.snowjak.rays.geometry.Point2D;

public class Point2DTest {
	
	@Test
	public void testGetX() {
		
		final Point2D p = new Point2D(-6, 2);
		
		assertEquals(-6d, p.getX(), 0.00001);
	}
	
	@Test
	public void testGetY() {
		
		final Point2D p = new Point2D(-6, 2);
		
		assertEquals(2d, p.getY(), 0.00001);
	}
	
}
