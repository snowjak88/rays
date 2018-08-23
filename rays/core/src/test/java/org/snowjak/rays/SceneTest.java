package org.snowjak.rays;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.snowjak.rays.camera.OrthographicCamera;
import org.snowjak.rays.material.Material;
import org.snowjak.rays.shape.SphereShape;

public class SceneTest {
	
	@Test
	public void testSerialize() {
		
		final var scene = new Scene(Arrays.asList(new Primitive(new SphereShape(0.5), (Material) null)),
				new OrthographicCamera(300, 400, 3, 4));
		final var expected = "{\"primitives\":[{\"shape\":{\"type\":\"sphere\",\"radius\":0.5,\"worldToLocal\":[]}}],\"camera\":{\"type\":\"orthographic\",\"pixelWidth\":300.0,\"pixelHeight\":400.0,\"worldWidth\":3.0,\"worldHeight\":4.0,\"worldToLocal\":[]}}";
		
		final var result = Settings.getInstance().getGson().toJson(scene);
		
		assertEquals(expected, result);
		
	}
	
	@Test
	public void testDeserialize() {
		
		final var json = "{\"primitives\":[{\"shape\":{\"type\":\"sphere\",\"radius\":0.5,\"worldToLocal\":[]}}],\"camera\":{\"type\":\"orthographic\",\"pixelWidth\":300.0,\"pixelHeight\":400.0,\"worldWidth\":3.0,\"worldHeight\":4.0,\"worldToLocal\":[]}}";
		
		final var result = Settings.getInstance().getGson().fromJson(json, Scene.class);
		
		assertNotNull(result);
		
		assertNotNull(result.getAccelerationStructure());
		assertNotNull(result.getAccelerationStructure().getPrimitives());
		assertFalse(result.getAccelerationStructure().getPrimitives().isEmpty());
		assertEquals(1, result.getAccelerationStructure().getPrimitives().size());
		
		final var primitive = result.getAccelerationStructure().getPrimitives().iterator().next();
		assertNotNull(primitive.getShape());
		assertTrue(primitive.getShape() instanceof SphereShape);
		assertEquals(0.5, ((SphereShape) primitive.getShape()).getRadius(), 0.00001);
		assertNotNull(primitive.getShape().getWorldToLocalTransforms());
		assertTrue(primitive.getShape().getWorldToLocalTransforms().isEmpty());
		
		assertNull(primitive.getMaterial());
		
		assertNotNull(result.getCamera());
		assertTrue(result.getCamera() instanceof OrthographicCamera);
		assertEquals(300.0, result.getCamera().getPixelWidth(), 0.00001);
		assertEquals(400.0, result.getCamera().getPixelHeight(), 0.00001);
		assertEquals(3.0, result.getCamera().getWorldWidth(), 0.00001);
		assertEquals(4.0, result.getCamera().getWorldHeight(), 0.00001);
	}
	
}
