package org.snowjak.rays.camera;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.sample.TracedSample;
import org.snowjak.rays.transform.Transform;
import org.snowjak.rays.transform.Transformable;

/**
 * Models a camera, translating {@link Sample}s into {@link TracedSample}s
 * (i.e., computing the {@link Ray}s resulting from Samples).
 * 
 * @author snowjak88
 *
 */
public abstract class Camera implements Transformable {
	
	private double width, height;
	private transient double halfWidth, halfHeight;
	private LinkedList<Transform> worldToLocal;
	private transient LinkedList<Transform> localToWorld = null;
	
	public Camera(double width, double height) {
		
		this(width, height, Collections.emptyList());
	}
	
	public Camera(double width, double height, Transform... worldToLocal) {
		
		this(width, height, Arrays.asList(worldToLocal));
	}
	
	public Camera(double width, double height, Collection<Transform> worldToLocal) {
		
		this.width = width;
		this.height = height;
		
		this.halfWidth = width / 2d;
		this.halfHeight = height / 2d;
		
		this.worldToLocal = new LinkedList<>();
		worldToLocal.stream().forEach(t -> this.appendTransform(t));
	}
	
	/**
	 * Given a {@link Sample}, compute the resulting {@link Ray} (expressed in
	 * world-coordinates) emanating from the given point on this camera's film and
	 * return it, packaged in a {@link TracedSample}.
	 * 
	 * @param sample
	 * @return
	 */
	public abstract TracedSample trace(Sample sample);
	
	@Override
	public List<Transform> getWorldToLocalTransforms() {
		
		return (List<Transform>) worldToLocal;
	}
	
	@Override
	public List<Transform> getLocalToWorldTransforms() {
		
		if (localToWorld == null) {
			localToWorld = new LinkedList<>(worldToLocal);
			Collections.reverse(localToWorld);
		}
		
		return (List<Transform>) localToWorld;
	}
	
	@Override
	public void appendTransform(Transform transform) {
		
		worldToLocal.addLast(transform);
	}
	
	public double getWidth() {
		
		return width;
	}
	
	public double getHeight() {
		
		return height;
	}
	
	public double getHalfWidth() {
		
		return halfWidth;
	}
	
	public double getHalfHeight() {
		
		return halfHeight;
	}
}
