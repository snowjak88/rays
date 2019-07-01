/**
 * 
 */
package org.snowjak.rays.light;

import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.annotations.UIType;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.interact.Interactable;
import org.snowjak.rays.interact.Interaction;
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
		@UIField(name = "rgb", type = RGB.class) })
public class DiffuseLight implements Light {
	
	private Shape shape;
	private RGB rgb = null;
	private Spectrum power = null;
	
	private transient Spectrum specificPower = null;
	
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
	public DiffuseLight(Shape shape, Spectrum power) {
		
		if (shape.getSurfaceArea() < 0d)
			throw new IllegalArgumentException(
					"Cannot instantiate a DiffuseLight using a Shape with indefinite surface-area!");
		
		this.shape = shape;
		this.power = power;
	}
	
	@Override
	public <T extends Interactable<T>> Point3D sampleSurface(Interaction<T> interaction, Sample sample) {
		
		return shape.sampleSurfaceFacing(interaction.getPoint(), sample).getPoint();
	}
	
	private Spectrum getPower() {
		
		if (power == null)
			if (rgb == null)
				throw new RuntimeException(
						"Cannot get power from a DiffuseLight without any configured spectrum or RGB!");
			else
				power = SpectralPowerDistribution.fromRGB(rgb);
			
		return power;
	}
	
	private Spectrum getSpecificPower() {
		
		if (specificPower == null)
			specificPower = getPower().multiply(1d / shape.getSurfaceArea());
		
		return specificPower;
	}
	
	@Override
	public <T extends Interactable<T>> Spectrum getRadiance(Point3D surface, Interaction<T> interaction) {
		
		return getSpecificPower();
	}
	
}
