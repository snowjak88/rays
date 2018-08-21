package org.snowjak.rays;

import java.util.List;

import org.snowjak.rays.geometry.Point2D;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.interact.DescribesSurface;
import org.snowjak.rays.interact.Interactable;
import org.snowjak.rays.interact.Interaction;
import org.snowjak.rays.interact.SurfaceDescriptor;
import org.snowjak.rays.material.Material;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.shape.Shape;
import org.snowjak.rays.transform.Transform;

public class Primitive implements Interactable<Primitive> {
	
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
	public <T extends DescribesSurface> SurfaceDescriptor<T> getSurface(Ray ray) {
		
		return shape.getSurface(ray);
	}
	
	@Override
	public <T extends DescribesSurface> SurfaceDescriptor<T> getSurfaceNearestTo(Point3D neighbor) {
		
		return shape.getSurfaceNearestTo(neighbor);
	}
	
	@Override
	public <T extends DescribesSurface> SurfaceDescriptor<T> sampleSurface(Sample sample) {
		
		return shape.sampleSurface(sample);
	}
	
	@Override
	public <T extends DescribesSurface> SurfaceDescriptor<T> sampleSurfaceFacing(Point3D neighbor, Sample sample) {
		
		return shape.sampleSurfaceFacing(neighbor, sample);
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
