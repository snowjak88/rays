package org.snowjak.rays;

import static org.apache.commons.math3.util.FastMath.pow;
import static org.apache.commons.math3.util.FastMath.sqrt;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.material.Material;
import org.snowjak.rays.material.PerfectMirrorMaterial;
import org.snowjak.rays.shape.PlaneShape;
import org.snowjak.rays.shape.SphereShape;
import org.snowjak.rays.transform.RotationTransform;
import org.snowjak.rays.transform.TranslationTransform;

public class SceneTest {
	
	@Test
	public void testSerialize() {
		
		final var scene = new Scene(Arrays.asList(new Primitive(new SphereShape(0.5), (Material) null)));
		final var expected = "{\"primitives\":[{\"shape\":{\"type\":\"sphere\",\"radius\":0.5,\"worldToLocal\":[]}}]}";
		
		final var result = Settings.getInstance().getGson().toJson(scene);
		
		assertEquals(expected, result);
		
	}
	
	@Test
	public void testDeserialize() {
		
		final var json = "{\"primitives\":[{\"shape\":{\"type\":\"sphere\",\"radius\":0.5,\"worldToLocal\":[{\"type\":\"translate\",\"dx\":0},{\"type\":\"rotate\",\"axis\":{\"x\":1,\"y\":0,\"z\":0},\"degrees\":90}]}}]}";
		
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
		assertEquals(2, primitive.getShape().getWorldToLocalTransforms().size());
		assertTrue(primitive.getShape().getWorldToLocalTransforms().get(0) instanceof TranslationTransform);
		assertTrue(primitive.getShape().getWorldToLocalTransforms().get(1) instanceof RotationTransform);
		
		assertNull(primitive.getMaterial());
	}
	
	@Test
	public void testPlanes() {
		
		final var horizontalPlane = new Primitive(new PlaneShape(), new PerfectMirrorMaterial());
		final var verticalPlane = new Primitive(
				new PlaneShape(new TranslationTransform(0, 0, +2), new RotationTransform(Vector3D.I, 90)),
				new PerfectMirrorMaterial());
		
		final var scene = new Scene(Arrays.asList(horizontalPlane, verticalPlane));
		
		//
		
		final var ray1 = new Ray(new Point3D(0, 2, -2), new Vector3D(0, -1, +1).normalize());
		final var interact1 = scene.getInteraction(ray1);
		
		assertNotNull("Interaction 1: no interaction found!", interact1);
		assertEquals("Interaction 1: did not interact with expected primitive!", horizontalPlane,
				interact1.getInteracted());
		assertEquals("Interaction 1: ray-T was not as expected!", sqrt(pow(2.0, 2) + pow(2.0, 2)),
				interact1.getInteractingRay().getT(), 0.01);
		
		assertEquals("Interaction 1: interacted point(X) not as expected!", 0.0, interact1.getPoint().getX(), 0.00001);
		assertEquals("Interaction 1: interacted point(Y) not as expected!", 0.0, interact1.getPoint().getY(), 0.00001);
		assertEquals("Interaction 1: interacted point(Z) not as expected!", 0.0, interact1.getPoint().getZ(), 0.00001);
		
		assertEquals("Interaction 1: interacted normal(X) not as expected!", 0.0, interact1.getNormal().getX(),
				0.00001);
		assertEquals("Interaction 1: interacted normal(Y) not as expected!", 1.0, interact1.getNormal().getY(),
				0.00001);
		assertEquals("Interaction 1: interacted normal(Z) not as expected!", 0.0, interact1.getNormal().getZ(),
				0.00001);
		
		//
		
		final var ray2 = new Ray(new Point3D(0, 4, 0), new Vector3D(0, -1, +1).normalize());
		final var interact2 = scene.getInteraction(ray2);
		
		assertNotNull("Interaction 2: no interaction found!", interact2);
		assertEquals("Interaction 2: did not interact with expected primitive!", verticalPlane,
				interact2.getInteracted());
		assertEquals("Interaction 2: ray-T was not as expected!", sqrt(pow(2.0, 2) + pow(2.0, 2)),
				interact2.getInteractingRay().getT(), 0.01);
		
		assertEquals("Interaction 2: interacted point(X) not as expected!", 0.0, interact2.getPoint().getX(), 0.00001);
		assertEquals("Interaction 2: interacted point(Y) not as expected!", 2.0, interact2.getPoint().getY(), 0.00001);
		assertEquals("Interaction 2: interacted point(Z) not as expected!", 2.0, interact2.getPoint().getZ(), 0.00001);
		
		assertEquals("Interaction 2: interacted normal(X) not as expected!", 0.0, interact2.getNormal().getX(),
				0.00001);
		assertEquals("Interaction 2: interacted normal(Y) not as expected!", 0.0, interact2.getNormal().getY(),
				0.00001);
		assertEquals("Interaction 2: interacted normal(Z) not as expected!", -1.0, interact2.getNormal().getZ(),
				0.00001);
		
		//
		
		final var ray3 = new Ray(new Point3D(0, 2, -4), new Vector3D(0, -1, +1).normalize());
		final var interact3 = scene.getInteraction(ray3);
		
		assertNotNull("Interaction 3: no interaction found!", interact1);
		assertEquals("Interaction 3: did not interact with expected primitive!", horizontalPlane,
				interact3.getInteracted());
		assertEquals("Interaction 3: ray-T was not as expected!", sqrt(pow(2.0, 2) + pow(2.0, 2)),
				interact3.getInteractingRay().getT(), 0.01);
		
		assertEquals("Interaction 3: interacted point(X) not as expected!", 0.0, interact3.getPoint().getX(), 0.00001);
		assertEquals("Interaction 3: interacted point(Y) not as expected!", 0.0, interact3.getPoint().getY(), 0.00001);
		assertEquals("Interaction 3: interacted point(Z) not as expected!", -2.0, interact3.getPoint().getZ(), 0.00001);
		
		assertEquals("Interaction 3: interacted normal(X) not as expected!", 0.0, interact3.getNormal().getX(),
				0.00001);
		assertEquals("Interaction 3: interacted normal(Y) not as expected!", 1.0, interact3.getNormal().getY(),
				0.00001);
		assertEquals("Interaction 3: interacted normal(Z) not as expected!", 0.0, interact3.getNormal().getZ(),
				0.00001);
	}
	
}
