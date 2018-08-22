package org.snowjak.rays.transform;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.snowjak.rays.Settings;
import org.snowjak.rays.geometry.Normal3D;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.geometry.Vector3D;

public class RotationTransformTest {
	
	private Transform transformAboutK;
	private Transform transformAboutJ;
	
	@Before
	public void setUp() throws Exception {
		
		transformAboutK = new RotationTransform(Vector3D.K, 90d);
		transformAboutJ = new RotationTransform(Vector3D.J, -90d);
	}
	
	@Test
	public void testWorldToLocalPoint() {
		
		Point3D point = new Point3D(1, 2, 3);
		Point3D transformed = transformAboutK.worldToLocal(point);
		
		assertEquals("Transformed X not as expected!", 2d, transformed.getX(), 0.00001);
		assertEquals("Transformed Y not as expected!", -1d, transformed.getY(), 0.00001);
		assertEquals("Transformed Z not as expected!", 3d, transformed.getZ(), 0.00001);
	}
	
	@Test
	public void testLocalToWorldPoint() {
		
		Point3D point = new Point3D(1, 2, 3);
		Point3D transformed = transformAboutJ.localToWorld(point);
		
		assertEquals("Transformed X not as expected!", -3d, transformed.getX(), 0.00001);
		assertEquals("Transformed Y not as expected!", 2d, transformed.getY(), 0.00001);
		assertEquals("Transformed Z not as expected!", 1d, transformed.getZ(), 0.00001);
	}
	
	@Test
	public void testWorldToLocalVector() {
		
		Vector3D vector = new Vector3D(1, 2, 3);
		Vector3D transformed = transformAboutK.worldToLocal(vector);
		
		assertEquals("Transformed X not as expected!", 2d, transformed.getX(), 0.00001);
		assertEquals("Transformed Y not as expected!", -1d, transformed.getY(), 0.00001);
		assertEquals("Transformed Z not as expected!", 3d, transformed.getZ(), 0.00001);
	}
	
	@Test
	public void testLocalToWorldVector() {
		
		Vector3D vector = new Vector3D(1, 2, 3);
		Vector3D transformed = transformAboutK.localToWorld(vector);
		
		assertEquals("Transformed X not as expected!", -2d, transformed.getX(), 0.00001);
		assertEquals("Transformed Y not as expected!", 1d, transformed.getY(), 0.00001);
		assertEquals("Transformed Z not as expected!", 3d, transformed.getZ(), 0.00001);
	}
	
	@Test
	public void testWorldToLocalNormal() {
		
		Normal3D normal = new Normal3D(1, 2, 3);
		Normal3D transformed = transformAboutK.worldToLocal(normal);
		
		assertEquals("Transformed X not as expected!", 2d, transformed.getX(), 0.00001);
		assertEquals("Transformed Y not as expected!", -1d, transformed.getY(), 0.00001);
		assertEquals("Transformed Z not as expected!", 3d, transformed.getZ(), 0.00001);
	}
	
	@Test
	public void testLocalToWorldNormal() {
		
		Normal3D normal = new Normal3D(1, 2, 3);
		Normal3D transformed = transformAboutK.localToWorld(normal);
		
		assertEquals("Transformed X not as expected!", -2d, transformed.getX(), 0.00001);
		assertEquals("Transformed Y not as expected!", 1d, transformed.getY(), 0.00001);
		assertEquals("Transformed Z not as expected!", 3d, transformed.getZ(), 0.00001);
	}
	
	@Test
	public void testSerialize() {
		
		final var transform = (RotationTransform) transformAboutJ;
		
		final var expected = "{\"type\":\"rotate\",\"axis\":{\"x\":0.0,\"y\":1.0,\"z\":0.0},\"degreesOfRotation\":-90.0}";
		
		final var result = Settings.getInstance().getGson().toJson(transform);
		
		assertEquals(expected, result);
	}
	
	@Test
	public void testDeserialize() {
		
		final var json = "{\"type\":\"rotate\",\"axis\":{\"x\":0.0,\"y\":1.0,\"z\":0.0},\"degreesOfRotation\":-90.0}";
		final var expected = (RotationTransform) transformAboutJ;
		
		final var result = Settings.getInstance().getGson().fromJson(json, Transform.class);
		
		assertTrue(RotationTransform.class.isAssignableFrom(result.getClass()));
		
		final var rotation = (RotationTransform) result;
		
		assertEquals(expected.getAxis().getX(), rotation.getAxis().getX(), 0.00001);
		assertEquals(expected.getAxis().getY(), rotation.getAxis().getY(), 0.00001);
		assertEquals(expected.getAxis().getZ(), rotation.getAxis().getZ(), 0.00001);
		
		assertEquals(expected.getDegreesOfRotation(), rotation.getDegreesOfRotation(), 0.00001);
	}
	
}
