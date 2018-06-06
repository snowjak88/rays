package org.snowjak.rays.spectrum;

import static org.junit.Assert.*;

import org.junit.Test;

public class ConstructedSpectrumTest {
	
	@Test
	public void testSmoothSpectrum() {
		
		final RGB rgb = new ConstructedSpectrum(CIEXYZ.fromRGB(new RGB(0.5, 0.5, 0.5))).toRGB();
		
		assertEquals("RGB (R) was not as expected!", 0.5, rgb.getRed(), 0.001);
		assertEquals("RGB (G) was not as expected!", 0.5, rgb.getGreen(), 0.001);
		assertEquals("RGB (B) was not as expected!", 0.5, rgb.getBlue(), 0.001);
	}
	
}
