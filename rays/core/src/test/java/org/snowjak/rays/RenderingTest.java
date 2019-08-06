package org.snowjak.rays;

import static org.apache.commons.math3.util.FastMath.PI;
import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.pow;
import static org.apache.commons.math3.util.FastMath.sin;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.snowjak.rays.camera.OrthographicCamera;
import org.snowjak.rays.geometry.Point2D;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.light.DiffuseLight;
import org.snowjak.rays.light.InfiniteLight;
import org.snowjak.rays.light.Light;
import org.snowjak.rays.light.PointLight;
import org.snowjak.rays.material.LambertianMaterial;
import org.snowjak.rays.renderer.PathTracingRenderer;
import org.snowjak.rays.sample.FixedSample;
import org.snowjak.rays.sample.TracedSample;
import org.snowjak.rays.sampler.StratifiedSampler;
import org.snowjak.rays.shape.PlaneShape;
import org.snowjak.rays.shape.SphereShape;
import org.snowjak.rays.spectrum.colorspace.RGB;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;
import org.snowjak.rays.texture.ConstantTexture;
import org.snowjak.rays.transform.TranslationTransform;

public class RenderingTest {
	
	@Test
	public void test1() {
		
		final var falloff = pow(3d - sin(45d * PI / 180d), 2);
		
		final var primitives = Arrays.asList(new Primitive(new SphereShape(1.0, new TranslationTransform(0, 0, 4)),
				new LambertianMaterial(new ConstantTexture(RGB.RED))));
		final var camera = new OrthographicCamera(200, 200, 2, 2);
		final Collection<Light> lights = Arrays.asList(
				new PointLight(new Point3D(0, 3, 3d + sin(45d * PI / 180d)), (SpectralPowerDistribution) Settings
						.getInstance().getIlluminatorSpectralPowerDistribution().multiply(falloff)));
		
		final var scene = new Scene(primitives, lights);
		
		final var renderer = new PathTracingRenderer(3, 1);
		
		final var sample = new FixedSample(new Point2D(100d, 100d - 100d * cos(45d * PI / 180d)), new Point2D(0.5, 0.5),
				0, Arrays.asList(0.5, 0.5, 0.5, 0.5), Arrays.asList(new Point2D(0.5, 0.5), new Point2D(0.5, 0.5),
						new Point2D(0.5, 0.5), new Point2D(0.5, 0.5)));
		final var tracedSample = camera.trace(sample);
		
		assertEquals(0.0, tracedSample.getRay().getOrigin().getX(), 0.00001);
		assertEquals(cos(45d * PI / 180d), tracedSample.getRay().getOrigin().getY(), 0.00001);
		assertEquals(0.0, tracedSample.getRay().getOrigin().getZ(), 0.00001);
		
		assertEquals(0.0, tracedSample.getRay().getDirection().getX(), 0.00001);
		assertEquals(0.0, tracedSample.getRay().getDirection().getY(), 0.00001);
		assertEquals(1.0, tracedSample.getRay().getDirection().getZ(), 0.00001);
		
		final var interaction = scene.getInteraction(tracedSample.getRay());
		assertNotNull(interaction);
		assertEquals(0.0, interaction.getPoint().getX(), 0.00001);
		assertEquals(cos(45d * PI / 180d), interaction.getPoint().getY(), 0.00001);
		assertEquals(4d - sin(45d * PI / 180d), interaction.getPoint().getZ(), 0.00001);
		
		assertEquals(0.0, interaction.getNormal().normalize().get(0), 0.00001);
		assertEquals(cos(45d * PI / 180d), Vector3D.from(interaction.getNormal()).normalize().get(1), 0.00001);
		assertEquals(-sin(45d * PI / 180d), Vector3D.from(interaction.getNormal()).normalize().get(2), 0.00001);
		
		final var l = scene.getLights().iterator().next();
		
		final var ls = l.sample(interaction, sample);
		
		assertTrue(l.isVisible(interaction, ls.getA(), scene));
		
		final var estimate = renderer.estimate(tracedSample, scene);
		
		assertNotNull(estimate);
	}
	
	@Test
	public void directLightingFurnaceTest() {
		
		final var albedo = 0.3;
		final var albedoRGB = new RGB(albedo, albedo, albedo);
		final var texture = new ConstantTexture(albedoRGB);
		
		final Collection<Primitive> primitives = Arrays
				.asList(new Primitive(new SphereShape(1d), new LambertianMaterial(texture)));
		
		final var radianceWatts = 100d;
		final var radiance = (SpectralPowerDistribution) SpectralPowerDistribution.fromRGB(RGB.WHITE)
				.rescale(radianceWatts);
		
		final Collection<Light> lights = Arrays.asList(new InfiniteLight(radiance));
		
		final var scene = new Scene(primitives, lights);
		
		final var sampler = new StratifiedSampler(0, 0, 1, 1, 4, 9, 25);
		
		final var renderer = new PathTracingRenderer(1, 1024);
		
		final var sample = sampler.getNextSample();
		
		final var estimate = renderer
				.estimate(new TracedSample(sample, new Ray(new Point3D(0, 0, -3), new Vector3D(0, 0, 1))), scene);
		
		assertEquals(radiance.multiply(texture.getSpectrum(null)).getTotalPower(),
				estimate.getRadiance().getTotalPower(), 0.05 * radianceWatts);
	}
	
	@Test
	public void directLightingDiffuseLightTest() {
		
		final var albedo = 0.8;
		final var albedoRGB = new RGB(albedo, albedo, albedo);
		final var texture = new ConstantTexture(albedoRGB);
		
		final Collection<Primitive> primitives = Arrays
				.asList(new Primitive(new PlaneShape(), new LambertianMaterial(texture)));
		
		/** W m^-2 sr^-1 */
		final var radianceWatts = 100d;
		/** W m^-2 sr^-1 */
		final var radiance = (SpectralPowerDistribution) SpectralPowerDistribution.fromRGB(RGB.WHITE)
				.rescale(radianceWatts);
		
		/** m */
		final var lightRadius = 1d;
		
		final var lightDistance = 4d;
		final Collection<Light> lights = Arrays.asList(new DiffuseLight(
				new SphereShape(lightRadius, new TranslationTransform(0, lightDistance, 0)), radiance));
		
		final var falloff = 1d / (lightDistance * lightDistance);
		
		final var scene = new Scene(primitives, lights);
		
		final var sampler = new StratifiedSampler(0, 0, 1, 1, 4, 9, 9);
		
		final var renderer = new PathTracingRenderer(1, 1024);
		
		final var sample = sampler.getNextSample();
		
		final var estimate = renderer
				.estimate(new TracedSample(sample, new Ray(new Point3D(0, 3, -3), new Vector3D(0, -1, 1))), scene);
		
		/** W m^-2 sr^-1 */
		final var expectedPower = radiance.multiply(falloff).multiply(texture.getSpectrum(null)).getTotalPower();
		final var estimatedPower = estimate.getRadiance().getTotalPower();
		assertEquals(expectedPower, estimatedPower, 0.05 * radianceWatts);
	}
	
	@Test
	public void directLightingPointLightTest() {
		
		final var albedo = 0.5;
		final var albedoRGB = new RGB(albedo, albedo, albedo);
		final var texture = new ConstantTexture(albedoRGB);
		
		final Collection<Primitive> primitives = Arrays
				.asList(new Primitive(new PlaneShape(), new LambertianMaterial(texture)));
		
		final var radianceWatts = 100d;
		final var radiance = (SpectralPowerDistribution) SpectralPowerDistribution.fromRGB(RGB.WHITE)
				.rescale(radianceWatts);
		
		final var lightDistance = 1d;
		final Collection<Light> lights = Arrays.asList(new PointLight(new Point3D(0, lightDistance, 0), radiance));
		
		final var scene = new Scene(primitives, lights);
		
		final var sampler = new StratifiedSampler(0, 0, 1, 1, 4, 9, 25);
		
		final var renderer = new PathTracingRenderer(1, 1024);
		
		final var sample = sampler.getNextSample();
		
		final var estimate = renderer
				.estimate(new TracedSample(sample, new Ray(new Point3D(0, 3, -3), new Vector3D(0, -1, 1))), scene);
		
		final var falloff = 1d / (lightDistance * lightDistance);
		
		assertEquals(radiance.multiply(falloff).multiply(texture.getSpectrum(null)).getTotalPower(),
				estimate.getRadiance().getTotalPower(), 0.05 * radianceWatts);
	}
	
}
