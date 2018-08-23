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
	
	private double focalLength;
	private transient Vector3D focalPoint;
	
	public PinholeCamera(double pixelWidth, double pixelHeight, double worldWidth, double worldHeight,
			double focalLength) {
		
		this(pixelWidth, pixelHeight, worldWidth, worldHeight, focalLength, Collections.emptyList());
	}
	
	public PinholeCamera(double pixelWidth, double pixelHeight, double worldWidth, double worldHeight,
			double focalLength, Transform... worldToLocal) {
		
		this(pixelWidth, pixelHeight, worldWidth, worldHeight, focalLength, Arrays.asList(worldToLocal));
	}
	
	public PinholeCamera(double pixelWidth, double pixelHeight, double worldWidth, double worldHeight,
			double focalLength, Collection<Transform> worldToLocal) {
		
		super(pixelWidth, pixelHeight, worldWidth, worldHeight, worldToLocal);
		this.focalLength = focalLength;
	}
	
	@Override
	public TracedSample trace(Sample sample) {
		
		if (focalPoint == null)
			focalPoint = new Vector3D(0, 0, focalLength);
		
		final var imagePlanePoint = new Point3D(getXConverter().apply(sample.getFilmPoint().getX()),
				-(getXConverter().apply(sample.getFilmPoint().getY())), 0);
		final var direction = focalPoint.subtract(imagePlanePoint).normalize();
		
		return new TracedSample(sample, localToWorld(new Ray(imagePlanePoint, direction)));
	}
	
	public double getFocalLength() {
		
		return focalLength;
	}
	
}
