package org.snowjak.rays.spectrum.colorspace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.snowjak.rays.Settings;

public class RGBTest {
	
	@Test
	public void testFromHSL_cyan() {
		
		final RGB cyan = RGB.fromHSL(180d, 1d, 0.5);
		assertEquals("Expected-Red not as expected!", 0d, cyan.getRed(), 0.00001);
		assertEquals("Expected-Green not as expected!", 1d, cyan.getGreen(), 0.00001);
		assertEquals("Expected-Blue not as expected!", 1d, cyan.getBlue(), 0.00001);
		
	}
	
	@Test
	public void testFromHSL_orange() {
		
		final RGB orange = RGB.fromHSL(30d, 1d, 0.5);
		assertEquals("Expected-Red not as expected!", 1d, orange.getRed(), 0.00001);
		assertEquals("Expected-Green not as expected!", 0.5d, orange.getGreen(), 0.00001);
		assertEquals("Expected-Blue not as expected!", 0d, orange.getBlue(), 0.00001);
		
	}
	
	@Test
	public void testFromHSL_purple() {
		
		final RGB purple = RGB.fromHSL(270d, 0.5d, 0.5);
		assertEquals("Expected-Red not as expected!", 0.5d, purple.getRed(), 0.00001);
		assertEquals("Expected-green not as expected!", 0.25d, purple.getGreen(), 0.00001);
		assertEquals("Expected-Blue not as expected!", 0.75d, purple.getBlue(), 0.00001);
	}
	
	@Test
	public void testFromPacked_cyan() {
		
		final int packedRgb = 0x00FFFF;
		final RGB unpacked = RGB.fromPacked(packedRgb);
		assertEquals("Expected-Red not as expected!", 0.0, unpacked.getRed(), 1d / 256d);
		assertEquals("Expected-Green not as expected!", 1.0, unpacked.getGreen(), 1d / 256d);
		assertEquals("Expected-Blue not as expected!", 1.0, unpacked.getBlue(), 1d / 256d);
	}
	
	@Test
	public void testFromPacked_olive() {
		
		final int packedRgb = 0x808000;
		final RGB unpacked = RGB.fromPacked(packedRgb);
		assertEquals("Expected-Red not as expected!", 0.5, unpacked.getRed(), 1d / 256d);
		assertEquals("Expected-Green not as expected!", 0.5, unpacked.getGreen(), 1d / 256d);
		assertEquals("Expected-Blue not as expected!", 0.0, unpacked.getBlue(), 1d / 256d);
	}
	
	@Test
	public void testFromPacked_purple() {
		
		final int packedRgb = 0x800080;
		final RGB unpacked = RGB.fromPacked(packedRgb);
		assertEquals("Expected-Red not as expected!", 0.5, unpacked.getRed(), 1d / 256d);
		assertEquals("Expected-Green not as expected!", 0.0, unpacked.getGreen(), 1d / 256d);
		assertEquals("Expected-Blue not as expected!", 0.5, unpacked.getBlue(), 1d / 256d);
	}
	
	@Test
	public void testConvertToXYZ_gray() {
		
		final var xyz = new RGB(0.5, 0.5, 0.5).to(XYZ.class);
		
		assertEquals("XYZ (X) is not as expected!", 0.20344, xyz.getX(), 0.0001);
		assertEquals("XYZ (Y) is not as expected!", 0.21404, xyz.getY(), 0.0001);
		assertEquals("XYZ (Z) is not as expected!", 0.23305, xyz.getZ(), 0.0001);
	}
	
	@Test
	public void testConvertToXYZ_red() {
		
		final var xyz = RGB.RED.to(XYZ.class);
		
		assertEquals("XYZ (X) is not as expected!", 0.41246, xyz.getX(), 0.0001);
		assertEquals("XYZ (Y) is not as expected!", 0.21267, xyz.getY(), 0.0001);
		assertEquals("XYZ (Z) is not as expected!", 0.01933, xyz.getZ(), 0.0001);
	}
	
	@Test
	public void testConvertToXYZ_green() {
		
		final var xyz = RGB.GREEN.to(XYZ.class);
		
		assertEquals("XYZ (X) is not as expected!", 0.35758, xyz.getX(), 0.0001);
		assertEquals("XYZ (Y) is not as expected!", 0.71515, xyz.getY(), 0.0001);
		assertEquals("XYZ (Z) is not as expected!", 0.11919, xyz.getZ(), 0.0001);
	}
	
	@Test
	public void testConvertToXYZ_blue() {
		
		final var xyz = RGB.BLUE.to(XYZ.class);
		
		assertEquals("XYZ (X) is not as expected!", 0.18044, xyz.getX(), 0.0001);
		assertEquals("XYZ (Y) is not as expected!", 0.07217, xyz.getY(), 0.0001);
		assertEquals("XYZ (Z) is not as expected!", 0.95050, xyz.getZ(), 0.0001);
	}
	
	@Test
	public void testSerialize() {
		
		final var rgb = RGB.GREEN;
		final var expected = "[0.0,1.0,0.0]";
		
		final var result = Settings.getInstance().getGson().toJson(rgb);
		
		assertEquals(expected, result);
	}
	
	@Test
	public void testDeserialize() {
		
		final var json = "[0.0,1.0,0.0]";
		final var expected = RGB.GREEN;
		
		final var result = Settings.getInstance().getGson().fromJson(json, RGB.class);
		
		assertTrue(RGB.class.isAssignableFrom(result.getClass()));
		
		final var rgb = (RGB) result;
		
		assertEquals(expected.getRed(), rgb.getRed(), 0.00001);
		assertEquals(expected.getGreen(), rgb.getGreen(), 0.00001);
		assertEquals(expected.getBlue(), rgb.getBlue(), 0.00001);
	}
}
