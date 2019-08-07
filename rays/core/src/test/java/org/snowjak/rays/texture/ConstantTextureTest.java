/**
 * 
 */
package org.snowjak.rays.texture;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.snowjak.rays.spectrum.colorspace.RGB;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;

/**
 * @author snowjak88
 *
 */
public class ConstantTextureTest {
	
	@Test
	public void testGetRed() {
		
		final var expected = RGB.RED;
		final var texture = new ConstantTexture(expected);
		final var textureSpd = texture.getSpectrum(null);
		final var illuminated = SpectralPowerDistribution.fromRGB(RGB.WHITE).multiply(textureSpd);
		final var rgb = illuminated.toRGB();
		
		assertEquals("Red Texture RGB(R) was not as expected!", expected.getRed(), rgb.getRed(), 0.05);
		assertEquals("Red Texture RGB(G) was not as expected!", expected.getGreen(), rgb.getGreen(), 0.05);
		assertEquals("Red Texture RGB(B) was not as expected!", expected.getBlue(), rgb.getBlue(), 0.05);
	}
	
	@Test
	public void testGetGreen() {
		
		final var expected = RGB.GREEN;
		final var texture = new ConstantTexture(expected);
		final var textureSpd = texture.getSpectrum(null);
		final var illuminated = SpectralPowerDistribution.fromRGB(RGB.WHITE).multiply(textureSpd);
		final var rgb = illuminated.toRGB();
		
		assertEquals("Red Texture RGB(R) was not as expected!", expected.getRed(), rgb.getRed(), 0.05);
		assertEquals("Red Texture RGB(G) was not as expected!", expected.getGreen(), rgb.getGreen(), 0.05);
		assertEquals("Red Texture RGB(B) was not as expected!", expected.getBlue(), rgb.getBlue(), 0.05);
	}
	
	@Test
	public void testGetBlue() {
		
		final var expected = RGB.BLUE;
		final var texture = new ConstantTexture(expected);
		final var textureSpd = texture.getSpectrum(null);
		final var illuminated = SpectralPowerDistribution.fromRGB(RGB.WHITE).multiply(textureSpd);
		final var rgb = illuminated.toRGB();
		
		assertEquals("Red Texture RGB(R) was not as expected!", expected.getRed(), rgb.getRed(), 0.05);
		assertEquals("Red Texture RGB(G) was not as expected!", expected.getGreen(), rgb.getGreen(), 0.05);
		assertEquals("Red Texture RGB(B) was not as expected!", expected.getBlue(), rgb.getBlue(), 0.05);
	}
	
}
