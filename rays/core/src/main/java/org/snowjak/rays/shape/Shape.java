package org.snowjak.rays.shape;

import static org.apache.commons.math3.util.FastMath.PI;
import static org.apache.commons.math3.util.FastMath.sqrt;

import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.geometry.boundingvolume.AABB;
import org.snowjak.rays.interact.DescribesSurface;
import org.snowjak.rays.transform.Transform;
import org.snowjak.rays.transform.Transformable;

/**
 * Represents anything that has a shape that can be transformed and interacted
 * with.
 * 
 * @author snowjak88
 */
public abstract class Shape implements Transformable, DescribesSurface {
	
	private final Deque<Transform> worldToLocal, localToWorld;
	private AABB worldAabb = null;
	
	public Shape() {
		
		this(null);
	}
	
	public Shape(AABB localAabb) {
		
		this(localAabb, Collections.emptyList());
	}
	
	public Shape(AABB localAabb, Transform... worldToLocal) {
		
		this(localAabb, Arrays.asList(worldToLocal));
	}
	
	public Shape(AABB localAabb, List<Transform> worldToLocal) {
		
		this.worldToLocal = new LinkedList<>();
		this.localToWorld = new LinkedList<>();
		
		worldToLocal.forEach(t -> this.appendTransform(t));
		
		if (localAabb != null)
			this.worldAabb = new AABB(
					localAabb.getCorners().stream().map(p -> localToWorld(p)).collect(Collectors.toList()));
	}
	
	public boolean hasBoundingVolume() {
		
		return (worldAabb != null);
	}
	
	/**
	 * @return this Shape's (world-frame) AABB
	 */
	public AABB getBoundingVolume() {
		
		return worldAabb;
	}
	
	@Override
	public boolean isIntersectableWith(Ray ray) {
		
		// If we don't have a bounding-volume, better go ahead and flag this shape as
		// worth-checking-out.
		if (!hasBoundingVolume())
			return true;
		
		return worldAabb.isIntersecting(worldToLocal(ray));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Transform> getWorldToLocalTransforms() {
		
		return (List<Transform>) worldToLocal;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Transform> getLocalToWorldTransforms() {
		
		return (List<Transform>) localToWorld;
	}
	
	@Override
	public void appendTransform(Transform transform) {
		
		worldToLocal.addLast(transform);
		localToWorld.addFirst(transform);
	}
	
	/**
	 * Given a point (expressed in global coordinates) which this shape is viewed
	 * from, compute the solid angle which this shape subtends as seen from that
	 * Point -- <em>assuming</em> that this shape looks like a sphere.
	 * 
	 * @param viewedFrom
	 * @param sphereRadius
	 * @return
	 */
	protected double computeSolidAngle_sphere(Point3D viewedFrom, double sphereRadius) {
		
		final double d = Vector3D.from(viewedFrom.subtract(getObjectZero())).getMagnitude();
		final double r = sphereRadius;
		
		return 2d * PI * (1d - sqrt(d * d - r * r) / d);
	}
}
