package org.snowjak.rays.camera;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;
import org.snowjak.rays.Settings;
import org.snowjak.rays.geometry.Point2D;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.sample.FixedSample;
import org.snowjak.rays.transform.RotationTransform;
import org.snowjak.rays.transform.TranslationTransform;

public class PinholeCameraTest {
	
	@Test
	public void testTrace() {
		
		final PinholeCamera camera = new PinholeCamera(400, 400, 4, 4, 3);
		
		final var sample = new FixedSample(new Point2D(100, 200), Point2D.ZERO, 0, Collections.emptyList(),
				Collections.emptyList());
		final var result = camera.trace(sample);
		
		assertNotNull(result);
		assertEquals(sample, result.getSample());
		
		assertEquals(-1, result.getRay().getOrigin().getX(), 0.00001);
		assertEquals(0, result.getRay().getOrigin().getY(), 0.00001);
		assertEquals(0, result.getRay().getOrigin().getZ(), 0.00001);
		
		assertEquals(0.316228, result.getRay().getDirection().getX(), 0.00001);
		assertEquals(0.0, result.getRay().getDirection().getY(), 0.00001);
		assertEquals(0.948683, result.getRay().getDirection().getZ(), 0.00001);
	}
	
	@Test
	public void testTrace_transformed() {
		
		final PinholeCamera camera = new PinholeCamera(400, 400, 4, 4, 3, new TranslationTransform(3, -2, 0),
				new RotationTransform(Vector3D.J, -45));
		
		final var sample = new FixedSample(new Point2D(100, 200), Point2D.ZERO, 0, Collections.emptyList(),
				Collections.emptyList());
		final var result = camera.trace(sample);
		
		assertNotNull(result);
		assertEquals(sample, result.getSample());
		
		assertEquals(2.293, result.getRay().getOrigin().getX(), 0.001);
		assertEquals(-2, result.getRay().getOrigin().getY(), 0.001);
		assertEquals(-0.707, result.getRay().getOrigin().getZ(), 0.001);
		
		assertEquals(-0.447213, result.getRay().getDirection().getX(), 0.001);
		assertEquals(0.0, result.getRay().getDirection().getY(), 0.001);
		assertEquals(0.894427, result.getRay().getDirection().getZ(), 0.001);
	}
	
	@Test
	public void testSerialize() {
		
		final var camera = new PinholeCamera(4, 4, 4, 4, 3);
		final var expected = "{\"type\":\"pinhole\",\"focalLength\":3.0,\"pixelWidth\":4.0,\"pixelHeight\":4.0,\"worldWidth\":4.0,\"worldHeight\":4.0,\"worldToLocal\":[]}";
		
		final var result = Settings.getInstance().getGson().toJson(camera);
		
		assertEquals(expected, result);
	}
	
	@Test
	public void testDeserialize() {
		
		final var json = "{\"type\":\"pinhole\",\"focalLength\":3.0,\"pixelWidth\":4.0,\"pixelHeight\":4.0,\"worldWidth\":4.0,\"worldHeight\":4.0,\"worldToLocal\":[]}";
		final var expected = new PinholeCamera(4, 4, 4, 4, 3);
		
		final var result = Settings.getInstance().getGson().fromJson(json, Camera.class);
		
		assertNotNull(result);
		
		assertTrue(PinholeCamera.class.isAssignableFrom(result.getClass()));
		
		final var camera = (PinholeCamera) result;
		
		assertEquals(expected.getPixelWidth(), camera.getPixelWidth(), 0.00001);
		assertEquals(expected.getPixelHeight(), camera.getPixelHeight(), 0.00001);
		assertEquals(expected.getWorldWidth(), camera.getWorldWidth(), 0.00001);
		assertEquals(expected.getWorldHeight(), camera.getWorldHeight(), 0.00001);
		assertEquals(expected.getFocalLength(), camera.getFocalLength(), 0.00001);
	}
	
}
