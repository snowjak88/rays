package org.snowjak.rays;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.snowjak.rays.acceleration.AccelerationStructure;
import org.snowjak.rays.acceleration.HierarchicalBoundingBox;
import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.annotations.UIType;
import org.snowjak.rays.camera.Camera;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.interact.Interaction;
import org.snowjak.rays.light.DiffuseLight;
import org.snowjak.rays.light.Light;

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
@UIType(fields = { @UIField(name = "primitives", type = Collection.class, collectedType = Primitive.class),
		@UIField(name = "camera", type = Camera.class),
		@UIField(name = "lights", type = Collection.class, collectedType = Light.class) })
public class Scene {
	
	private Collection<Primitive> primitives = null;
	private transient AccelerationStructure accelerationStructure = null;
	private transient AccelerationStructure physicalLightAccelerationStructure = null;
	private Camera camera;
	private Collection<Light> lights = null;
	
	public Scene(Collection<Primitive> primitives) {
		
		this(primitives, null);
	}
	
	public Scene(Collection<Primitive> primitives, Collection<Light> lights) {
		
		this.primitives = primitives;
		this.lights = lights;
		
	}
	
	public Scene(AccelerationStructure accelerationStructure) {
		
		this(accelerationStructure, null);
	}
	
	public Scene(AccelerationStructure accelerationStructure, Collection<Light> lights) {
		
		this.accelerationStructure = accelerationStructure;
		this.lights = lights;
	}
	
	public AccelerationStructure getAccelerationStructure() {
		
		if (accelerationStructure == null)
			accelerationStructure = new HierarchicalBoundingBox(primitives);
		
		return accelerationStructure;
	}
	
	private AccelerationStructure getPhysicalLightAccelerationStructure() {
		
		if (physicalLightAccelerationStructure == null)
			physicalLightAccelerationStructure = new HierarchicalBoundingBox(
					getLights().parallelStream().filter(l -> l instanceof DiffuseLight).map(l -> (DiffuseLight) l)
							.filter(l -> l.isVisible()).map(l -> l.getPrimitive()).collect(Collectors.toList()));
		
		return physicalLightAccelerationStructure;
	}
	
	public Camera getCamera() {
		
		return camera;
	}
	
	public Collection<Light> getLights() {
		
		if (lights == null)
			return Collections.emptyList();
		
		return lights;
	}
	
	/**
	 * Get the {@link Interaction} closest to the given {@link Ray}'s origin, or
	 * <code>null</code> if no such Interaction exists.
	 * 
	 * @param ray
	 * @return
	 */
	public Interaction<Primitive> getInteraction(Ray ray) {
		
		return getInteraction(ray, null);
	}
	
	/**
	 * Get the {@link Interaction} closest to the given {@link Ray}'s origin
	 * (ignoring the given (physical) Light), or <code>null</code> if no such
	 * Interaction exists.
	 * 
	 * @param ray
	 * @return
	 */
	public Interaction<Primitive> getInteraction(Ray ray, DiffuseLight ignoring) {
		
		final var primitiveInteraction = getAccelerationStructure().getInteraction(ray);
		final var lightInteraction = getPhysicalLightAccelerationStructure().getInteraction(ray,
				(ignoring == null) ? null : ignoring.getPrimitive());
		
		if (primitiveInteraction == null)
			return lightInteraction;
		
		if (lightInteraction == null)
			return primitiveInteraction;
		
		if (primitiveInteraction.getInteractingRay().getT() < lightInteraction.getInteractingRay().getT())
			return primitiveInteraction;
		else
			return lightInteraction;
	}
}
