package org.snowjak.rays.shape;

import static org.apache.commons.math3.util.FastMath.PI;
import static org.apache.commons.math3.util.FastMath.abs;
import static org.apache.commons.math3.util.FastMath.acos;
import static org.apache.commons.math3.util.FastMath.atan2;
import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.pow;
import static org.apache.commons.math3.util.FastMath.sin;
import static org.apache.commons.math3.util.FastMath.sqrt;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.snowjak.rays.Settings;
import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.annotations.UIType;
import org.snowjak.rays.geometry.Normal3D;
import org.snowjak.rays.geometry.Point2D;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.geometry.boundingvolume.AABB;
import org.snowjak.rays.interact.SurfaceDescriptor;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.transform.Transform;
import org.snowjak.rays.util.Duo;

@UIType(type = "sphere", fields = { @UIField(name = "radius", type = Double.class, defaultValue = "1"),
		@UIField(name = "worldToLocal", type = Collection.class, collectedType = Transform.class) })
public class SphereShape extends Shape {
	
	private double radius;
	
	private transient AABB localAabb = null;
	
	public SphereShape(double radius) {
		
		this(radius, Collections.emptyList());
	}
	
	public SphereShape(double radius, Transform... worldToLocal) {
		
		this(radius, Arrays.asList(worldToLocal));
	}
	
	public SphereShape(double radius, List<Transform> worldToLocal) {
		
		super(worldToLocal);
		this.radius = radius;
	}
	
	@Override
	public AABB getLocalBoundingVolume() {
		
		if (localAabb == null)
			localAabb = new AABB(new Point3D(-radius, -radius, -radius), new Point3D(+radius, +radius, +radius));
		
		return localAabb;
	}
	
	@Override
	public SurfaceDescriptor<Shape> getSurface(Ray ray) {
		
		final Ray localRay = worldToLocal(ray);
		final Double localIntersectionT = getLocalIntersectionT(localRay, false);
		
		if (localIntersectionT == null)
			return null;
		
		final Point3D localPoint = localRay.getPointAlong(localIntersectionT);
		final Normal3D localNormal = Normal3D.from(Vector3D.from(localPoint).normalize());
		
		return localToWorld(
				new SurfaceDescriptor<>(this, localPoint, localNormal, getParamFromLocalSurface(localPoint)));
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
	
	@Override
	public SurfaceDescriptor<Shape> getSurfaceNearestTo(Point3D neighbor) {
		
		return getSurface(new Ray(neighbor, Vector3D.from(neighbor, getObjectZero())));
	}
	
	@Override
	public SurfaceDescriptor<Shape> sampleSurfaceArea(Sample sample) {
		
		final Point2D samplePoint = sample.getAdditional2DSample();
		final boolean flipHemispheres = (sample.getAdditional1DSample() <= 0.5);
		
		final double sin2_theta = samplePoint.getX();
		final double cos2_theta = 1d - sin2_theta;
		final double sin_theta = sqrt(sin2_theta);
		final double cos_theta = sqrt(cos2_theta);
		
		final double orientation = samplePoint.getY() * 2d * PI;
		//
		final double x = sin_theta * cos(orientation);
		final double y = cos_theta * (flipHemispheres ? -1d : 1d);
		final double z = sin_theta * sin(orientation);
		//
		final Vector3D samplePoint_local = new Vector3D(x, y, z).multiply(radius);
		final Normal3D normal_local = Normal3D.from(samplePoint_local.normalize());
		return localToWorld(new SurfaceDescriptor<>(this, Point3D.from(samplePoint_local), normal_local,
				getParamFromLocalSurface(Point3D.from(samplePoint_local))));
	}
	
	@Override
	public double pdf_sampleSurfaceArea(SurfaceDescriptor<?> surface) {
		
		return 1d / (4d * PI);
	}
	
	@Override
	public SurfaceDescriptor<Shape> sampleSurfaceAreaFacing(Point3D neighbor, Sample sample) {
		
		final Vector3D neighbor_local = Vector3D.from(worldToLocal(neighbor));
		
		final Vector3D J;
		if (neighbor_local.getMagnitude() == 0d)
			J = Vector3D.J;
		else
			J = neighbor_local.normalize();
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
		final Vector3D samplePointToNeighbor_local = neighbor_local.subtract(samplePoint_local);
		final Normal3D normal_local = Normal3D.from(samplePointToNeighbor_local.normalize());
		return localToWorld(new SurfaceDescriptor<>(this, Point3D.from(samplePoint_local), normal_local,
				getParamFromLocalSurface(Point3D.from(samplePoint_local))));
	}
	
	@Override
	public double pdf_sampleSurfaceAreaFacing(Point3D neighbor, Sample sample, SurfaceDescriptor<?> surface) {
		
		//
		// We don't sample the whole Sphere evenly, but only that hemisphere facing the
		// neighboring point.
		return 1d / (2d * PI);
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
	
	@Override
	public boolean canSampleSolidAngleFrom() {
		
		return true;
	}
	
	@Override
	public Duo<Vector3D, Double> sampleSolidAngleFrom(SurfaceDescriptor<?> neighbor, Sample sample) {
		
		final var rnd = sample.getAdditional2DSample();
		
		final var toLightV = Vector3D.from(neighbor.getPoint(), getObjectZero());
		final var distance = toLightV.getMagnitude();
		
		//
		// Pick a direction within the solid-angle subtended by this sphere, expressed
		// in polar coordinates, with
		final var q = sqrt(1d - pow(getRadius() / distance, 2));
		final var theta = acos(1d - rnd.getX() + rnd.getX() * q);
		final var phi = 2d * PI * rnd.getY();
		
		//
		// Construct a Cartesian basis with w oriented from the neighboring point toward
		// the sphere's center.
		final var w = toLightV.normalize();
		final var v = w.orthogonal();
		final var u = w.crossProduct(v);
		
		//
		// Convert these polar-coordinates to (local) Cartesian coordinates.
		final var y_l = cos(phi) * sin(theta);
		final var x_l = cos(theta);
		final var z_l = sin(phi) * sin(theta);
		
		//
		// Convert these local- to world-coordinates using that basis.
		final var worldV = w.multiply(x_l).add(v.multiply(y_l)).add(u.multiply(z_l)).normalize();
		
		return new Duo<>(worldV, pdf_sampleSolidAngleFrom(neighbor, worldV));
	}
	
	@Override
	public double pdf_sampleSolidAngleFrom(SurfaceDescriptor<?> neighbor, Vector3D direction) {
		
		//
		// If this direction simply doesn't intersect this sphere at all, then there's
		// no possibility of selecting that direction.
		
		final var intersection = getSurface(new Ray(neighbor.getPoint(), direction));
		if (intersection == null)
			return 0.0;
		
		final var fromCenterV = Vector3D.from(getObjectZero(), neighbor.getPoint());
		final var centerDistance = fromCenterV.getMagnitude();
		
		final var toIntersectV = Vector3D.from(neighbor.getPoint(), intersection.getPoint());
		
		final var dotProduct = direction.negate().dotProduct(intersection.getNormal());
		
		final var intersectDistanceSq = toIntersectV.getMagnitudeSq();
		
		final var q = sqrt(1d - pow(getRadius() / centerDistance, 2));
		final var d = (2d * PI * intersectDistanceSq * (1d - q));
		
		return dotProduct / d;
	}
	
}
