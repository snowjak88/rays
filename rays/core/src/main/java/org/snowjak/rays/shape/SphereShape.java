package org.snowjak.rays.shape;

import static org.apache.commons.math3.util.FastMath.PI;
import static org.apache.commons.math3.util.FastMath.abs;
import static org.apache.commons.math3.util.FastMath.acos;
import static org.apache.commons.math3.util.FastMath.atan2;
import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.sin;
import static org.apache.commons.math3.util.FastMath.sqrt;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.snowjak.rays.Settings;
import org.snowjak.rays.geometry.Normal3D;
import org.snowjak.rays.geometry.Point2D;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.geometry.boundingvolume.AABB;
import org.snowjak.rays.interact.SurfaceDescriptor;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.transform.Transform;

public class SphereShape extends Shape {
	
	private final AABB aabb;
	private final double radius;
	
	public SphereShape(double radius) {
		
		this(radius, Collections.emptyList());
	}
	
	public SphereShape(double radius, Transform... worldToLocal) {
		
		this(radius, Arrays.asList(worldToLocal));
	}
	
	public SphereShape(double radius, List<Transform> worldToLocal) {
		
		super(worldToLocal);
		this.radius = radius;
		this.aabb = new AABB(new Point3D(-radius, -radius, -radius), new Point3D(+radius, +radius, +radius));
	}
	
	@Override
	public boolean isIntersectableWith(Ray ray) {
		
		return aabb.isIntersecting(worldToLocal(ray));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public SurfaceDescriptor<SphereShape> getSurface(Ray ray) {
		
		final Ray localRay = worldToLocal(ray);
		final Double localIntersectionT = getLocalIntersectionT(localRay, false);
		
		if (localIntersectionT == null)
			return null;
		
		final Point3D localPoint = localRay.getPointAlong(localIntersectionT);
		final Normal3D localNormal = Normal3D.from(Vector3D.from(localPoint).normalize());
		
		return localToWorld(new SurfaceDescriptor<SphereShape>(this, localPoint, localNormal,
				getParamFromLocalSurface(localPoint)));
	}
	
	/**
	 * For a given Ray, calculate the smallest value <code>t</code> that defines its
	 * intersection-point along that ray with this sphere -- or <code>null</code> if
	 * no such intersection exists.
	 * <p>
	 * <strong>Note</strong> that this works only in object-local coordinates!
	 * </p>
	 * 
	 * @param ray
	 * @param includeBehindRay
	 *            <code>true</code> if we should consider intersections behind the
	 *            Ray, or only those in front of it
	 * @return
	 */
	private Double getLocalIntersectionT(Ray ray, boolean includeBehindRay) {
		
		Vector3D l = Vector3D.from(ray.getOrigin()).negate();
		double t_ca = l.dotProduct(ray.getDirection());
		
		if (t_ca < 0d)
			return null;
		
		double d2 = l.dotProduct(l) - (t_ca * t_ca);
		double r2 = (radius * radius);
		if (d2 > r2)
			return null;
		
		double t_hc = sqrt(r2 - d2);
		
		double t0 = t_ca - t_hc;
		double t1 = t_ca + t_hc;
		
		if (includeBehindRay) {
			if (abs(t0) < abs(t1))
				return t0;
			else
				return t1;
		}
		
		if (t0 < Settings.getInstance().getDoubleEqualityEpsilon()
				&& t1 < Settings.getInstance().getDoubleEqualityEpsilon())
			return null;
		
		if (t0 < Settings.getInstance().getDoubleEqualityEpsilon())
			return t1;
		else if (t1 < Settings.getInstance().getDoubleEqualityEpsilon())
			return t0;
		else if (t0 < t1)
			return t0;
		else
			return t1;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public SurfaceDescriptor<SphereShape> getSurfaceNearestTo(Point3D neighbor) {
		
		return getSurface(new Ray(neighbor, Vector3D.from(getObjectZero().subtract(neighbor))));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public SurfaceDescriptor<SphereShape> sampleSurface(Sample sample) {
		
		final Point2D samplePoint = sample.getAdditional2DSample();
		
		final double sin2_theta = samplePoint.getX();
		final double cos2_theta = 1d - sin2_theta;
		final double sin_theta = sqrt(sin2_theta);
		final double cos_theta = sqrt(cos2_theta);
		
		final double orientation = samplePoint.getY() * 2d * PI;
		//
		final double x = sin_theta * cos(orientation);
		final double y = cos_theta;
		final double z = sin_theta * sin(orientation);
		//
		final Vector3D samplePoint_local = new Vector3D(x, y, z).multiply(radius);
		final Normal3D normal_local = Normal3D.from(samplePoint_local.normalize());
		return localToWorld(new SurfaceDescriptor<SphereShape>(this, Point3D.from(samplePoint_local), normal_local,
				getParamFromLocalSurface(Point3D.from(samplePoint_local))));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public SurfaceDescriptor<SphereShape> sampleSurfaceFacing(Point3D neighbor, Sample sample) {
		
		final Vector3D towardsV_local = Vector3D.from(worldToLocal(neighbor));
		
		final Vector3D J = towardsV_local.normalize();
		final Vector3D I = J.orthogonal();
		final Vector3D K = I.crossProduct(J);
		//
		//
		//
		final Point2D samplePoint = sample.getAdditional2DSample();
		
		final double sin2_theta = samplePoint.getX();
		final double cos2_theta = 1d - sin2_theta;
		final double sin_theta = sqrt(sin2_theta);
		final double cos_theta = sqrt(cos2_theta);
		
		final double orientation = samplePoint.getY() * 2d * PI;
		//
		final double x = sin_theta * cos(orientation);
		final double y = cos_theta;
		final double z = sin_theta * sin(orientation);
		//
		final Vector3D samplePoint_local = (Vector3D) I.multiply(x).add(J.multiply(y)).add(K.multiply(z))
				.multiply(radius);
		final Normal3D normal_local = Normal3D.from(samplePoint_local.normalize());
		return localToWorld(new SurfaceDescriptor<SphereShape>(this, Point3D.from(samplePoint_local), normal_local,
				getParamFromLocalSurface(Point3D.from(samplePoint_local))));
	}
	
	@Override
	public double computeSolidAngle(Point3D viewedFrom) {
		
		return computeSolidAngle_sphere(viewedFrom, radius);
	}
	
	@Override
	public Point2D getParamFromLocalSurface(Point3D point) {
		
		// Compute the surface parameterization in terms of theta and .
		// Theta = acos ( z / r ) [normalized to [0,1] ]
		// Phi = atan ( y / x ) [normalized to [0,1] ]
		return new Point2D(acos(point.getZ() / radius) / (PI), (atan2(point.getY(), point.getX()) + PI) / (2d * PI));
	}
	
	/**
	 * @return this sphere's radius
	 */
	public double getRadius() {
		
		return radius;
	}
	
}
