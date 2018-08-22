package org.snowjak.rays.geometry.boundingvolume;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;
import org.snowjak.rays.Settings;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.transform.RotationTransform;

public class AABBTest {
	
	@Test
	public void testAABBAABBListOfTransform() {
		
		AABB unTransformed = new AABB(new Point3D(0, 0, 0), new Point3D(1, 2, 0));
		AABB transformed = new AABB(unTransformed, Arrays.asList(new RotationTransform(Vector3D.K, 90d)));
		
		assertEquals("Rotated min-extent-X not as expected!", -2d, transformed.getMinExtent().getX(), 0.00001);
		assertEquals("Rotated min-extent-Y not as expected!", 0d, transformed.getMinExtent().getY(), 0.00001);
		assertEquals("Rotated min-extent-Z not as expected!", 0d, transformed.getMinExtent().getZ(), 0.00001);
		
		assertEquals("Rotated max-extent-X not as expected!", 0d, transformed.getMaxExtent().getX(), 0.00001);
		assertEquals("Rotated max-extent-Y not as expected!", 1d, transformed.getMaxExtent().getY(), 0.00001);
		assertEquals("Rotated max-extent-Z not as expected!", 0d, transformed.getMaxExtent().getZ(), 0.00001);
	}
	
	@Test
	public void testAABBCollectionOfPointListOfTransform() {
		
		AABB transformed = new AABB(Arrays.asList(new Point3D(0, 0, 0), new Point3D(1, 2, 0)),
				Arrays.asList(new RotationTransform(Vector3D.K, -90d)));
		
		assertEquals("Rotated min-extent-X not as expected!", 0d, transformed.getMinExtent().getX(), 0.00001);
		assertEquals("Rotated min-extent-Y not as expected!", -1d, transformed.getMinExtent().getY(), 0.00001);
		assertEquals("Rotated min-extent-Z not as expected!", 0d, transformed.getMinExtent().getZ(), 0.00001);
		
		assertEquals("Rotated max-extent-X not as expected!", 2, transformed.getMaxExtent().getX(), 0.00001);
		assertEquals("Rotated max-extent-Y not as expected!", 0, transformed.getMaxExtent().getY(), 0.00001);
		assertEquals("Rotated max-extent-Z not as expected!", 0d, transformed.getMaxExtent().getZ(), 0.00001);
	}
	
	@Test
	public void testAABBCollectionOfPoint() {
		
		AABB aabb = new AABB(Arrays.asList(new Point3D(0, 0, 0), new Point3D(-1, 0, 3), new Point3D(1, -2, 0),
				new Point3D(1, 2, -3)));
		
		assertEquals("Min-extent-X not as expected!", -1d, aabb.getMinExtent().getX(), 0.00001);
		assertEquals("Min-extent-Y not as expected!", -2d, aabb.getMinExtent().getY(), 0.00001);
		assertEquals("Min-extent-Z not as expected!", -3d, aabb.getMinExtent().getZ(), 0.00001);
		
		assertEquals("Max-extent-X not as expected!", 1d, aabb.getMaxExtent().getX(), 0.00001);
		assertEquals("Max-extent-Y not as expected!", 2d, aabb.getMaxExtent().getY(), 0.00001);
		assertEquals("Max-extent-Z not as expected!", 3d, aabb.getMaxExtent().getZ(), 0.00001);
	}
	
	@Test
	public void testUnion() {
		
		AABB aabb1 = new AABB(new Point3D(-1, -1, -1), new Point3D(0, 0, 0)),
				aabb2 = new AABB(new Point3D(-1, 0, -1), new Point3D(2, 2, 2));
		AABB union = AABB.union(Arrays.asList(aabb1, aabb2));
		
		assertEquals("Min-extent-X not as expected!", -1d, union.getMinExtent().getX(), 0.00001);
		assertEquals("Min-extent-Y not as expected!", -1d, union.getMinExtent().getY(), 0.00001);
		assertEquals("Min-extent-Z not as expected!", -1d, union.getMinExtent().getZ(), 0.00001);
		
		assertEquals("Max-extent-X not as expected!", 2d, union.getMaxExtent().getX(), 0.00001);
		assertEquals("Max-extent-Y not as expected!", 2d, union.getMaxExtent().getY(), 0.00001);
		assertEquals("Max-extent-Z not as expected!", 2d, union.getMaxExtent().getZ(), 0.00001);
	}
	
	@Test
	public void testContaining_true() {
		
		AABB aabb1 = new AABB(new Point3D(-1, -1, -1), new Point3D(1, 1, 1));
		AABB aabb2 = new AABB(new Point3D(-0.5, -0.5, -0.5), new Point3D(0.5, 0.5, 0.5));
		
		assertTrue(aabb1.isContaining(aabb2));
		assertFalse(aabb2.isContaining(aabb1));
	}
	
	@Test
	public void testContaining_false() {
		
		AABB aabb1 = new AABB(new Point3D(-1, -1, -1), new Point3D(0, 0, 0));
		AABB aabb2 = new AABB(new Point3D(-0.5, -0.5, -0.5), new Point3D(0.5, 0.5, 0.5));
		
		assertFalse(aabb1.isContaining(aabb2));
		assertFalse(aabb2.isContaining(aabb1));
	}
	
	@Test
	public void testOverlapping_true() {
		
		AABB aabb1 = new AABB(new Point3D(-1, -1, -1), new Point3D(0.5, 0.5, 0.5));
		AABB aabb2 = new AABB(new Point3D(0, 0, 0), new Point3D(1, 1, 1));
		
		assertTrue(aabb1.isOverlapping(aabb2));
		assertTrue(aabb2.isOverlapping(aabb1));
	}
	
	@Test
	public void testOverlapping_false() {
		
		AABB aabb1 = new AABB(new Point3D(-1, -1, -1), new Point3D(-0.5, -0.5, -0.5));
		AABB aabb2 = new AABB(new Point3D(0, 0, 0), new Point3D(1, 1, 1));
		
		assertFalse(aabb1.isOverlapping(aabb2));
		assertFalse(aabb2.isOverlapping(aabb1));
	}
	
	@Test
	public void testIsIntersecting() {
		
		AABB aabb = new AABB(new Point3D(0, 0, 0), new Point3D(2, 2, 2));
		
		Ray rayMiss = new Ray(new Point3D(-1, -1, -5), new Vector3D(0, 0, 1));
		Ray rayHit = new Ray(new Point3D(1, 1, 1), new Vector3D(0, 0, 1));
		
		assertFalse("Expected miss is a hit!", aabb.isIntersecting(rayMiss));
		assertTrue("Expected hit is a miss!", aabb.isIntersecting(rayHit));
	}
	
	@Test
	public void testSerialization() {
		
		final var aabb = new AABB(new Point3D(1, 2, 3), new Point3D(2, 3, 4));
		final var expected = "{\"minExtent\":{\"x\":1.0,\"y\":2.0,\"z\":3.0},\"maxExtent\":{\"x\":2.0,\"y\":3.0,\"z\":4.0}}";
		
		final var result = Settings.getInstance().getGson().toJson(aabb);
		
		assertEquals(expected, result);
	}
	
	@Test
	public void testDeserialization() {
		
		final var json = "{\"minExtent\":{\"x\":1.0,\"y\":2.0,\"z\":3.0},\"maxExtent\":{\"x\":2.0,\"y\":3.0,\"z\":4.0}}";
		final var expected = new AABB(new Point3D(1, 2, 3), new Point3D(2, 3, 4));
		
		final var result = Settings.getInstance().getGson().fromJson(json, AABB.class);
		
		assertNotNull(result);
		assertEquals(expected.getMinExtent().getX(), result.getMinExtent().getX(), 0.00001);
		assertEquals(expected.getMinExtent().getY(), result.getMinExtent().getY(), 0.00001);
		assertEquals(expected.getMinExtent().getZ(), result.getMinExtent().getZ(), 0.00001);
		
		assertEquals(expected.getMaxExtent().getX(), result.getMaxExtent().getX(), 0.00001);
		assertEquals(expected.getMaxExtent().getY(), result.getMaxExtent().getY(), 0.00001);
		assertEquals(expected.getMaxExtent().getZ(), result.getMaxExtent().getZ(), 0.00001);
	}
	
}
