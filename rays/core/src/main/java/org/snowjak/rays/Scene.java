package org.snowjak.rays;

import org.snowjak.rays.acceleration.AccelerationStructure;
import org.snowjak.rays.camera.Camera;

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
	
	private final AccelerationStructure accelerationStructure;
	private final Camera camera;
	
	public Scene(AccelerationStructure accelerationStructure, Camera camera) {
		
		this.accelerationStructure = accelerationStructure;
		this.camera = camera;
	}
	
	public AccelerationStructure getAccelerationStructure() {
		
		return accelerationStructure;
	}
	
	public Camera getCamera() {
		
		return camera;
	}
}
