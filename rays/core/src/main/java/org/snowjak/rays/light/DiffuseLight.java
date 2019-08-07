/**
 * 
 */
package org.snowjak.rays.light;

import java.util.function.Function;

import org.snowjak.rays.Primitive;
import org.snowjak.rays.Scene;
import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.annotations.UIType;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.interact.Interactable;
import org.snowjak.rays.interact.Interaction;
import org.snowjak.rays.material.EmissionMaterial;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.shape.Shape;
import org.snowjak.rays.spectrum.Spectrum;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;
import org.snowjak.rays.util.Quad;

/**
 * A DiffuseLight occupies a definite region of space defined by a
 * {@link Shape}. Radiance is emitted evenly across the surface of the Shape, in
 * the direction of the defined surface-normal.
 * <p>
 * By default, a DiffuseLight is configured to be "visible" -- i.e., its shape
 * can participate in ray-intersections, and it can be viewed directly by the
 * observer. If set to "not visible", then this DiffuseLight will only
 * contribute radiance to other surfaces, and will not itself be directly
 * visible.
 * </p>
 * 
 * @author snowjak88
 *
 */
@UIType(type = "diffuse", fields = { @UIField(name = "shape", type = Shape.class),
		@UIField(name = "radiance", type = SpectralPowerDistribution.class),
		@UIField(name = "visible", type = Boolean.class, defaultValue = "true") })
public class DiffuseLight implements Light {
	
	private Shape shape;
	private SpectralPowerDistribution radiance = null;
	private boolean visible = true;
	
	private transient Primitive primitive = null;
	
	/**
	 * Define a new DiffuseLight, with the given {@link Shape} emitting
	 * {@code radiance} W m^-2 sr^-1 in all directions.
	 * 
	 * @param shape
	 * @param radiance
	 */
	public DiffuseLight(Shape shape, SpectralPowerDistribution radiantIntensity) {
		
		this(shape, radiantIntensity, true);
	}
	
	/**
	 * Define a new DiffuseLight, with the given {@link Shape} emitting
	 * {@code radiance} W m^-2 sr^-1 in all directions.
	 * 
	 * @param shape
	 * @param radiance
	 * @param visible
	 */
	public DiffuseLight(Shape shape, SpectralPowerDistribution radiance, boolean visible) {
		
		if (!shape.canSampleSolidAngleFrom())
			throw new IllegalArgumentException(
					"Cannot instantiate a DiffuseLight using a Shape that doesn't support sampling its solid-angle!");
		
		this.shape = shape;
		this.radiance = radiance;
		this.visible = visible;
	}
	
	@Override
	public <T extends Interactable<T>> Quad<Vector3D, Double, Spectrum, Function<Scene, Boolean>> sample(
			Interaction<T> interaction, Sample sample) {
		
		final var s = getPrimitive().sampleSolidAngleFrom(interaction, sample);
		
		final var lightIntersect = getPrimitive().getInteraction(new Ray(interaction.getPoint(), s.getA()));
		final var surfaceDot = s.getA().negate().dotProduct(lightIntersect.getNormal());
		final var fromLightV = Vector3D.from(lightIntersect.getPoint(), interaction.getPoint());
		final var distanceSq = fromLightV.getMagnitudeSq();
		
		final var visibilityRay = new Ray(interaction.getPoint(), s.getA());
		
		return new Quad<>(s.getA(), s.getB(), getRadiance().multiply(surfaceDot / distanceSq), (scene) -> {
			final var isect = scene.getInteraction(visibilityRay, this);
			return (isect == null
					|| Vector3D.from(interaction.getPoint(), isect.getPoint()).getMagnitudeSq() > distanceSq);
		});
	}
	
	@Override
	public <T extends Interactable<T>> double pdf_sample(Interaction<T> interaction, Vector3D w_i, Scene scene) {
		
		return getPrimitive().pdf_sampleSolidAngleFrom(interaction, w_i);
	}
	
	/**
	 * Get the {@link Primitive} representing this DiffuseLight in physical terms
	 * (i.e., {@link Shape} plus {@link EmissionMaterial}).
	 * 
	 * @return
	 */
	public Primitive getPrimitive() {
		
		if (primitive == null)
			primitive = new Primitive(shape, new EmissionMaterial(SpectralPowerDistribution.BLACK));
		
		return primitive;
	}
	
	/**
	 * Whether this DiffuseLight is configured to be visible -- i.e., if its
	 * Primitive ({@link #getPrimitive()}) is directly visible to the Scene's
	 * Camera.
	 *
	 */
	public boolean isVisible() {
		
		return this.visible;
	}
	
	/**
	 * Get the radiance of this light (W m^-2 sr^-1).
	 * 
	 * @return
	 */
	protected SpectralPowerDistribution getRadiance() {
		
		return radiance;
	}
	
}
