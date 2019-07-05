/**
 * 
 */
package org.snowjak.rays.light;

import static org.junit.Assert.*;

import org.junit.Test;
import org.snowjak.rays.Settings;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;

/**
 * @author snowjak88
 *
 */
public class PointLightTest {
	
	@Test
	public void testDeserialize() {
		
		final var json = "{\"type\":\"point\", \"position\":{\"x\":0.0,\"y\":1.0,\"z\":2.0}, \"power\":{\"type\":\"blackbody\",\"kelvin\":2500,\"intensity\":100}}";
		final var expected = new PointLight(new Point3D(0.0, 1.0, 2.0),
				SpectralPowerDistribution.fromBlackbody(2500, 100));
		
		final var obj = Settings.getInstance().getGson().fromJson(json, Light.class);
		
		assertNotNull("PointLight was not deserialized from JSON!", obj);
		assertTrue("JSON did not deserialize to an instance of PointLight!", obj instanceof PointLight);
		
		final PointLight pl = (PointLight) obj;
		
		assertNotNull("Deserialized PointLight has no position!", pl.getPosition());
		assertEquals("Deserialized PointLight has unexpected position (X)!", expected.getPosition().getX(),
				pl.getPosition().getX(), 0.00001);
		assertEquals("Deserialized PointLight has unexpected position (Y)!", expected.getPosition().getY(),
				pl.getPosition().getY(), 0.00001);
		assertEquals("Deserialized PointLight has unexpected position (Z)!", expected.getPosition().getZ(),
				pl.getPosition().getZ(), 0.00001);
		
		assertNotNull("PointLight has no power associated with it!", pl.getPower());
		assertEquals(expected.getRgb().getRed(), pl.getRgb().getRed(), 0.00001);
		assertEquals(expected.getRgb().getGreen(), pl.getRgb().getGreen(), 0.00001);
		assertEquals(expected.getRgb().getBlue(), pl.getRgb().getBlue(), 0.00001);
	}
	
}
