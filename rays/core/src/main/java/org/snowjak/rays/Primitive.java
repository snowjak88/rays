package org.snowjak.rays;

import java.util.List;

import org.snowjak.rays.geometry.Point2D;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.interact.Interactable;
import org.snowjak.rays.interact.Interaction;
import org.snowjak.rays.interact.SurfaceDescriptor;
import org.snowjak.rays.material.Material;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.shape.Shape;
import org.snowjak.rays.transform.Transform;
import org.snowjak.rays.transform.Transformable;

public class Primitive implements Interactable<Primitive>, Transformable {
	
	private final Shape shape;
	private final Material material;
	
	public Primitive(Shape shape, Material material) {
		
		this.shape = shape;
		this.material = material;
	}
	
	public Shape getShape() {
		
		return shape;
	}
	
	public Material getMaterial() {
		
		return material;
	}
	
	@Override
	public List<Transform> getWorldToLocalTransforms() {
		
		return shape.getWorldToLocalTransforms();
	}
	
	@Override
	public List<Transform> getLocalToWorldTransforms() {
		
		return shape.getLocalToWorldTransforms();
	}
	
	@Override
	public void appendTransform(Transform transform) {
		
		shape.appendTransform(transform);
	}
	
	@Override
	public boolean isIntersectableWith(Ray ray) {
		
		return shape.isIntersectableWith(ray);
	}
	
	@Override
	public SurfaceDescriptor<Primitive> getSurface(Ray ray) {
		
		final var sd = shape.getSurface(ray);
		if (sd == null)
			return null;
		
		return new SurfaceDescriptor<>(this, sd);
	}
	
	@Override
	public SurfaceDescriptor<Primitive> getSurfaceNearestTo(Point3D neighbor) {
		
		return new SurfaceDescriptor<>(this, shape.getSurfaceNearestTo(neighbor));
	}
	
	@Override
	public SurfaceDescriptor<Primitive> sampleSurface(Sample sample) {
		
		return new SurfaceDescriptor<>(this, shape.sampleSurface(sample));
	}
	
	@Override
	public SurfaceDescriptor<Primitive> sampleSurfaceFacing(Point3D neighbor, Sample sample) {
		
		return new SurfaceDescriptor<>(this, shape.sampleSurfaceFacing(neighbor, sample));
	}
	
	@Override
	public double computeSolidAngle(Point3D viewedFrom) {
		
		return shape.computeSolidAngle(viewedFrom);
	}
	
	@Override
	public Point2D getParamFromLocalSurface(Point3D point) {
		
		return shape.getParamFromLocalSurface(point);
	}
	
	@Override
	public Interaction<Primitive> getInteraction(Ray ray) {
		
		final var sd = getSurface(ray);
		if (sd == null)
			return null;
		
		return new Interaction<Primitive>(this, ray, sd);
	}
	
}
