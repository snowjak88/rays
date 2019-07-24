/**
 * 
 */
package org.snowjak.rays.light;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.Primitive;
import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.annotations.UIType;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.interact.Interactable;
import org.snowjak.rays.interact.Interaction;
import org.snowjak.rays.material.EmissionMaterial;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.shape.Shape;
import org.snowjak.rays.spectrum.Spectrum;
import org.snowjak.rays.spectrum.colorspace.RGB;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;

/**
 * A DiffuseLight occupies a definite region of space defined by a
 * {@link Shape}. Radiance is emitted evenly across the surface of the Shape, in
 * the direction of the defined surface-normal.
 * 
 * @author snowjak88
 *
 */
@UIType(type = "diffuse", fields = { @UIField(name = "shape", type = Shape.class),
		@UIField(name = "rgb", type = RGB.class), @UIField(name = "power", type = SpectralPowerDistribution.class),
		@UIField(name = "visible", type = Boolean.class, defaultValue = "true") })
public class DiffuseLight implements Light {
	
	private Shape shape;
	private RGB rgb = null;
	private SpectralPowerDistribution power = null;
	private boolean visible = true;
	
	private transient SpectralPowerDistribution specificPower = null;
	private transient Primitive primitive = null;
	
	public DiffuseLight(Shape shape, RGB rgb) {
		
		this(shape, SpectralPowerDistribution.fromRGB(rgb));
	}
	
	/**
	 * Define a new DiffuseLight, with the given {@link Shape} emitting a total of
	 * {@code power} energy in all directions.
	 * 
	 * @param shape
	 * @param power
	 */
	public DiffuseLight(Shape shape, SpectralPowerDistribution power) {
		
		this(shape, power, true);
	}
	
	/**
	 * Define a new DiffuseLight, with the given {@link Shape} emitting a total of
	 * {@code power} energy in all directions.
	 * 
	 * @param shape
	 * @param power
	 * @param visible
	 */
	public DiffuseLight(Shape shape, SpectralPowerDistribution power, boolean visible) {
		
		if (shape.getSurfaceArea() < 0d)
			throw new IllegalArgumentException(
					"Cannot instantiate a DiffuseLight using a Shape with indefinite surface-area!");
		
		this.shape = shape;
		this.power = power;
		this.visible = visible;
	}
	
	@Override
	public <T extends Interactable<T>> LightSample sampleSurface(Interaction<T> interaction, Sample sample) {
		
		final var p = shape.sampleSurfaceFacing(interaction.getPoint(), sample).getPoint();
		return new LightSample(p, Vector3D.from(p, interaction.getPoint()).normalize(), 1d / (4d * FastMath.PI));
	}
	
	/**
	 * Get the {@link Primitive} representing this DiffuseLight in physical terms
	 * (i.e., {@link Shape} plus {@link EmissionMaterial}).
	 * 
	 * @return
	 */
	public Primitive getPrimitive() {
		
		if (primitive == null)
			primitive = new Primitive(shape, new EmissionMaterial(getSpecificPower()));
		
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
	
	private SpectralPowerDistribution getPower() {
		
		if (power == null)
			if (rgb == null)
				throw new RuntimeException(
						"Cannot get power from a DiffuseLight without any configured spectrum or RGB!");
			else
				power = SpectralPowerDistribution.fromRGB(rgb);
			
		return power;
	}
	
	private SpectralPowerDistribution getSpecificPower() {
		
		if (specificPower == null)
			specificPower = (SpectralPowerDistribution) getPower().multiply(1d / shape.getSurfaceArea());
		
		return specificPower;
	}
	
	@Override
	public <T extends Interactable<T>> Spectrum getRadiance(Point3D surface, Interaction<T> interaction) {
		
		return getSpecificPower();
	}
	
}
