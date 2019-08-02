/**
 * 
 */
package org.snowjak.rays.light;

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
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;

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
		
		if (shape.getSurfaceArea() < 0d)
			throw new IllegalArgumentException(
					"Cannot instantiate a DiffuseLight using a Shape with indefinite surface-area!");
		
		this.shape = shape;
		this.radiance = radiance;
		this.visible = visible;
	}
	
	@Override
	public <T extends Interactable<T>> LightSample sample(Interaction<T> interaction, Sample sample) {
		
		final var s = shape.sampleSurfaceFacing(interaction.getPoint(), sample);
		return new LightSample(s.getPoint(), Vector3D.from(s.getPoint(), interaction.getPoint()).normalize(),
				shape.sampleSurfaceFacingP(interaction.getPoint(), sample, s), getRadiance());
	}
	
	@Override
	public <T extends Interactable<T>> LightSample sample(Interaction<T> interaction, Ray sampleDirection,
			Scene scene) {
		
		final var shapeSurface = shape.getSurface(sampleDirection);
		
		if (shapeSurface == null)
			return new LightSample(null, sampleDirection.getDirection().negate(), 0d, getRadiance());
		
		return new LightSample(shapeSurface.getPoint(), sampleDirection.getDirection().negate(),
				shape.sampleSurfaceP(shapeSurface), getRadiance());
	}
	
	/**
	 * Get the {@link Primitive} representing this DiffuseLight in physical terms
	 * (i.e., {@link Shape} plus {@link EmissionMaterial}).
	 * 
	 * @return
	 */
	public Primitive getPrimitive() {
		
		if (primitive == null)
			primitive = new Primitive(shape, new EmissionMaterial(getRadiance()));
		
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
