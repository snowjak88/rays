package org.snowjak.rays.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.snowjak.rays.Settings;

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
	
	@Test
	public void testSerialize() {
		
		final var n = new Normal3D(3, -6, 5);
		final var expected = "{\"x\":3.0,\"y\":-6.0,\"z\":5.0}";
		
		assertEquals(expected, Settings.getInstance().getGson().toJson(n));
	}
	
	@Test
	public void testDeserialize() {
		
		final var json = "{\"x\":3.0,\"y\":-6.0,\"z\":5.0}";
		final var n = new Normal3D(3, -6, 5);
		
		final var result = Settings.getInstance().getGson().fromJson(json, Normal3D.class);
		
		assertNotNull(result);
		assertEquals(n.getX(), result.getX(), 0.00001);
		assertEquals(n.getY(), result.getY(), 0.00001);
		assertEquals(n.getZ(), result.getZ(), 0.00001);
	}
}
