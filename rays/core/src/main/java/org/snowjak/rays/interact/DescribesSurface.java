package org.snowjak.rays.interact;

import org.snowjak.rays.geometry.Point2D;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.transform.Transformable;

/**
 * Denotes that an object can report {@link SurfaceDescriptor}s for various
 * points on itself.
 * 
 * @author snowjak88
 */
public interface DescribesSurface extends Transformable {
	
	/**
	 * Returns <code>true</code> if the given {@link Ray} at least comes
	 * <em>close</em> to intersecting with this surface. (This is intended to be a
	 * fast method of culling Rays which come nowhere near the surface, leaving
	 * {@link #getSurface(Ray)} to describe intersections more accurately, if they
	 * exist at all.)
	 * 
	 * @param ray
	 * @return
	 */
	public boolean isIntersectableWith(Ray ray);
	
	/**
	 * Given a {@link Ray} (considered to be in the global reference-frame),
	 * determine if the Ray intersects with this surface and, if it does, construct
	 * the resulting {@link SurfaceDescriptor}. If not, return <code>null</code>.
	 * 
	 * @param ray
	 * @return
	 */
	public <T extends DescribesSurface> SurfaceDescriptor<T> getSurface(Ray ray);
	
	/**
	 * Given a <code>neighbor</code>ing point in 3-space, select the point on the
	 * surface of this object closest to that neighboring point.
	 * 
	 * @param neighbor
	 * @return
	 */
	public <T extends DescribesSurface> SurfaceDescriptor<T> getSurfaceNearestTo(Point3D neighbor);
	
	/**
	 * Sample a point from the surface of this object.
	 * 
	 * @param sample
	 * @return
	 */
	public <T extends DescribesSurface> SurfaceDescriptor<T> sampleSurface(Sample sample);
	
	/**
	 * Sample a point from the surface of this object such that the sampled point is
	 * "near" the specified <code>neighbor</code>ing point.
	 * 
	 * @param neighbor
	 * @param sample
	 * @return
	 */
	public <T extends DescribesSurface> SurfaceDescriptor<T> sampleSurfaceFacing(Point3D neighbor, Sample sample);
	
	/**
	 * Given a neighboring point <code>viewedFrom</code>, compute the solid-angle
	 * that this object spans as seen from that point.
	 * 
	 * @param viewedFrom
	 * @return
	 */
	public double computeSolidAngle(Point3D viewedFrom);
	
	/**
	 * Given a 3-D point (in object-local coordinates) on this surface, compute the
	 * equivalent 2-D surface-parameters.
	 * 
	 * @param surface
	 * @return
	 */
	public Point2D getParamFromLocalSurface(Point3D point);
}
