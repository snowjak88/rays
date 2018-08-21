package org.snowjak.rays.camera;

import java.util.Collection;

import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.sample.TracedSample;
import org.snowjak.rays.sampler.Sampler;
import org.snowjak.rays.transform.Transform;

/**
 * <p>
 * Models an
 * <a href="https://en.wikipedia.org/wiki/Orthographic_projection">orthographic
 * projection</a> camera. Yields a rendered image where parallel lines are
 * always parallel and perspective is absent.
 * </p>
 * <p>
 * <strong>Note</strong> that Camera instances expect incoming Samples to have
 * their film-points distributed about {@code (0,0,0)}. Ensure that your
 * selected {@link Sampler} is configured appropriately.
 * </p>
 * 
 * @author snowjak88
 * @see Camera
 */
public class OrthographicCamera extends Camera {
	
	public OrthographicCamera() {
		
		super();
	}
	
	public OrthographicCamera(Transform... worldToLocal) {
		
		super(worldToLocal);
	}
	
	public OrthographicCamera(Collection<Transform> worldToLocal) {
		
		super(worldToLocal);
	}
	
	@Override
	public TracedSample trace(Sample sample) {
		
		// In local-coordinates, an orthographic Ray proceeds orthogonally to the
		// image-plane -- i.e., in the direction (0,0,1).
		
		final var localPoint = new Point3D(sample.getFilmPoint().getX(), sample.getFilmPoint().getY(), 0);
		final var localRay = new Ray(localPoint, Vector3D.K);
		
		//
		// Of course, we need to transform this local-coordinate Ray into
		// world-coordinates.
		//
		final var worldRay = localToWorld(localRay);
		
		return new TracedSample(sample, worldRay);
	}
	
}
