package org.snowjak.rays.shape;

import static org.apache.commons.math3.util.FastMath.PI;
import static org.apache.commons.math3.util.FastMath.sqrt;

import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.geometry.Vector3D;
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
	
	public Shape() {
		
		this(Collections.emptyList());
	}
	
	public Shape(Transform... worldToLocal) {
		
		this(Arrays.asList(worldToLocal));
	}
	
	public Shape(List<Transform> worldToLocal) {
		
		this.worldToLocal = new LinkedList<>();
		this.localToWorld = new LinkedList<>();
		
		worldToLocal.forEach(t -> this.appendTransform(t));
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
