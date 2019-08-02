package org.snowjak.rays.light;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.Scene;
import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.annotations.UIType;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.interact.Interactable;
import org.snowjak.rays.interact.Interaction;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;

/**
 * Represents a point light-source -- a non-physical {@link Light} with a
 * location and no size.
 * 
 * @author snowjak88
 *
 */
@UIType(type = "point", fields = { @UIField(name = "position", type = Point3D.class),
		@UIField(name = "radiance", type = SpectralPowerDistribution.class) })
public class PointLight implements Light {
	
	private Point3D position;
	private SpectralPowerDistribution radiance = null;
	
	/**
	 * @param position
	 *            world-space coordinates
	 * @param radiance
	 *            W m^-2 sr^-1
	 */
	public PointLight(Point3D position, SpectralPowerDistribution radiance) {
		
		this.position = position;
		this.radiance = radiance;
	}
	
	public Point3D getPosition() {
		
		return position;
	}
	
	/**
	 * Get the radiance (W m^-2 sr^-1) yielded by this light.
	 * 
	 * @return
	 */
	protected SpectralPowerDistribution getRadiance() {
		
		return radiance;
	}
	
	@Override
	public <T extends Interactable<T>> LightSample sample(Interaction<T> interaction, Sample sample) {
		
		return new LightSample(position, Vector3D.from(position, interaction.getPoint()).normalize(),
				1d / (4d * FastMath.PI), getRadiance());
	}
	
	@Override
	public <T extends Interactable<T>> LightSample sample(Interaction<T> interaction, Ray sampleDirection,
			Scene scene) {
		
		return new LightSample(getPosition(), sampleDirection.getDirection().negate(), 0.0, getRadiance());
	}
	
}
