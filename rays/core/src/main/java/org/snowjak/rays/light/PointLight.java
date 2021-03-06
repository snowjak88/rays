package org.snowjak.rays.light;

import static org.apache.commons.math3.util.FastMath.PI;

import java.util.function.Function;

import org.snowjak.rays.Scene;
import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.annotations.UIType;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.interact.Interactable;
import org.snowjak.rays.interact.Interaction;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.spectrum.Spectrum;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;
import org.snowjak.rays.util.Quad;

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
		this.radiance = (SpectralPowerDistribution) radiance;
	}
	
	@Override
	public boolean isDelta() {
		
		return true;
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
	public <T extends Interactable<T>> Quad<Vector3D, Double, Spectrum, Function<Scene, Boolean>> sample(
			Interaction<T> interaction, Sample sample) {
		
		final var point = interaction.getPoint();
		
		final var s = Vector3D.from(point, position);
		final var distanceSq = s.getMagnitudeSq();
		
		final var visibilityRay = new Ray(point, s);
		return new Quad<>(s.normalize(), 1d / PI, getRadiance().multiply(1d / distanceSq), (scene) -> {
			final var isect = scene.getInteraction(visibilityRay);
			return (isect == null || Vector3D.from(point, isect.getPoint()).getMagnitudeSq() > distanceSq);
		});
	}
	
	@Override
	public <T extends Interactable<T>> double pdf_sample(Interaction<T> interaction, Vector3D w_i, Scene scene) {
		
		return 0.0;
	}
	
}
