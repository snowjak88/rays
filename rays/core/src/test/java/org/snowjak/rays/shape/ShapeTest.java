package org.snowjak.rays.shape;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.snowjak.rays.geometry.Point2D;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.geometry.boundingvolume.AABB;
import org.snowjak.rays.interact.SurfaceDescriptor;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.transform.RotationTransform;
import org.snowjak.rays.transform.TranslationTransform;

public class ShapeTest {
	
	private Shape shape;
	
	@Before
	public void setUp() {
		
		shape = new Shape(Collections.emptyList()) {
			
			@Override
			public SurfaceDescriptor<Shape> getSurfaceNearestTo(Point3D point) {
				
				// We don't care about this method for the purposes of this
				// test.
				return null;
			}
			
			@Override
			public SurfaceDescriptor<Shape> sampleSurface(Sample sample) {
				
				// We don't care about this method for the purposes of this
				// test.
				return null;
			}
			
			@Override
			public SurfaceDescriptor<Shape> sampleSurfaceFacing(Point3D neighbor, Sample sample) {
				
				// We don't care about this method for the purposes of this
				// test.
				return null;
			}
			
			@Override
			public double computeSolidAngle(Point3D viewedFrom) {
				
				// We don't care about this method for the purposes of this
				// test.
				return 0;
			}
			
			@Override
			public SurfaceDescriptor<Shape> getSurface(Ray ray) {
				
				// We don't care about this method for the purposes of this
				// test.
				return null;
			}
			
			@Override
			public boolean isIntersectableWith(Ray ray) {
				
				// We don't care about this method for the purposes of this
				// test.
				return false;
			}
			
			@Override
			public Point2D getParamFromLocalSurface(Point3D point) {
				
				// We don't care about this method for the purposes of this
				// test.
				return null;
			}
			
			@Override
			public AABB getLocalBoundingVolume() {
				
				// We don't care about this method for the purposes of this
				// test.
				return null;
			}
			
			@Override
			public double getSurfaceArea() {
				
				// We don't care about this method for the purposes of this
				// test.
				return 0;
			}
			
			@Override
			public double sampleSurfaceP(SurfaceDescriptor<?> surface) {
				
				// We don't care about this method for the purposes of this
				// test.
				return 0;
			}
			
			@Override
			public double sampleSurfaceFacingP(Point3D neighbor, Sample sample, SurfaceDescriptor<?> surface) {
				
				// We don't care about this method for the purposes of this
				// test.
				return 0;
			}
		};
	}
	
	@Test
	public void testAppendTransform() {
		
		shape.appendTransform(new TranslationTransform(3d, 0d, 0d));
		shape.appendTransform(new RotationTransform(Vector3D.J, 90d));
		
		//
		// Therefore, a point located at (1,0,0) relative to the object should
		// be located at (3,0,1) relative to the world-origin.
		//
		Point3D relativePoint = new Point3D(1, 0, 0);
		Point3D absolutePoint = shape.localToWorld(relativePoint);
		
		assertEquals("Absolute X is not as expected!", 3d, absolutePoint.getX(), 0.00001);
		assertEquals("Absolute Y is not as expected!", 0d, absolutePoint.getY(), 0.00001);
		assertEquals("Absolute Z is not as expected!", -1d, absolutePoint.getZ(), 0.00001);
	}
	
}
