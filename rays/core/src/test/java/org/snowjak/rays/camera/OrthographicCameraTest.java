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

public class OrthographicCameraTest {
	
	@Test
	public void testTrace() {
		
		final OrthographicCamera camera = new OrthographicCamera(4, 4);
		
		final var sample = new FixedSample(new Point2D(1, 2), Point2D.ZERO, 0, Collections.emptyList(),
				Collections.emptyList());
		final var result = camera.trace(sample);
		
		assertNotNull(result);
		assertEquals(sample, result.getSample());
		
		assertEquals(-1d, result.getRay().getOrigin().getX(), 0.00001);
		assertEquals(0d, result.getRay().getOrigin().getY(), 0.00001);
		assertEquals(0d, result.getRay().getOrigin().getZ(), 0.00001);
		assertEquals(0d, result.getRay().getDirection().getX(), 0.00001);
		assertEquals(0d, result.getRay().getDirection().getY(), 0.00001);
		assertEquals(1d, result.getRay().getDirection().getZ(), 0.00001);
	}
	
	@Test
	public void testTrace_transformed() {
		
		final OrthographicCamera camera = new OrthographicCamera(4, 4, new TranslationTransform(1, 1, 0),
				new RotationTransform(Vector3D.J, 45));
		
		final var sample = new FixedSample(new Point2D(1, 2), Point2D.ZERO, 0, Collections.emptyList(),
				Collections.emptyList());
		final var result = camera.trace(sample);
		
		assertNotNull(result);
		assertEquals(sample, result.getSample());
		
		assertEquals(0.293d, result.getRay().getOrigin().getX(), 0.001);
		assertEquals(1d, result.getRay().getOrigin().getY(), 0.001);
		assertEquals(0.707d, result.getRay().getOrigin().getZ(), 0.001);
		assertEquals(0.707d, result.getRay().getDirection().getX(), 0.001);
		assertEquals(0d, result.getRay().getDirection().getY(), 0.001);
		assertEquals(0.707d, result.getRay().getDirection().getZ(), 0.001);
	}
	
	@Test
	public void testSerialize() {
		
		final var camera = new OrthographicCamera(4, 4);
		final var expected = "{\"type\":\"orthographic\",\"width\":4.0,\"height\":4.0,\"worldToLocal\":[]}";
		
		final var result = Settings.getInstance().getGson().toJson(camera);
		
		assertEquals(expected, result);
	}
	
	@Test
	public void testDeserialize() {
		
		final var json = "{\"type\":\"orthographic\",\"width\":4.0,\"height\":4.0,\"worldToLocal\":[]}";
		final var expected = new OrthographicCamera(4, 4);
		
		final var result = Settings.getInstance().getGson().fromJson(json, Camera.class);
		
		assertNotNull(result);
		
		assertTrue(OrthographicCamera.class.isAssignableFrom(result.getClass()));
		
		final var camera = (OrthographicCamera) result;
		
		assertEquals(expected.getWidth(), camera.getWidth(), 0.00001);
		assertEquals(expected.getHeight(), camera.getHeight(), 0.00001);
	}
}
