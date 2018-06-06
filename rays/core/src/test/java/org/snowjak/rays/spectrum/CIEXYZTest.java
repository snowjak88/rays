package org.snowjak.rays.spectrum;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CIEXYZTest {
	
	@Test
	public void testColorMappingFunctionSize() {
		
		assertEquals("CMF table-size is not as expected!", 471, CIEXYZ.COLOR_MAPPING_FUNCTIONS.size());
	}
	
	@Test
	public void testFromWavelength_lowWavelength() {
		
		final CIEXYZ xyz = CIEXYZ.fromWavelength(396.5);
		
		assertEquals("XYZ (X) is not as expected!", 0.009390127, xyz.getX(), 0.0001);
		assertEquals("XYZ (Y) is not as expected!", 0.000264074, xyz.getY(), 0.0001);
		assertEquals("XYZ (Z) is not as expected!", 0.044470715, xyz.getZ(), 0.0001);
	}
	
	@Test
	public void testFromWavelength_highWavelength() {
		
		final CIEXYZ xyz = CIEXYZ.fromWavelength(736.5);
		
		assertEquals("XYZ (X) is not as expected!", 0.000895585, xyz.getX(), 0.0001);
		assertEquals("XYZ (Y) is not as expected!", 0.000323412, xyz.getY(), 0.0001);
		assertEquals("XYZ (Z) is not as expected!", 0.000000000, xyz.getZ(), 0.0001);
	}
	
	@Test
	public void testFromSpectrum_standardIlluminator() {
		
		final CIEXYZ xyz_beforeNormalization = CIEXYZ.fromSpectrum(CIEXYZ.D65_STANDARD_ILLUMINATOR_SPECTRUM);
		final CIEXYZ xyz = new CIEXYZ(xyz_beforeNormalization.getTriplet().divide(xyz_beforeNormalization.getY()));
		
		assertEquals("XYZ (X) is not as expected!", 0.95047, xyz.getX() / xyz.getY(), 0.0001);
		assertEquals("XYZ (Y) is not as expected!", 1.00000, xyz.getY() / xyz.getY(), 0.0001);
		assertEquals("XYZ (Z) is not as expected!", 1.08883, xyz.getZ() / xyz.getY(), 0.0001);
	}
	
	@Test
	public void testFromRGB_gray() {
		
		final CIEXYZ xyz = CIEXYZ.fromRGB(new RGB(0.5, 0.5, 0.5));
		
		assertEquals("XYZ (X) is not as expected!", 0.20344, xyz.getX(), 0.0001);
		assertEquals("XYZ (Y) is not as expected!", 0.21404, xyz.getY(), 0.0001);
		assertEquals("XYZ (Z) is not as expected!", 0.23305, xyz.getZ(), 0.0001);
	}
	
	@Test
	public void testFromRGB_red() {
		
		final CIEXYZ xyz = CIEXYZ.fromRGB(new RGB(1.0, 0.0, 0.0));
		
		assertEquals("XYZ (X) is not as expected!", 0.41246, xyz.getX(), 0.0001);
		assertEquals("XYZ (Y) is not as expected!", 0.21267, xyz.getY(), 0.0001);
		assertEquals("XYZ (Z) is not as expected!", 0.01933, xyz.getZ(), 0.0001);
	}
	
	@Test
	public void testFromRGB_green() {
		
		final CIEXYZ xyz = CIEXYZ.fromRGB(new RGB(0.0, 1.0, 0.0));
		
		assertEquals("XYZ (X) is not as expected!", 0.35758, xyz.getX(), 0.0001);
		assertEquals("XYZ (Y) is not as expected!", 0.71515, xyz.getY(), 0.0001);
		assertEquals("XYZ (Z) is not as expected!", 0.11919, xyz.getZ(), 0.0001);
	}
	
	@Test
	public void testFromRGB_blue() {
		
		final CIEXYZ xyz = CIEXYZ.fromRGB(new RGB(0.0, 0.0, 1.0));
		
		assertEquals("XYZ (X) is not as expected!", 0.18044, xyz.getX(), 0.0001);
		assertEquals("XYZ (Y) is not as expected!", 0.07217, xyz.getY(), 0.0001);
		assertEquals("XYZ (Z) is not as expected!", 0.95050, xyz.getZ(), 0.0001);
	}
	
	public void testToRGB_1() {
		
		final RGB rgb = new CIEXYZ(0.10, 0.10, 0.10).toRGB();
		
		assertEquals("RGB (R) is not as expected!", 0.38182, rgb.getRed(), 0.0001);
		assertEquals("RGB (G) is not as expected!", 0.34035, rgb.getGreen(), 0.0001);
		assertEquals("RGB (B) is not as expected!", 0.33341, rgb.getBlue(), 0.0001);
		
	}
	
	public void testToRGB_2() {
		
		final RGB rgb = new CIEXYZ(0.30, 0.40, 0.50).toRGB();
		
		assertEquals("RGB (R) is not as expected!", 0.36239, rgb.getRed(), 0.0001);
		assertEquals("RGB (G) is not as expected!", 0.72230, rgb.getGreen(), 0.0001);
		assertEquals("RGB (B) is not as expected!", 0.71092, rgb.getBlue(), 0.0001);
		
	}
	
	@Test
	public void testLinearInterpolate() {
		
		assertEquals("Linear interpolation is not as expected!", 1.0, CIEXYZ.linearInterpolate(0.0, 1, 2), 0.00001);
		assertEquals("Linear interpolation is not as expected!", 2.5, CIEXYZ.linearInterpolate(0.5, 2, 3), 0.00001);
		assertEquals("Linear interpolation is not as expected!", 1.5, CIEXYZ.linearInterpolate(-0.5, 2, 3), 0.00001);
		assertEquals("Linear interpolation is not as expected!", 3.5, CIEXYZ.linearInterpolate(1.5, 2, 3), 0.00001);
	}
	
}
