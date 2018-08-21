package org.snowjak.rays.camera;

import static org.junit.Assert.*;

import java.util.Collections;

import org.junit.Test;
import org.snowjak.rays.geometry.Point2D;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.sample.FixedSample;
import org.snowjak.rays.sampler.PseudorandomSampler;
import org.snowjak.rays.transform.RotationTransform;
import org.snowjak.rays.transform.TranslationTransform;

public class PinholeCameraTest {
	
	@Test
	public void testTrace() {
		
		final PinholeCamera camera = new PinholeCamera(3);
		
		final var sample = new FixedSample(new PseudorandomSampler(0, 0, 1, 1, 1), new Point2D(1, 2), Point2D.ZERO, 0,
				Collections.emptyList(), Collections.emptyList());
		final var result = camera.trace(sample);
		
		assertNotNull(result);
		assertEquals(sample, result.getSample());
		
		assertEquals(1, result.getRay().getOrigin().getX(), 0.00001);
		assertEquals(-2, result.getRay().getOrigin().getY(), 0.00001);
		assertEquals(0, result.getRay().getOrigin().getZ(), 0.00001);
		assertEquals(-0.26726, result.getRay().getDirection().getX(), 0.00001);
		assertEquals(0.53452, result.getRay().getDirection().getY(), 0.00001);
		assertEquals(0.80178, result.getRay().getDirection().getZ(), 0.00001);
	}
	
	@Test
	public void testTrace_transformed() {
		
		final PinholeCamera camera = new PinholeCamera(3, new TranslationTransform(3, -2, 0),
				new RotationTransform(Vector3D.J, -45));
		
		final var sample = new FixedSample(new PseudorandomSampler(0, 0, 1, 1, 1), new Point2D(1, 2), Point2D.ZERO, 0,
				Collections.emptyList(), Collections.emptyList());
		final var result = camera.trace(sample);
		
		assertNotNull(result);
		assertEquals(sample, result.getSample());
		
		assertEquals(3.707, result.getRay().getOrigin().getX(), 0.001);
		assertEquals(-4, result.getRay().getOrigin().getY(), 0.001);
		assertEquals(0.707, result.getRay().getOrigin().getZ(), 0.001);
		
		assertEquals(-0.755925, result.getRay().getDirection().getX(), 0.001);
		assertEquals(0.53452, result.getRay().getDirection().getY(), 0.001);
		assertEquals(0.377963, result.getRay().getDirection().getZ(), 0.001);
	}
	
}
