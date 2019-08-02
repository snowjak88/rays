package org.snowjak.rays.light;

import org.snowjak.rays.Scene;
import org.snowjak.rays.Settings;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.interact.Interactable;
import org.snowjak.rays.interact.Interaction;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.spectrum.Spectrum;

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
	 * Calculate this Light's falloff factor due to distance. Ordinarily equal to 1
	 * / ( distance^2 ), although implementations may opt to override this to, e.g.,
	 * implement infinite light-sources.
	 * 
	 * @param distanceSq
	 *            the square of the distance between the light-point and the
	 *            surface-point.
	 * @return
	 */
	public default double getFalloff(double distanceSq) {
		
		return 1d / distanceSq;
	}
	
	/**
	 * Given an {@link Interaction} somewhere in the {@link Scene}, sample a point
	 * on this Light that may possibly illuminate that Interaction.
	 * 
	 * @param interaction
	 * @return
	 */
	public <T extends Interactable<T>> LightSample sample(Interaction<T> interaction, Sample sample);
	
	/**
	 * Given an {@link Interaction} somewhere in the {@link Scene}, and a
	 * {@link Ray} pointing somewhere in the world, what is the probability that
	 * this Light would select the reverse of this direction along which to send
	 * some radiation?
	 * 
	 * @param <T>
	 * @param interaction
	 * @param lightSurface
	 * @return
	 */
	public <T extends Interactable<T>> LightSample sample(Interaction<T> interaction, Ray sampleDirection, Scene scene);
	
	/**
	 * Determines if the given {@code surface} point on this Light is visible from
	 * the given {@link Interaction} -- i.e., if anything in this {@link Scene}
	 * interferes with direct line-of-sight.
	 * 
	 * @param interaction
	 * @param scene
	 * @return
	 */
	public default <T extends Interactable<T>> boolean isVisible(Point3D surface, Interaction<T> interaction,
			Scene scene) {
		
		//
		// Set up a Ray from the Interaction to the surface-point, and see if we have
		// any interactions along that Ray.
		//
		final var iPoint = interaction.getPoint();
		final var dirInteractionToLight = Vector3D.from(iPoint, surface);
		
		final var lightDistance = dirInteractionToLight.getMagnitude();
		final var lightRay = new Ray(iPoint, dirInteractionToLight.normalize());
		
		final var lightInteraction = scene.getInteraction(lightRay);
		//
		// If there was an interaction along that ray,
		// and the distance to the interaction is less than the distance to the light,
		// then the interaction is occluding the light.
		//
		if (lightInteraction == null)
			return true;
		
		if (lightDistance - lightInteraction.getInteractingRay().getT() >= Settings.getInstance()
				.getDoubleEqualityEpsilon())
			return false;
		
		return true;
	}
	
	/**
	 * Data bean holding a sampled point, direction, and their accompanying PDF.
	 * 
	 * @author snowjak88
	 *
	 */
	public static class LightSample {
		
		private final Point3D point;
		private final Vector3D direction;
		private final double pdf;
		private final Spectrum radiance;
		
		public LightSample(Point3D point, Vector3D direction, double pdf, Spectrum radiance) {
			
			this.point = point;
			this.direction = direction.normalize();
			this.pdf = pdf;
			this.radiance = radiance;
		}
		
		/**
		 * The sampled point on the Light's surface.
		 * 
		 * @return
		 */
		public Point3D getPoint() {
			
			return point;
		}
		
		/**
		 * The normalized direction extending away from the Light's surface, toward the
		 * given neighboring point.
		 * 
		 * @return
		 */
		public Vector3D getDirection() {
			
			return direction;
		}
		
		/**
		 * The PDF accompanying this point and direction.
		 * 
		 * @return
		 */
		public double getPdf() {
			
			return pdf;
		}
		
		/**
		 * The sampled radiance (W m^-2 sr^-1)
		 * 
		 * @return
		 */
		public Spectrum getRadiance() {
			
			return radiance;
		}
		
	}
}
