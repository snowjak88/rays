package org.snowjak.rays.shape;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.snowjak.rays.Settings;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.interact.SurfaceDescriptor;
import org.snowjak.rays.transform.RotationTransform;
import org.snowjak.rays.transform.TranslationTransform;

public class SphereShapeTest {
	
	private SphereShape sphere;
	
	@Before
	public void setUp() throws Exception {
		
		sphere = new SphereShape(1d,
				Arrays.asList(new TranslationTransform(3d, 0d, 0d), new RotationTransform(Vector3D.J, 90d)));
	}
	
	@Test
	public void testGetSurface_hit() {
		
		Ray ray = new Ray(new Point3D(0, 0, 0), new Vector3D(1, 0, 0));
		
		SurfaceDescriptor<Shape> hit = sphere.getSurface(ray);
		
		assertNotNull("Expected hit was actually a miss!", hit);
		
		assertEquals("Hit point X not as expected", 2d, hit.getPoint().getX(), 0.00001);
		assertEquals("Hit point Y not as expected", 0d, hit.getPoint().getY(), 0.00001);
		assertEquals("Hit point Z not as expected", 0d, hit.getPoint().getZ(), 0.00001);
		
		assertEquals("Hit normal X not as expected", -1d, hit.getNormal().getX(), 0.00001);
		assertEquals("Hit normal Y not as expected", 0d, hit.getNormal().getY(), 0.00001);
		assertEquals("Hit normal Z not as expected", 0d, hit.getNormal().getZ(), 0.00001);
		
		assertEquals("Hit point distance not as expected", 2d,
				Vector3D.from(hit.getPoint().subtract(ray.getOrigin())).getMagnitude(), 0.00001);
	}
	
	@Test
	public void testGetSurface_miss() {
		
		Ray ray = new Ray(new Point3D(0, 0, -5), new Vector3D(1, 0, 0));
		
		SurfaceDescriptor<Shape> hit = sphere.getSurface(ray);
		
		assertNull("Expected miss was actually a hit!", hit);
	}
	
	@Test
	public void testGetSurfaceNearest_exterior() {
		
		final var neighbor = new Point3D(-2, 0, 0);
		final var sphere = new SphereShape(1d, new RotationTransform(Vector3D.J, 90));
		
		final var surface = sphere.getSurfaceNearestTo(neighbor);
		assertEquals(-1d, surface.getNormal().getX(), 0.0001);
		assertEquals(0d, surface.getNormal().getY(), 0.0001);
		assertEquals(0d, surface.getNormal().getZ(), 0.0001);
	}
	
	@Test
	public void testGetSurfaceNearest_interior() {
		
		final var neighbor = new Point3D(-1, 0, 0);
		final var sphere = new SphereShape(2d, new RotationTransform(Vector3D.J, 90));
		
		final var surface = sphere.getSurfaceNearestTo(neighbor);
		assertEquals(1d, surface.getNormal().getX(), 0.0001);
		assertEquals(0d, surface.getNormal().getY(), 0.0001);
		assertEquals(0d, surface.getNormal().getZ(), 0.0001);
	}
	
	@Test
	public void testSerialization() {
		
		final var sphere = new SphereShape(0.5, new TranslationTransform(1, 2, 3));
		
		final var expected = "{\"type\":\"sphere\",\"radius\":0.5,\"worldToLocal\":[{\"type\":\"translate\",\"dx\":1.0,\"dy\":2.0,\"dz\":3.0}]}";
		
		final var result = Settings.getInstance().getGson().toJson(sphere);
		
		assertEquals(expected, result);
	}
	
	@Test
	public void testDeserialization() {
		
		final var json = "{\"type\":\"sphere\",\"radius\":0.5,\"worldToLocal\":[{\"type\":\"translate\",\"dx\":1.0,\"dy\":2.0,\"dz\":3.0}]}";
		
		final var expected = new SphereShape(0.5, new TranslationTransform(1, 2, 3));
		
		final var result = Settings.getInstance().getGson().fromJson(json, Shape.class);
		
		assertNotNull(result);
		
		assertTrue(SphereShape.class.isAssignableFrom(result.getClass()));
		
		final var sphere = (SphereShape) result;
		
		assertEquals(expected.getRadius(), sphere.getRadius(), 0.00001);
		assertEquals(expected.getWorldToLocalTransforms().size(), sphere.getWorldToLocalTransforms().size());
		assertEquals(expected.getWorldToLocalTransforms().size(), 1);
		
		assertTrue(TranslationTransform.class.isAssignableFrom(expected.getWorldToLocalTransforms().get(0).getClass()));
		assertTrue(TranslationTransform.class.isAssignableFrom(sphere.getWorldToLocalTransforms().get(0).getClass()));
		
		assertEquals(((TranslationTransform) expected.getWorldToLocalTransforms().get(0)).getDx(),
				((TranslationTransform) sphere.getWorldToLocalTransforms().get(0)).getDx(), 0.00001);
		assertEquals(((TranslationTransform) expected.getWorldToLocalTransforms().get(0)).getDy(),
				((TranslationTransform) sphere.getWorldToLocalTransforms().get(0)).getDy(), 0.00001);
		assertEquals(((TranslationTransform) expected.getWorldToLocalTransforms().get(0)).getDz(),
				((TranslationTransform) sphere.getWorldToLocalTransforms().get(0)).getDz(), 0.00001);
		
	}
}
