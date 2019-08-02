/**
 * 
 */
package org.snowjak.rays.material;

import static org.apache.commons.math3.util.FastMath.PI;
import static org.junit.Assert.*;

import org.junit.Test;
import org.snowjak.rays.Primitive;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.sampler.StratifiedSampler;
import org.snowjak.rays.shape.PlaneShape;
import org.snowjak.rays.spectrum.Spectrum;
import org.snowjak.rays.spectrum.colorspace.RGB;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;
import org.snowjak.rays.texture.ConstantTexture;

/**
 * @author snowjak88
 *
 */
public class LambertianMaterialTest {
	
	@Test
	public void testGetReflection() {
		
		//
		// Consider a perfectly-Lambertian plane with an isotropic albedo
		// of 0.8
		// This plane is subjected to uniform illumination equal
		// to 1 W / m^2 sr
		//
		// We can compute the irradiance reflected at a point on
		// the plane as:
		//
		// Lo = integral: (rho / pi) * Li * cos(theta) dw
		//
		// where Li = 1 W / m^2 sr
		// rho = albedo
		// theta = angle between incident and normal
		// w = solid-angle
		//
		// = ( rho / pi ) * integral: Li * cos(theta) dw
		//
		// = ( rho / pi ) * ( Li * pi )
		//
		// = rho * Li
		//
		
		final var albedo = 0.8;
		final var materialRGB = new RGB(albedo, albedo, albedo);
		final var texture = new ConstantTexture(materialRGB);
		final var material = new LambertianMaterial(texture);
		final var primitive = new Primitive(new PlaneShape(), material);
		
		final var ray = new Ray(new Point3D(0, 2, -2), new Vector3D(0, -1, 1).normalize());
		final var interaction = primitive.getInteraction(ray);
		
		//
		// 1 W m^-2 sr^-1
		final Spectrum radiantIntensity = SpectralPowerDistribution.fromRGB(RGB.WHITE).rescale(1d);
		//
		// ( Li * pi ) ( W m^-2 )
		final Spectrum totalRadiantFlux = radiantIntensity.multiply(PI);
		
		//
		// Should be equal to ( rho / pi ) * ( Li * pi ) = ( rho * Li ) ( W m^-2 sr^-1 )
		//
		final var sampler = new StratifiedSampler(0, 0, 1, 1, 9);
		final var reflectedSample = material.getReflectionSample(interaction, sampler.getNextSample());
		final var reflected = reflectedSample.getAlbedo().multiply(totalRadiantFlux);
		
		assertEquals("Reflected power is not as expected.",
				radiantIntensity.multiply(texture.getSpectrum(interaction)).getTotalPower(), reflected.getTotalPower(),
				0.001);
		
		final var expectedRGB = SpectralPowerDistribution.fromRGB(materialRGB).rescale(reflected.getTotalPower())
				.toRGB();
		
		assertEquals("Reflected RGB(R) is not as expected.", expectedRGB.getRed(), reflected.toRGB().getRed(), 0.05);
		assertEquals("Reflected RGB(G) is not as expected.", expectedRGB.getGreen(), reflected.toRGB().getGreen(),
				0.05);
		assertEquals("Reflected RGB(B) is not as expected.", expectedRGB.getBlue(), reflected.toRGB().getBlue(), 0.05);
	}
	
}
