package org.snowjak.rays.interact;

import org.snowjak.rays.geometry.Normal3D;
import org.snowjak.rays.geometry.Point2D;
import org.snowjak.rays.geometry.Point3D;

/**
 * Describes a surface at a specific point.
 * <p>
 * <strong>Note</strong>: Unless explicitly stated, all Points, Vectors, etc.
 * are assumed to be given in terms of global coordinates.
 * 
 * @author snowjak88
 */
public class SurfaceDescriptor<S extends DescribesSurface<S>> {
	
	private final S described;
	private final Point3D point;
	private final Normal3D normal;
	private final Point2D param;
	
	public <D extends DescribesSurface<D>> SurfaceDescriptor(S described, SurfaceDescriptor<D> toCopy) {
		
		this(described, toCopy.getPoint(), toCopy.getNormal(), toCopy.getParam());
	}
	
	public SurfaceDescriptor(S described, Point3D point, Normal3D normal, Point2D param) {
		
		this.described = described;
		this.point = point;
		this.normal = normal;
		this.param = param;
	}
	
	/**
	 * @return the object with the described surface
	 */
	public S getDescribed() {
		
		return described;
	}
	
	/**
	 * @return the point on the surface
	 */
	public Point3D getPoint() {
		
		return point;
	}
	
	/**
	 * @return the surface-normal at the point
	 */
	public Normal3D getNormal() {
		
		return normal;
	}
	
	/**
	 * @return the parameterization <code>(u,v)</code> of the surface
	 */
	public Point2D getParam() {
		
		return param;
	}
	
}
