package org.snowjak.rays.geometry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.snowjak.rays.Settings;

public class RayTest {
	
	@Test
	public void testSerialize() {
		
		final Ray r = new Ray(new Point3D(1, 2, 3), Vector3D.J, 1.0, 2, -3, 4);
		final var expected = "{\"o\":{\"x\":1.0,\"y\":2.0,\"z\":3.0},\"d\":{\"x\":0.0,\"y\":1.0,\"z\":0.0},\"t\":1.0,\"depth\":2,\"minT\":-3.0,\"maxT\":4.0}";
		
		assertEquals(expected, Settings.getInstance().getGson().toJson(r));
	}
	
	@Test
	public void testDeserialize() {
		
		final var json = "{\"o\":{\"x\":1.0,\"y\":2.0,\"z\":3.0},\"d\":{\"x\":0.0,\"y\":1.0,\"z\":0.0},\"t\":1.0,\"depth\":2,\"minT\":-3.0,\"maxT\":4.0}";
		final Ray r = new Ray(new Point3D(1, 2, 3), Vector3D.J, 1.0, 2, -3, 4);
		
		final var result = Settings.getInstance().getGson().fromJson(json, Ray.class);
		
		assertNotNull(result);
		assertEquals(r.getOrigin().getX(), result.getOrigin().getX(), 0.00001);
		assertEquals(r.getOrigin().getY(), result.getOrigin().getY(), 0.00001);
		assertEquals(r.getOrigin().getZ(), result.getOrigin().getZ(), 0.00001);
		
		assertEquals(r.getDirection().getX(), result.getDirection().getX(), 0.00001);
		assertEquals(r.getDirection().getY(), result.getDirection().getY(), 0.00001);
		assertEquals(r.getDirection().getZ(), result.getDirection().getZ(), 0.00001);
		
		assertEquals(r.getT(), result.getT(), 0.00001);
		assertEquals(r.getDepth(), result.getDepth());
		assertEquals(r.getWindowMinT(), result.getWindowMinT(), 0.00001);
		assertEquals(r.getWindowMaxT(), result.getWindowMaxT(), 0.00001);
	}
	
}
