package org.snowjak.rays.spectrum;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CIEXYZTest {
	
	@Test
	public void testFromWavelength_lowWavelength() {
		
		final CIEXYZ xyz = CIEXYZ.fromWavelength(396.5);
		
		assertEquals("XYZ (X) is not as expected!", 0.012277865, xyz.getX(), 0.001);
		assertEquals("XYZ (Y) is not as expected!", 0.001385143, xyz.getY(), 0.001);
		assertEquals("XYZ (Z) is not as expected!", 0.060436050, xyz.getZ(), 0.001);
	}
	
	@Test
	public void testFromWavelength_highWavelength() {
		
		final CIEXYZ xyz = CIEXYZ.fromWavelength(736.5);
		
		assertEquals("XYZ (X) is not as expected!", 0.000741939, xyz.getX(), 0.001);
		assertEquals("XYZ (Y) is not as expected!", 0.000285400, xyz.getY(), 0.001);
		assertEquals("XYZ (Z) is not as expected!", 0.000000000, xyz.getZ(), 0.001);
	}
	
	@Test
	public void testFromRGB_gray() {
		
		final CIEXYZ xyz = CIEXYZ.fromRGB(new RGB(0.5, 0.5, 0.5));
		
		assertEquals("XYZ (X) is not as expected!", 0.20344, xyz.getX(), 0.001);
		assertEquals("XYZ (Y) is not as expected!", 0.21404, xyz.getY(), 0.001);
		assertEquals("XYZ (Z) is not as expected!", 0.23305, xyz.getZ(), 0.001);
	}
	
	@Test
	public void testFromRGB_red() {
		
		final CIEXYZ xyz = CIEXYZ.fromRGB(new RGB(1.0, 0.0, 0.0));
		
		assertEquals("XYZ (X) is not as expected!", 0.41246, xyz.getX(), 0.001);
		assertEquals("XYZ (Y) is not as expected!", 0.21267, xyz.getY(), 0.001);
		assertEquals("XYZ (Z) is not as expected!", 0.01933, xyz.getZ(), 0.001);
	}
	
	@Test
	public void testFromRGB_green() {
		
		final CIEXYZ xyz = CIEXYZ.fromRGB(new RGB(0.0, 1.0, 0.0));
		
		assertEquals("XYZ (X) is not as expected!", 0.35758, xyz.getX(), 0.001);
		assertEquals("XYZ (Y) is not as expected!", 0.71515, xyz.getY(), 0.001);
		assertEquals("XYZ (Z) is not as expected!", 0.11919, xyz.getZ(), 0.001);
	}
	
	@Test
	public void testFromRGB_blue() {
		
		final CIEXYZ xyz = CIEXYZ.fromRGB(new RGB(0.0, 0.0, 1.0));
		
		assertEquals("XYZ (X) is not as expected!", 0.18044, xyz.getX(), 0.001);
		assertEquals("XYZ (Y) is not as expected!", 0.07217, xyz.getY(), 0.001);
		assertEquals("XYZ (Z) is not as expected!", 0.95030, xyz.getZ(), 0.001);
	}
	
	public void testToRGB_1() {
		
		final RGB rgb = new CIEXYZ(0.10, 0.10, 0.10).toRGB();
		
		assertEquals("RGB (R) is not as expected!", 0.38182, rgb.getRed(), 0.001);
		assertEquals("RGB (G) is not as expected!", 0.34035, rgb.getGreen(), 0.001);
		assertEquals("RGB (B) is not as expected!", 0.33341, rgb.getBlue(), 0.001);
		
	}
	
public void testToRGB_2() {
		
		final RGB rgb = new CIEXYZ(0.30, 0.40, 0.50).toRGB();
		
		assertEquals("RGB (R) is not as expected!", 0.36239, rgb.getRed(), 0.001);
		assertEquals("RGB (G) is not as expected!", 0.72230, rgb.getGreen(), 0.001);
		assertEquals("RGB (B) is not as expected!", 0.71092, rgb.getBlue(), 0.001);
		
	}
	
}
