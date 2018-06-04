package org.snowjak.rays.shape;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
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

		SurfaceDescriptor<SphereShape> hit = sphere.getSurface(ray);

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

		SurfaceDescriptor<SphereShape> hit = sphere.getSurface(ray);

		assertNull("Expected miss was actually a hit!", hit);
	}

}
