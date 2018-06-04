package org.snowjak.rays.transform;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.snowjak.rays.geometry.Normal3D;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.geometry.Vector3D;

public class TranslationTransformTest {
	
	private Transform transform;
	
	@Before
	public void setUp() {
		
		transform = new TranslationTransform(+1, +2, +3);
	}
	
	@Test
	public void worldToLocalPoint() {
		
		Point3D point = new Point3D(5, 5, 5);
		Point3D transformed = transform.worldToLocal(point);
		
		assertEquals("Transformed X not as expected!", 4, transformed.getX(), 0.00001);
		assertEquals("Transformed Y not as expected!", 3, transformed.getY(), 0.00001);
		assertEquals("Transformed Z not as expected!", 2, transformed.getZ(), 0.00001);
	}
	
	@Test
	public void localToWorldPoint() {
		
		Point3D point = new Point3D(5, 5, 5);
		Point3D transformed = transform.localToWorld(point);
		
		assertEquals("Transformed X not as expected!", 6, transformed.getX(), 0.00001);
		assertEquals("Transformed Y not as expected!", 7, transformed.getY(), 0.00001);
		assertEquals("Transformed Z not as expected!", 8, transformed.getZ(), 0.00001);
	}
	
	@Test
	public void worldToLocalVector() {
		
		Vector3D vector = new Vector3D(5, 5, 5);
		Vector3D transformed = transform.worldToLocal(vector);
		
		assertEquals("Transformed X not as expected!", 5, transformed.getX(), 0.00001);
		assertEquals("Transformed Y not as expected!", 5, transformed.getY(), 0.00001);
		assertEquals("Transformed Z not as expected!", 5, transformed.getZ(), 0.00001);
	}
	
	@Test
	public void localToWorldVector() {
		
		Vector3D vector = new Vector3D(5, 5, 5);
		Vector3D transformed = transform.localToWorld(vector);
		
		assertEquals("Transformed X not as expected!", 5, transformed.getX(), 0.00001);
		assertEquals("Transformed Y not as expected!", 5, transformed.getY(), 0.00001);
		assertEquals("Transformed Z not as expected!", 5, transformed.getZ(), 0.00001);
	}
	
	@Test
	public void worldToLocalNormal() {
		
		Normal3D normal = new Normal3D(5, 5, 5);
		Normal3D transformed = transform.worldToLocal(normal);
		
		assertEquals("Transformed X not as expected!", 5, transformed.getX(), 0.00001);
		assertEquals("Transformed Y not as expected!", 5, transformed.getY(), 0.00001);
		assertEquals("Transformed Z not as expected!", 5, transformed.getZ(), 0.00001);
	}
	
	@Test
	public void localToWorldNormal() {
		
		Normal3D normal = new Normal3D(5, 5, 5);
		Normal3D transformed = transform.localToWorld(normal);
		
		assertEquals("Transformed X not as expected!", 5, transformed.getX(), 0.00001);
		assertEquals("Transformed Y not as expected!", 5, transformed.getY(), 0.00001);
		assertEquals("Transformed Z not as expected!", 5, transformed.getZ(), 0.00001);
	}
	
}
