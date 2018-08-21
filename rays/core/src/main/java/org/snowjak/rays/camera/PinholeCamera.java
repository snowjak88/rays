package org.snowjak.rays.camera;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.sample.TracedSample;
import org.snowjak.rays.sampler.Sampler;
import org.snowjak.rays.transform.Transform;

/**
 * <p>
 * Models a <a href="https://en.wikipedia.org/wiki/Pinhole_camera_model">pinhole
 * camera</a>.
 * </p>
 * <p>
 * <strong>Note</strong> that, because pinhole-cameras produce inverted images,
 * this PinholeCamera implementation will pre-invert all received {@link Sample}
 * film-points when computing resulting {@link Ray}s.
 * </p>
 * <p>
 * <strong>Note</strong> that Camera instances expect incoming Samples to have
 * their film-points distributed about {@code (0,0,0)}. Ensure that your
 * selected {@link Sampler} is configured appropriately.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class PinholeCamera extends Camera {
	
	private final Vector3D focalPoint;
	
	public PinholeCamera(double focalLength) {
		
		this(focalLength, Collections.emptyList());
	}
	
	public PinholeCamera(double focalLength, Transform... worldToLocal) {
		
		this(focalLength, Arrays.asList(worldToLocal));
	}
	
	public PinholeCamera(double focalLength, Collection<Transform> worldToLocal) {
		
		super(worldToLocal);
		focalPoint = new Vector3D(0, 0, focalLength);
	}
	
	@Override
	public TracedSample trace(Sample sample) {
		
		final var imagePlanePoint = new Point3D(sample.getFilmPoint().getX(), -sample.getFilmPoint().getY(), 0);
		final var direction = focalPoint.subtract(imagePlanePoint).normalize();
		
		return new TracedSample(sample, localToWorld(new Ray(imagePlanePoint, direction)));
	}
	
}
