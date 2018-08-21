package org.snowjak.rays.interact;

import org.snowjak.rays.geometry.Normal3D;
import org.snowjak.rays.geometry.Point2D;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.geometry.Vector3D;

/**
 * An Interaction defines how and where a {@link Ray} interacts with an
 * {@link Interactable}.
 * 
 * @author snowjak88
 */
public class Interaction<T extends Interactable<T>> extends SurfaceDescriptor<T> {
	
	private final Ray interactingRay;
	
	private final Vector3D w_e;
	
	/**
	 * Create a new Interaction at the defined surface point (described by the given
	 * {@link SurfaceDescriptor}). This Interaction is defined as taking place along
	 * the given <code>interactingRay</code> between <code>mintT</code> and
	 * <code>maxT</code>.
	 * 
	 * @param vector
	 * @param normal
	 * @param param
	 */
	public Interaction(T interacted, Ray interactingRay, SurfaceDescriptor<? extends DescribesSurface<?>> surface) {
		
		this(interacted, interactingRay, surface.getPoint(), surface.getNormal(), surface.getParam());
	}
	
	/**
	 * Create a new Interaction at the defined <code>point</code>, with the
	 * specified surface-<code>normal</code> and surface-parameterization. This
	 * Interaction is defined as taking place along the given
	 * <code>interactingRay</code> between <code>mintT</code> and <code>maxT</code>.
	 * 
	 * @param point
	 * @param normal
	 * @param param
	 */
	public Interaction(T interacted, Ray interactingRay, Point3D point, Normal3D normal, Point2D param) {
		
		super(interacted, point, normal, param);
		
		this.interactingRay = interactingRay;
		this.w_e = interactingRay.getDirection().negate().normalize();
	}
	
	/**
	 * Alias for {@link SurfaceDescriptor#getDescribed()}
	 * 
	 * @return the object which is being interacted with
	 */
	public T getInteracted() {
		
		return getDescribed();
	}
	
	/**
	 * @return the {@link Ray} which produced this {@link Interaction}
	 */
	public Ray getInteractingRay() {
		
		return interactingRay;
	}
	
	/**
	 * @return the {@link Vector3D} directed from the point of interaction toward
	 *         the "eye" (i.e., the interacting ray's origin)
	 */
	public Vector3D getW_e() {
		
		return w_e;
	}
	
}
