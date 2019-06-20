package org.snowjak.rays.light;

import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.annotations.UIType;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.interact.Interactable;
import org.snowjak.rays.interact.Interaction;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.spectrum.Spectrum;
import org.snowjak.rays.spectrum.colorspace.RGB;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;

/**
 * Represents a point light-source -- a non-physical {@link Light} with a
 * location and no size.
 * 
 * @author snowjak88
 *
 */
@UIType(type = "point", fields = { @UIField(name = "position", type = Point3D.class),
		@UIField(name = "rgb", type = RGB.class) })
public class PointLight implements Light {
	
	private Point3D position;
	private Spectrum energy = null;
	private RGB rgb = null;
	
	public PointLight(Point3D position, RGB rgb) {
		
		this.position = position;
		this.rgb = rgb;
	}
	
	public PointLight(Point3D position, Spectrum energy) {
		
		this.position = position;
		this.energy = energy;
	}
	
	public Point3D getPosition() {
		
		return position;
	}
	
	public Spectrum getEnergy() {
		
		if (energy == null)
			if (rgb == null)
				throw new RuntimeException(
						"Trying to get energy from a PointLight that has no configured spectrum or RGB!");
			else
				energy = SpectralPowerDistribution.fromRGB(rgb);
			
		return energy;
	}
	
	public RGB getRgb() {
		
		if (rgb == null)
			rgb = energy.toRGB(true);
		
		return rgb;
	}
	
	@Override
	public <T extends Interactable<T>> Point3D sampleSurface(Interaction<T> interaction, Sample sample) {
		
		return position;
	}
	
	@Override
	public <T extends Interactable<T>> Spectrum getRadiance(Point3D surface, Interaction<T> interaction) {
		
		return getEnergy();
	}
	
}
