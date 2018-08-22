package org.snowjak.rays.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.snowjak.rays.Settings;

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
	
	@Test
	public void testSerialize() {
		
		final Point2D p = new Point2D(-6, 2);
		final var expected = "{\"x\":-6.0,\"y\":2.0}";
		
		assertEquals(expected, Settings.getInstance().getGson().toJson(p));
	}
	
	@Test
	public void testDeserialize() {
		
		final var json = "{\"x\":-6.0,\"y\":2.0}";
		final Point2D p = new Point2D(-6, 2);
		
		final var result = Settings.getInstance().getGson().fromJson(json, Point2D.class);
		
		assertNotNull(result);
		assertEquals(p.getX(), result.getX(), 0.00001);
		assertEquals(p.getY(), result.getY(), 0.00001);
	}
}
