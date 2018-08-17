package org.snowjak.rays.spectrum.colorspace;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.snowjak.rays.Settings;

public class XYZTest {
	
	@Test
	public void testFromWavelength_lowWavelength() {
		
		final XYZ xyz = XYZ.fromWavelength(396.5);
		
		assertEquals("XYZ (X) is not as expected!", 0.009390127, xyz.getX(), 0.0001);
		assertEquals("XYZ (Y) is not as expected!", 0.000264074, xyz.getY(), 0.0001);
		assertEquals("XYZ (Z) is not as expected!", 0.044470715, xyz.getZ(), 0.0001);
	}
	
	@Test
	public void testFromWavelength_highWavelength() {
		
		final XYZ xyz = XYZ.fromWavelength(736.5);
		
		assertEquals("XYZ (X) is not as expected!", 0.000895585, xyz.getX(), 0.0001);
		assertEquals("XYZ (Y) is not as expected!", 0.000323412, xyz.getY(), 0.0001);
		assertEquals("XYZ (Z) is not as expected!", 0.000000000, xyz.getZ(), 0.0001);
	}
	
	@Test
	public void testFromSpectrum_standardIlluminator() {
		
		final var spd = Settings.getInstance().getIlluminatorSpectralPowerDistribution();
		final XYZ xyz = XYZ.fromSpectrum(spd, false);
		
		assertEquals("XYZ (X) is not as expected!", 0.95047, xyz.getX() / xyz.getY(), 0.01);
		assertEquals("XYZ (Y) is not as expected!", 1.00000, xyz.getY() / xyz.getY(), 0.01);
		assertEquals("XYZ (Z) is not as expected!", 1.08883, xyz.getZ() / xyz.getY(), 0.01);
	}
	
	@Test
	public void test_convertToRGB_1() {
		
		final var rgb = new XYZ(0.1, 0.1, 0.1).to(RGB.class);
		
		assertEquals("RGB (R) is not as expected!", 0.38182, rgb.getRed(), 0.0001);
		assertEquals("RGB (G) is not as expected!", 0.34035, rgb.getGreen(), 0.0001);
		assertEquals("RGB (B) is not as expected!", 0.33341, rgb.getBlue(), 0.0001);
	}
	
	@Test
	public void test_convertToRGB_2() {
		
		final var rgb = new XYZ(0.3, 0.4, 0.5).to(RGB.class);
		
		assertEquals("RGB (R) is not as expected!", 0.36239, rgb.getRed(), 0.0001);
		assertEquals("RGB (G) is not as expected!", 0.72230, rgb.getGreen(), 0.0001);
		assertEquals("RGB (B) is not as expected!", 0.71092, rgb.getBlue(), 0.0001);
	}
	
}
