package org.snowjak.rays.camera;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.sample.TracedSample;
import org.snowjak.rays.sampler.Sampler;
import org.snowjak.rays.transform.Transform;
import org.snowjak.rays.transform.Transformable;

/**
 * Models a camera, translating {@link Sample}s into {@link TracedSample}s
 * (i.e., computing the {@link Ray}s resulting from Samples).
 * <p>
 * <strong>Note</strong> that Camera instances expect incoming Samples to have
 * their film-points distributed about {@code (0,0,0)}. Ensure that your
 * selected {@link Sampler} is configured appropriately.
 * </p>
 * 
 * @author snowjak88
 *
 */
public abstract class Camera implements Transformable {
	
	private final Deque<Transform> worldToLocal, localToWorld;
	
	public Camera() {
		
		this(Collections.emptyList());
	}
	
	public Camera(Transform... worldToLocal) {
		
		this(Arrays.asList(worldToLocal));
	}
	
	public Camera(Collection<Transform> worldToLocal) {
		
		this.worldToLocal = new LinkedList<>();
		this.localToWorld = new LinkedList<>();
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
}
