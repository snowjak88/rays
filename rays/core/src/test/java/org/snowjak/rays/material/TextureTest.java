package org.snowjak.rays.material;

import static org.junit.Assert.*;

import org.junit.Test;
import org.snowjak.rays.interact.DescribesSurface;
import org.snowjak.rays.interact.SurfaceDescriptor;
import org.snowjak.rays.spectrum.colorspace.RGB;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;
import org.snowjak.rays.texture.Texture;

public class TextureTest {
	
	@Test
	public void testGetSpectrum() {
		
		var texture = new Texture() {
			
			@Override
			public <S extends DescribesSurface<S>> RGB getRGB(SurfaceDescriptor<S> surfaceDescriptor) {
				
				return new RGB(0.2, 0.3, 0.4);
			}
			
		};
		
		final var radiance = SpectralPowerDistribution.fromRGB(RGB.WHITE);
		final var textSpd = texture.getSpectrum(null);
		
		final var rgb = radiance.multiply(textSpd).toRGB();
		
		assertEquals("Texture RGB(R) not as expected!", 0.2, rgb.getRed(), 0.01);
		assertEquals("Texture RGB(R) not as expected!", 0.3, rgb.getGreen(), 0.01);
		assertEquals("Texture RGB(R) not as expected!", 0.4, rgb.getBlue(), 0.01);
	}
	
}
