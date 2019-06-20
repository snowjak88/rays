package org.snowjak.rays.light;

import org.snowjak.rays.Scene;
import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.annotations.UIType;
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
			energy = SpectralPowerDistribution.fromRGB(rgb);
		
		return energy;
	}
	
	public RGB getRgb() {
		
		if (rgb == null)
			rgb = energy.toRGB(true);
		
		return rgb;
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
		final var dirInteractionToLight = Vector3D.from(surface).subtract(iPoint);
		
		final var lightDistance = dirInteractionToLight.getMagnitude();
		final var lightRay = new Ray(iPoint, Vector3D.from(surface).subtract(iPoint).normalize());
		
		final var lightInteraction = scene.getInteraction(lightRay);
		//
		// If there was an interaction along that ray,
		// and the distance to the interaction is less than the distance to the light,
		// then the interaction is occluding the light.
		//
		if (lightInteraction != null && lightDistance > lightInteraction.getInteractingRay().getT())
			return false;
		
		return true;
	}
	
	@Override
	public <T extends Interactable<T>> Spectrum getRadiance(Point3D surface, Interaction<T> interaction) {
		
		return getEnergy();
	}
	
}
