package org.snowjak.rays.light;

import org.snowjak.rays.Scene;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.interact.Interactable;
import org.snowjak.rays.interact.Interaction;
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
public class PointLight implements Light {
	
	private Point3D position;
	private Spectrum energy;
	
	public PointLight(Point3D position, RGB color) {
		
		this(position, SpectralPowerDistribution.fromRGB(color));
	}
	
	public PointLight(Point3D position, Spectrum energy) {
		
		assert (position != null);
		assert (energy != null);
		
		this.position = position;
		this.energy = energy;
	}
	
	public Point3D getPosition() {
		
		return position;
	}
	
	public Spectrum getEnergy() {
		
		return energy;
	}
	
	@Override
	public <T extends Interactable<T>> Point3D sampleSurface(Interaction<T> interaction) {
		
		return position;
	}
	
	@Override
	public <T extends Interactable<T>> boolean isVisible(Point3D surface, Interaction<T> interaction, Scene scene) {
		
		//
		// Set up a Ray from the Interaction to the surface-point, and see if we have
		// any interactions along that Ray.
		//
		final var iPoint = interaction.getPoint();
		final var lightRay = new Ray(iPoint, Vector3D.from(surface).subtract(iPoint).normalize());
		
		if (scene.getInteraction(lightRay) != null)
			return false;
		
		return true;
	}
	
	@Override
	public <T extends Interactable<T>> Spectrum getRadiance(Point3D surface, Interaction<T> interaction) {
		
		return getEnergy();
	}
	
}
