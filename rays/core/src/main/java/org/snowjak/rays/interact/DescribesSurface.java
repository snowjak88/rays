package org.snowjak.rays.interact;

import org.snowjak.rays.geometry.Point2D;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.util.Duo;

/**
 * Denotes that an object can report {@link SurfaceDescriptor}s for various
 * points on itself.
 * 
 * @author snowjak88
 */
public interface DescribesSurface<D extends DescribesSurface<D>> {
	
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
	public SurfaceDescriptor<D> getSurface(Ray ray);
	
	/**
	 * Given a <code>neighbor</code>ing point in 3-space, select the point on the
	 * surface of this object closest to that neighboring point.
	 * 
	 * @param neighbor
	 * @return
	 */
	public SurfaceDescriptor<D> getSurfaceNearestTo(Point3D neighbor);
	
	/**
	 * Sample a point from the surface of this object.
	 * 
	 * @param sample
	 * @return
	 */
	public SurfaceDescriptor<D> sampleSurfaceArea(Sample sample);
	
	/**
	 * Given a sample of this surface (obtained via
	 * {@link #sampleSurfaceArea(Sample)}), compute the probability that this sample
	 * would have been chosen.
	 * 
	 * @param surface
	 * 
	 * @return
	 */
	public double pdf_sampleSurfaceArea(SurfaceDescriptor<?> surface);
	
	/**
	 * Sample a point from the surface of this object such that the sampled point is
	 * "near" the specified <code>neighbor</code>ing point.
	 * 
	 * @param neighbor
	 * @param sample
	 * @return
	 */
	public SurfaceDescriptor<D> sampleSurfaceAreaFacing(Point3D neighbor, Sample sample);
	
	/**
	 * Given a 3-D point (in object-local coordinates) on this surface, compute the
	 * equivalent 2-D surface-parameters.
	 * 
	 * @param surface
	 * @return
	 */
	public Point2D getParamFromLocalSurface(Point3D point);
	
	/**
	 * Given a sample of this surface (obtained via
	 * {@link #sampleSurfaceAreaFacing(Point3D, Sample)}), compute the probability
	 * that this sample would have been chosen.
	 * 
	 * @param neighbor
	 * @param sample
	 * @param surfaceSample
	 * @return
	 */
	public double pdf_sampleSurfaceAreaFacing(Point3D neighbor, Sample sample, SurfaceDescriptor<?> surfaceSample);
	
	/**
	 * @return true if calls to {@link #sampleSolidAngleFrom(Point3D, Sample)} are
	 *         implemented.
	 */
	public boolean canSampleSolidAngleFrom();
	
	/**
	 * Given an neighboring point, sample a direction within the solid-angle
	 * subtended by this surface.
	 * 
	 * @param neighbor
	 * @param sample
	 * @return {@link Duo}(direction, PDF)
	 */
	public Duo<Vector3D, Double> sampleSolidAngleFrom(SurfaceDescriptor<?> neighbor, Sample sample);
	
	/**
	 * Given a direction originating from the neighboring point, within the
	 * solid-angle subtended by this surface, calculate the probability that this
	 * direction would have been selected.
	 * 
	 * @param neighbor
	 * @param direction
	 * @return PDF
	 */
	public double pdf_sampleSolidAngleFrom(SurfaceDescriptor<?> neighbor, Vector3D direction);
}
