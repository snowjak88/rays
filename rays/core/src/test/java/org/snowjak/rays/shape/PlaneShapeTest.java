package org.snowjak.rays.shape;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.snowjak.rays.geometry.Normal3D;
import org.snowjak.rays.geometry.Point2D;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.interact.SurfaceDescriptor;
import org.snowjak.rays.transform.TranslationTransform;

public class PlaneShapeTest {

	@Test
	public void testGetSurfaceNearestTo() {

		final PlaneShape plane = new PlaneShape(Arrays.asList(new TranslationTransform(0, -3, 0)));

		final Point3D nearbyPoint = new Point3D(3, 5, 2);
		final SurfaceDescriptor<PlaneShape> surfaceDescriptor = plane.getSurfaceNearestTo(nearbyPoint);
		final Point3D surfacePoint = surfaceDescriptor.getPoint();
		final Normal3D surfaceNormal = surfaceDescriptor.getNormal();
		final Point2D surfaceParam = surfaceDescriptor.getParam();

		assertEquals("Nearby surface point-X not as expected!", 3, surfacePoint.getX(), 0.00001);
		assertEquals("Nearby surface point-Y not as expected!", -3, surfacePoint.getY(), 0.00001);
		assertEquals("Nearby surface point-Z not as expected!", 2, surfacePoint.getZ(), 0.00001);

		assertEquals("Nearby surface normal-X not as expected!", 0, surfaceNormal.getX(), 0.00001);
		assertEquals("Nearby surface normal-Y not as expected!", 1, surfaceNormal.getY(), 0.00001);
		assertEquals("Nearby surface normal-Z not as expected!", 0, surfaceNormal.getZ(), 0.00001);

		assertEquals("Nearby surface param-X not as expected!", 3, surfaceParam.getX(), 0.00001);
		assertEquals("Nearby surface param-Y not as expected!", 2, surfaceParam.getY(), 0.00001);
	}

	@Test
	public void testGetParamFromLocalSurface() {

		PlaneShape plane = new PlaneShape(Collections.emptyList());

		final Point3D surfacePoint = new Point3D(5, 0, 2);
		final Point2D surfaceParam = plane.getParamFromLocalSurface(surfacePoint);

		assertEquals("Surface param-X not as expected!", 5, surfaceParam.getX(), 0.00001);
		assertEquals("Surface param-Y not as expected!", 2, surfaceParam.getY(), 0.00001);
	}

	@Test
	public void testIsIntersectableWith() {

		final PlaneShape plane = new PlaneShape();

		Ray ray = new Ray(new Point3D(3, 3, 0), new Vector3D(-1, -1, 0).normalize());
		assertTrue(plane.isIntersectableWith(ray));
	}

	@Test
	public void testGetIntersection() {

		final PlaneShape plane = new PlaneShape();

		Ray localRay = new Ray(new Point3D(3, 3, 2), new Vector3D(-1, -1, 0).normalize());
		SurfaceDescriptor<PlaneShape> surfaceDescriptor = plane.getSurface(localRay);

		Point3D surfacePoint = surfaceDescriptor.getPoint();
		Normal3D surfaceNormal = surfaceDescriptor.getNormal();
		Point2D surfaceParam = surfaceDescriptor.getParam();

		assertEquals("Nearby surface point-X not as expected!", 0, surfacePoint.getX(), 0.00001);
		assertEquals("Nearby surface point-Y not as expected!", 0, surfacePoint.getY(), 0.00001);
		assertEquals("Nearby surface point-Z not as expected!", 2, surfacePoint.getZ(), 0.00001);

		assertEquals("Nearby surface normal-X not as expected!", 0, surfaceNormal.getX(), 0.00001);
		assertEquals("Nearby surface normal-Y not as expected!", 1, surfaceNormal.getY(), 0.00001);
		assertEquals("Nearby surface normal-Z not as expected!", 0, surfaceNormal.getZ(), 0.00001);

		assertEquals("Nearby surface param-X not as expected!", 0, surfaceParam.getX(), 0.00001);
		assertEquals("Nearby surface param-Y not as expected!", 2, surfaceParam.getY(), 0.00001);
	}

}
