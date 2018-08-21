package org.snowjak.rays.transform;

import java.util.Iterator;
import java.util.List;

import org.snowjak.rays.geometry.Normal3D;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.interact.DescribesSurface;
import org.snowjak.rays.interact.Interactable;
import org.snowjak.rays.interact.Interaction;
import org.snowjak.rays.interact.SurfaceDescriptor;

/**
 * Indicates that an object is associated with one or more {@link Transform}s,
 * and can use those Transforms to convert points in its object-local
 * coordinate-space to world/global and vice versa.
 * 
 * @author snowjak88
 */
public interface Transformable {
	
	/**
	 * Get the Transforms currently affecting this Transformable, in the order such
	 * that an {@link Iterator} traversing the {@link List} will correctly give the
	 * Transformable's orientation in local coordinates.
	 */
	public List<Transform> getWorldToLocalTransforms();
	
	/**
	 * Get the Transforms currently affecting this Transformable, in the order such
	 * that an {@link Iterator} traversing the {@link List} will correctly give the
	 * Transformable's orientation in world coordinates.
	 */
	public List<Transform> getLocalToWorldTransforms();
	
	/**
	 * Add the given {@link Transform} to the end of the list of world-to-local
	 * Transforms (and implicitly to the beginning of the corresponding
	 * local-to-world list).
	 * 
	 * @param transform
	 */
	public void appendTransform(Transform transform);
	
	/**
	 * Transform the given Point3D from world- to this-object-local coordinates.
	 * 
	 * @param point
	 * @return
	 */
	public default Point3D worldToLocal(Point3D point) {
		
		Point3D working = point;
		for (Transform t : getWorldToLocalTransforms())
			working = t.worldToLocal(working);
		
		return working;
	}
	
	/**
	 * Transform the given Point3D from this-object-local to world-coordinates.
	 * 
	 * @param point
	 * @return
	 */
	public default Point3D localToWorld(Point3D point) {
		
		Point3D working = point;
		for (Transform t : getLocalToWorldTransforms())
			working = t.localToWorld(working);
		
		return working;
	}
	
	/**
	 * Transform the given Vector3D from world- to this-object-local coordinates.
	 * 
	 * @param vector
	 * @return
	 */
	public default Vector3D worldToLocal(Vector3D vector) {
		
		Vector3D working = vector;
		for (Transform t : getWorldToLocalTransforms())
			working = t.worldToLocal(working);
		
		return working;
	}
	
	/**
	 * Transform the given Vector3D from this-object-local to world-coordinates.
	 * 
	 * @param vector
	 * @return
	 */
	public default Vector3D localToWorld(Vector3D vector) {
		
		Vector3D working = vector;
		for (Transform t : getLocalToWorldTransforms())
			working = t.localToWorld(working);
		
		return working;
	}
	
	/**
	 * Transform the given Ray from world- to this-object-local coordinates.
	 * 
	 * @param ray
	 * @return
	 */
	public default Ray worldToLocal(Ray ray) {
		
		Ray working = ray;
		for (Transform t : getWorldToLocalTransforms())
			working = t.worldToLocal(working);
		
		return working;
	}
	
	/**
	 * Transform the given Ray from this-object-local to world-coordinates.
	 * 
	 * @param ray
	 * @return
	 */
	public default Ray localToWorld(Ray ray) {
		
		Ray working = ray;
		for (Transform t : getLocalToWorldTransforms())
			working = t.localToWorld(working);
		
		return working;
	}
	
	/**
	 * Transform the given Normal3D from world- to this-object-local coordinates.
	 * 
	 * @param normal
	 * @return
	 */
	public default Normal3D worldToLocal(Normal3D normal) {
		
		Normal3D working = normal;
		for (Transform t : getWorldToLocalTransforms())
			working = t.worldToLocal(working);
		
		return working;
	}
	
	/**
	 * Transform the given Normal3D from this-object-local to world-coordinates.
	 * 
	 * @param normal
	 * @return
	 */
	public default Normal3D localToWorld(Normal3D normal) {
		
		Normal3D working = normal;
		for (Transform t : getLocalToWorldTransforms())
			working = t.localToWorld(working);
		
		return working;
	}
	
	/**
	 * Transform the given SurfaceDescriptor from world- to this-object-local
	 * coordinates.
	 * 
	 * @param surfaceDescriptor
	 * @return
	 */
	public default <S extends DescribesSurface<S>> SurfaceDescriptor<S> worldToLocal(
			SurfaceDescriptor<S> surfaceDescriptor) {
		
		SurfaceDescriptor<S> working = surfaceDescriptor;
		for (Transform t : getWorldToLocalTransforms())
			working = t.worldToLocal(working);
		
		return working;
	}
	
	/**
	 * Transform the given SurfaceDescriptor from this-object-local to
	 * world-coordinates.
	 * 
	 * @param surfaceDescriptor
	 * @return
	 */
	public default <S extends DescribesSurface<S>> SurfaceDescriptor<S> localToWorld(
			SurfaceDescriptor<S> surfaceDescriptor) {
		
		SurfaceDescriptor<S> working = surfaceDescriptor;
		for (Transform t : getLocalToWorldTransforms())
			working = t.localToWorld(working);
		
		return working;
	}
	
	/**
	 * Transform the given Interaction from world- to this-object-local coordinates.
	 * 
	 * @param interaction
	 * @return
	 */
	public default <I extends Interactable<I>> Interaction<I> worldToLocal(Interaction<I> interaction) {
		
		Interaction<I> working = interaction;
		for (Transform t : getWorldToLocalTransforms())
			working = t.worldToLocal(working);
		
		return working;
	}
	
	/**
	 * Transform the given Interaction from this-object-local to world-coordinates.
	 * 
	 * @param interaction
	 * @return
	 */
	public default <I extends Interactable<I>> Interaction<I> localToWorld(Interaction<I> interaction) {
		
		Interaction<I> working = interaction;
		for (Transform t : getLocalToWorldTransforms())
			working = t.localToWorld(working);
		
		return working;
	}
	
	/**
	 * Compute the world-coordinates for the center (<code>{0,0,0}</code>) of this
	 * object's coordinate system.
	 * 
	 * @return
	 */
	public default Point3D getObjectZero() {
		
		return localToWorld(Point3D.ZERO);
	}
}
