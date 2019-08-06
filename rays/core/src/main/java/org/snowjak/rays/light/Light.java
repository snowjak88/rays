package org.snowjak.rays.light;

import org.snowjak.rays.Scene;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.interact.Interactable;
import org.snowjak.rays.interact.Interaction;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.spectrum.Spectrum;
import org.snowjak.rays.util.Trio;

/**
 * Defines a light-source.
 * 
 * @author snowjak88
 *
 */
public interface Light {
	
	/**
	 * Indicates whether this Light has no size, and can practically be sampled only
	 * once.
	 * 
	 * @return
	 */
	public default boolean isDelta() {
		
		return false;
	}
	
	/**
	 * Indicates whether this Light-source is infinite in size and power.
	 * 
	 * @return
	 */
	public default boolean isInfinite() {
		
		return false;
	}
	
	/**
	 * Given an {@link Interaction} somewhere in the {@link Scene}, sample a
	 * direction from that point toward this Light that could illuminate that point.
	 * (This method will <em>not</em> test for shadowing.)
	 * <p>
	 * Light sampling is assumed to be used by the rendering equation formulated in
	 * the area-form:
	 * 
	 * <pre>
	 *                                                                                w` .dot. n`
	 * L_d( X, w ) = integral(all X): g(X, X`) p(X, w, w`) Le(X`, w`) (-w` .dot. n) --------------- dA(X`)
	 *                                                                              || X -> X` ||^2
	 * 
	 * X  = surface position
	 * n  =    "    normal
	 * w  = eye-vector
	 * w` = from-light vector
	 * X` = point on light-surface from X along -w`
	 * n` = normal on light-surface at X`
	 * 
	 * g(X,X`)   = geometric. 1 if X is visible from X`, 0 if not
	 * p(X,w,w`) = albedo at X
	 * Le(X`,w`) = radiance emitted from light at X` along w`
	 * </pre>
	 * </p>
	 * <p>
	 * It is assumed that each Light implementation will assume the responsibility
	 * of including the following terms into each sample it returns from this
	 * method. These terms <strong>must</strong> be included in the {@link Spectrum}
	 * returned from this method:
	 * <ol>
	 * <li>{@code w` .dot. n`}</li>
	 * <li>{@code || X -> X`||^2}</li>
	 * </ol>
	 * Certain implementations will omit these terms. For example, a point
	 * light-source would omit the first term (lacking any surface to have a
	 * normal), whereas an "infinite" light-source would omit the second term
	 * (having an infinite distance).
	 * </p>
	 * 
	 * @param interaction
	 * @return {@link Trio}({@code w_i}, PDF, radiance}
	 */
	public <T extends Interactable<T>> Trio<Vector3D, Double, Spectrum> sample(Interaction<T> interaction,
			Sample sample);
	
	/**
	 * Given an {@link Interaction} somewhere in the {@link Scene}, and a direction
	 * {@code w_i} from that point toward this Light, what is the probability that
	 * this Light would send radiance down that direction?
	 * 
	 * @param <T>
	 * @param interaction
	 * @param lightSurface
	 * @return
	 */
	public <T extends Interactable<T>> double pdf_sample(Interaction<T> interaction, Vector3D w_i, Scene scene);
	
	/**
	 * Determines if the given {@link Interaction} can view this Light along the
	 * given direction {@code w_i} -- i.e., if anything in this {@link Scene}
	 * interferes with direct line-of-sight.
	 * 
	 * @param interaction
	 * @param scene
	 * @return
	 */
	public default <T extends Interactable<T>> boolean isVisible(Interaction<T> interaction, Vector3D w_i,
			Scene scene) {
		
		//
		// Set up a Ray from the Interaction along the direction.
		//
		final var iPoint = interaction.getPoint();
		
		final var lightRay = new Ray(iPoint, w_i.normalize());
		
		final DiffuseLight ignore;
		if (DiffuseLight.class.isAssignableFrom(getClass()))
			ignore = (DiffuseLight) this;
		else
			ignore = null;
		
		final var lightInteraction = scene.getInteraction(lightRay, ignore);
		//
		// If there was an interaction along that ray,
		// and the occluding object is not this same light,
		// then the interaction is occluding the light.
		//
		if (lightInteraction != null)
			return false;
		
		return true;
	}
}
