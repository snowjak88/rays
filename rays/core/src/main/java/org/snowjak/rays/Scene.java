package org.snowjak.rays;

import java.util.Collection;

import org.snowjak.rays.acceleration.AccelerationStructure;
import org.snowjak.rays.acceleration.HierarchicalBoundingBox;
import org.snowjak.rays.camera.Camera;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.interact.Interaction;

/**
 * A Scene represents everything to be rendered:
 * <ul>
 * <li>All {@link Primitive}s (housed in a suitable
 * {@link AccelerationStructure})</li>
 * <li>The configured {@link Camera}</li>
 * </ul>
 * 
 * @author snowjak88
 *
 */
public class Scene {
	
	private Collection<Primitive> primitives = null;
	private transient AccelerationStructure accelerationStructure = null;
	private Camera camera;
	
	public Scene(Collection<Primitive> primitives, Camera camera) {
		
		this.primitives = primitives;
		this.camera = camera;
	}
	
	public Scene(AccelerationStructure accelerationStructure, Camera camera) {
		
		this.accelerationStructure = accelerationStructure;
		this.camera = camera;
	}
	
	public AccelerationStructure getAccelerationStructure() {
		
		if (accelerationStructure == null)
			accelerationStructure = new HierarchicalBoundingBox(primitives);
		
		return accelerationStructure;
	}
	
	public Camera getCamera() {
		
		return camera;
	}
	
	/**
	 * Get the {@link Interaction} closest to the given {@link Ray}'s origin, or
	 * <code>null</code> if no such Interaction exists.
	 * 
	 * @param ray
	 * @return
	 */
	public Interaction<Primitive> getInteraction(Ray ray) {
		
		return accelerationStructure.getInteraction(ray);
	}
}
