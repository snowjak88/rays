package org.snowjak.rays.material;

import static org.junit.Assert.*;

import org.junit.Test;
import org.snowjak.rays.Primitive;
import org.snowjak.rays.geometry.Normal3D;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.interact.Interaction;
import org.snowjak.rays.sample.FixedSample;
import org.snowjak.rays.spectrum.colorspace.RGB;
import org.snowjak.rays.texture.ConstantTexture;

public class PerfectMirrorMaterialTest {
	
	@Test
	public void testGetReflectionV() {
		
		final var interaction = new Interaction<Primitive>(null, new Ray(new Point3D(-3, 0, 0), new Vector3D(+1, 0, 0)),
				new Point3D(0, 0, 0), Normal3D.from(new Vector3D(-1, +1, 0).normalize()), null);
		
		final var expectedReflection = new Vector3D(0, +1, 0);
		
		final var result = new PerfectMirrorMaterial(new ConstantTexture(RGB.WHITE))
				.getReflectionV(interaction, new FixedSample()).normalize();
		
		assertEquals(expectedReflection.getX(), result.getX(), 0.00001);
		assertEquals(expectedReflection.getY(), result.getY(), 0.00001);
		assertEquals(expectedReflection.getZ(), result.getZ(), 0.00001);
	}
	
}
