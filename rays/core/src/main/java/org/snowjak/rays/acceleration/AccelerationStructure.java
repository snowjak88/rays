package org.snowjak.rays.acceleration;

import org.snowjak.rays.Primitive;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.interact.Interaction;

/**
 * An acceleration structure allows for economical {@link Ray}/{@link Primitive}
 * intersection testing by only testing those Primitives that are likely to
 * intersect any given Ray.
 * 
 * @author snowjak88
 *
 */
public interface AccelerationStructure {
	
	/**
	 * Get the closest {@link Interaction} within this acceleration structure with
	 * this Ray.
	 * 
	 * @param ray
	 * @return
	 */
	public Interaction<Primitive> getInteraction(Ray ray);
	
}
