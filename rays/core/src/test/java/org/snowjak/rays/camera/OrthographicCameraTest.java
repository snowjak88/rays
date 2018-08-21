package org.snowjak.rays.camera;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;

import org.junit.Test;
import org.snowjak.rays.geometry.Point2D;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.sample.FixedSample;
import org.snowjak.rays.sampler.PseudorandomSampler;
import org.snowjak.rays.transform.RotationTransform;
import org.snowjak.rays.transform.TranslationTransform;

public class OrthographicCameraTest {
	
	@Test
	public void testTrace() {
		
		final OrthographicCamera camera = new OrthographicCamera();
		
		final var sample = new FixedSample(new PseudorandomSampler(0, 0, 1, 1, 1), new Point2D(1, 2), Point2D.ZERO, 0,
				Collections.emptyList(), Collections.emptyList());
		final var result = camera.trace(sample);
		
		assertNotNull(result);
		assertEquals(sample, result.getSample());
		
		assertEquals(1d, result.getRay().getOrigin().getX(), 0.00001);
		assertEquals(2d, result.getRay().getOrigin().getY(), 0.00001);
		assertEquals(0d, result.getRay().getOrigin().getZ(), 0.00001);
		assertEquals(0d, result.getRay().getDirection().getX(), 0.00001);
		assertEquals(0d, result.getRay().getDirection().getY(), 0.00001);
		assertEquals(1d, result.getRay().getDirection().getZ(), 0.00001);
	}
	
	@Test
	public void testTrace_transformed() {
		
		final OrthographicCamera camera = new OrthographicCamera(new TranslationTransform(1, 1, 0),
				new RotationTransform(Vector3D.J, 45));
		
		final var sample = new FixedSample(new PseudorandomSampler(0, 0, 1, 1, 1), new Point2D(1, 2), Point2D.ZERO, 0,
				Collections.emptyList(), Collections.emptyList());
		final var result = camera.trace(sample);
		
		assertNotNull(result);
		assertEquals(sample, result.getSample());
		
		assertEquals(1.707d, result.getRay().getOrigin().getX(), 0.001);
		assertEquals(3d, result.getRay().getOrigin().getY(), 0.001);
		assertEquals(-0.707d, result.getRay().getOrigin().getZ(), 0.001);
		assertEquals(0.707d, result.getRay().getDirection().getX(), 0.001);
		assertEquals(0d, result.getRay().getDirection().getY(), 0.001);
		assertEquals(0.707d, result.getRay().getDirection().getZ(), 0.001);
	}
}
