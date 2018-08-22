package org.snowjak.rays.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.snowjak.rays.Settings;
import org.snowjak.rays.geometry.Point3D;

import com.google.gson.GsonBuilder;

public class Point3DTest {
	
	@Test
	public void testGetX() {
		
		final Point3D p = new Point3D(3, -7, 2);
		
		assertEquals(3d, p.getX(), 0.00001);
	}
	
	@Test
	public void testGetY() {
		
		final Point3D p = new Point3D(3, -7, 2);
		
		assertEquals(-7d, p.getY(), 0.00001);
	}
	
	@Test
	public void testGetZ() {
		
		final Point3D p = new Point3D(3, -7, 2);
		
		assertEquals(2d, p.getZ(), 0.00001);
	}
	
	@Test
	public void testSerialize() {
		
		final Point3D p = new Point3D(-6, 2, 4);
		final var expected = "{\"x\":-6.0,\"y\":2.0,\"z\":4.0}";
		
		final var gson = new GsonBuilder().registerTypeAdapter(Point3D.class, new Point3D.Loader()).create();
		
		assertEquals(expected, gson.toJson(p));
	}
	
	@Test
	public void testDeserialize() {
		
		final var json = "{\"x\":-6.0,\"y\":2.0,\"z\":4.0}";
		final Point3D p = new Point3D(-6, 2, 4);
		
		final var result = Settings.getInstance().getGson().fromJson(json, Point3D.class);
		
		assertNotNull(result);
		assertEquals(p.getX(), result.getX(), 0.00001);
		assertEquals(p.getY(), result.getY(), 0.00001);
		assertEquals(p.getZ(), result.getZ(), 0.00001);
	}
}
