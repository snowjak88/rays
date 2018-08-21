package org.snowjak.rays.shape;

import static org.apache.commons.math3.util.FastMath.PI;
import static org.apache.commons.math3.util.FastMath.signum;

import java.util.List;

import org.snowjak.rays.Settings;
import org.snowjak.rays.geometry.Normal3D;
import org.snowjak.rays.geometry.Point2D;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.interact.SurfaceDescriptor;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.transform.Transform;

/**
 * Represents a plane -- a flat surface of 0 thickness and infinite extent.
 * Absent any {@link Transform}s, this plane's surface-normal is oriented toward
 * {@link Vector3D#J}.
 * 
 * @author snowjak88
 */
public class PlaneShape extends Shape {
	
	private Normal3D localNormal = Normal3D.from(Vector3D.J);
	
	public PlaneShape(Transform... worldToLocal) {
		
		super(null, worldToLocal);
	}
	
	public PlaneShape(List<Transform> worldToLocal) {
		
		super(null, worldToLocal);
	}
	
	@Override
	public boolean isIntersectableWith(Ray ray) {
		
		//
		// The only way this Ray will fail to interact with this Plane anywhere
		// is if its origin y-coordinate and direction y-coordinate are of the
		// same sign (i.e., it is pointing away from the plane), or else if its
		// direction y-coordinate is 0 (i.e., it is pointing parallel to the
		// plane).
		//
		final Ray localRay = worldToLocal(ray);
		final double rayOriginY = localRay.getOrigin().getY();
		final double rayDirectionY = localRay.getDirection().getY();
		
		return !(Settings.getInstance().nearlyEqual(signum(rayOriginY), rayDirectionY))
				&& !(Settings.getInstance().nearlyEqual(rayDirectionY, 0d));
	}
	
	@Override
	public SurfaceDescriptor<Shape> getSurface(Ray ray) {
		
		final Ray localRay = worldToLocal(ray);
		
		final double t = -localRay.getOrigin().getY() / localRay.getDirection().getY();
		if (t < Settings.getInstance().getDoubleEqualityEpsilon() || Double.isNaN(t)
				|| Settings.getInstance().nearlyEqual(t, 0d))
			return null;
		
		Ray intersectingRay = new Ray(localRay.getOrigin(), localRay.getDirection(), t, localRay.getDepth(), t, t);
		Point3D intersectionPoint = intersectingRay.getPointAlong();
		Point2D surfaceParam = getParamFromLocalSurface(intersectionPoint);
		
		return localToWorld(new SurfaceDescriptor<>(this, intersectionPoint, localNormal, surfaceParam));
	}
	
	@Override
	public SurfaceDescriptor<Shape> getSurfaceNearestTo(Point3D neighbor) {
		
		Point3D localNeighbor = worldToLocal(neighbor);
		Point3D surfacePoint = new Point3D(localNeighbor.getX(), 0.0, localNeighbor.getZ());
		return localToWorld(
				new SurfaceDescriptor<>(this, surfacePoint, localNormal, getParamFromLocalSurface(surfacePoint)));
	}
	
	@Override
	public SurfaceDescriptor<Shape> sampleSurface(Sample sample) {
		
		final Point2D samplePoint = sample.getAdditional2DSample();
		final double x = (samplePoint.getX() - 0.5) * Double.MAX_VALUE;
		final double y = 0d;
		final double z = (samplePoint.getY() - 0.5) * Double.MAX_VALUE;
		
		final Point3D surfacePoint = new Point3D(x, y, z);
		
		return localToWorld(
				new SurfaceDescriptor<>(this, surfacePoint, localNormal, getParamFromLocalSurface(surfacePoint)));
	}
	
	@Override
	public SurfaceDescriptor<Shape> sampleSurfaceFacing(Point3D neighbor, Sample sample) {
		
		return sampleSurface(sample);
	}
	
	@Override
	public double computeSolidAngle(Point3D viewedFrom) {
		
		return 2d * PI;
	}
	
	@Override
	public Point2D getParamFromLocalSurface(Point3D point) {
		
		return new Point2D(point.getX(), point.getZ());
	}
	
}
