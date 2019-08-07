package org.snowjak.rays;

import static org.apache.commons.math3.util.FastMath.PI;
import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.pow;
import static org.apache.commons.math3.util.FastMath.sin;
import static org.apache.commons.math3.util.FastMath.sqrt;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
import org.snowjak.rays.transform.RotationTransform;
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
		
		final var renderer = new PathTracingRenderer(3, 1, 1);
		
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
		
		final var luminance = 125d;
		final var radiance = (SpectralPowerDistribution) SpectralPowerDistribution.fromRGB(RGB.WHITE)
				.rescale(luminance);
		final var radianceWatts = radiance.getTotalPower();
		
		final Collection<Light> lights = Arrays.asList(new InfiniteLight(radiance));
		
		final var scene = new Scene(primitives, lights);
		
		final var sampler = new StratifiedSampler(0, 0, 1, 1, 4, 9, 25);
		
		final var renderer = new PathTracingRenderer(1, 1, 1024);
		
		final var sample = sampler.getNextSample();
		
		final var estimate = renderer
				.estimate(new TracedSample(sample, new Ray(new Point3D(0, 0, -3), new Vector3D(0, 0, 1))), scene);
		
		assertEquals(radiance.multiply(pow(PI, 2) / 2d).multiply(texture.getSpectrum(null)).getTotalPower(),
				estimate.getRadiance().getTotalPower(), 0.075 * radianceWatts);
	}
	
	@Test
	public void directLightingDiffuseLightTest() {
		
		final var albedo = 0.8;
		final var albedoRGB = new RGB(albedo, albedo, albedo);
		final var texture = new ConstantTexture(albedoRGB);
		
		final Collection<Primitive> primitives = Arrays
				.asList(new Primitive(new PlaneShape(), new LambertianMaterial(texture)));
		
		final var luminance = 125d;
		/** W m^-2 sr^-1 */
		final var radiance = (SpectralPowerDistribution) SpectralPowerDistribution.fromRGB(RGB.WHITE)
				.rescale(luminance);
		/** W m^-2 sr^-1 */
		final var radianceWatts = radiance.getTotalPower();
		
		/** m */
		final var lightRadius = 1d;
		
		final var lightDistance = 4d;
		final Collection<Light> lights = Arrays.asList(new DiffuseLight(
				new SphereShape(lightRadius, new TranslationTransform(0, lightDistance, 0)), radiance));
		
		final var falloff = 1d / (lightDistance * lightDistance);
		
		final var scene = new Scene(primitives, lights);
		
		final var sampler = new StratifiedSampler(0, 0, 1, 1, 4, 9, 9);
		
		final var renderer = new PathTracingRenderer(1, 1, 1024);
		
		final var sample = sampler.getNextSample();
		
		final var estimate = renderer
				.estimate(new TracedSample(sample, new Ray(new Point3D(0, 3, -3), new Vector3D(0, -1, 1))), scene);
		
		/** W m^-2 sr^-1 */
		final var expectedPower = radiance.multiply(falloff).multiply(texture.getSpectrum(null)).getTotalPower();
		final var estimatedPower = estimate.getRadiance().getTotalPower();
		assertEquals(expectedPower, estimatedPower, 0.05 * radianceWatts);
	}
	
	@Test
	public void directLightingDiffuseLightTest2() {
		
		//
		// Same as directLightingDiffuseLightTest(), but rotated 90 degrees
		//
		
		final var albedo = 0.8;
		final var albedoRGB = new RGB(albedo, albedo, albedo);
		final var texture = new ConstantTexture(albedoRGB);
		
		final Collection<Primitive> primitives = Arrays
				.asList(new Primitive(new PlaneShape(), new LambertianMaterial(texture)));
		
		final var luminance = 125d;
		/** W m^-2 sr^-1 */
		final var radiance = (SpectralPowerDistribution) SpectralPowerDistribution.fromRGB(RGB.WHITE)
				.rescale(luminance);
		/** W m^-2 sr^-1 */
		final var radianceWatts = radiance.getTotalPower();
		
		/** m */
		final var lightRadius = 1d;
		
		final var lightDistance = 4d;
		final Collection<Light> lights = Arrays.asList(new DiffuseLight(
				new SphereShape(lightRadius, new TranslationTransform(lightDistance, 0, 0)), radiance));
		
		final var falloff = 1d / (lightDistance * lightDistance);
		
		final var scene = new Scene(primitives, lights);
		
		final var sampler = new StratifiedSampler(0, 0, 1, 1, 4, 9, 9);
		
		final var renderer = new PathTracingRenderer(1, 1, 1024);
		
		final var sample = sampler.getNextSample();
		
		final var estimate = renderer
				.estimate(new TracedSample(sample, new Ray(new Point3D(3, 0, -3), new Vector3D(-1, 0, 1))), scene);
		
		/** W m^-2 sr^-1 */
		final var expectedPower = radiance.multiply(falloff).multiply(texture.getSpectrum(null)).getTotalPower();
		final var estimatedPower = estimate.getRadiance().getTotalPower();
		assertEquals(expectedPower, estimatedPower, 0.05 * radianceWatts);
	}
	
	@Test
	public void directLightingDiffuseLightTest3() {
		
		//
		// A plane with a surface-normal of (-1,0,0) is illuminated at a distance of 2m
		// by a light-source emitting 100 W m^-2 sr^-1.
		//
		
		final var albedo = 0.8;
		final var albedoRGB = new RGB(albedo, albedo, albedo);
		final var texture = new ConstantTexture(albedoRGB);
		
		final Collection<Primitive> primitives = Arrays.asList(
				new Primitive(new PlaneShape(new RotationTransform(Vector3D.K, 90)), new LambertianMaterial(texture)));
		
		final var luminance = 125d;
		/** W m^-2 sr^-1 */
		final var radiance = (SpectralPowerDistribution) SpectralPowerDistribution.fromRGB(RGB.WHITE)
				.rescale(luminance);
		/** W m^-2 sr^-1 */
		final var radianceWatts = radiance.getTotalPower();
		
		/** m */
		final var lightRadius = 1d;
		
		final var lightDistance = 2d;
		final Collection<Light> lights = Arrays.asList(new DiffuseLight(
				new SphereShape(lightRadius, new TranslationTransform(lightDistance, 0, 0)), radiance));
		
		final var falloff = 1d / (lightDistance * lightDistance);
		
		final var scene = new Scene(primitives, lights);
		
		final var sampler = new StratifiedSampler(0, 0, 1, 1, 4, 9, 9);
		
		final var renderer = new PathTracingRenderer(1, 1, 1024);
		
		final var sample = sampler.getNextSample();
		
		final var estimate = renderer
				.estimate(new TracedSample(sample, new Ray(new Point3D(1, -1, 0), new Vector3D(-1, 1, 0))), scene);
		
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
		
		final var luminance = 125d;
		final var radiance = (SpectralPowerDistribution) SpectralPowerDistribution.fromRGB(RGB.WHITE)
				.rescale(luminance);
		/** W m^-2 sr^-1 */
		final var radianceWatts = radiance.getTotalPower();
		
		final var lightDistance = 1d;
		final Collection<Light> lights = Arrays.asList(new PointLight(new Point3D(0, lightDistance, 0), radiance));
		
		final var scene = new Scene(primitives, lights);
		
		final var sampler = new StratifiedSampler(0, 0, 1, 1, 4, 9, 25);
		
		final var renderer = new PathTracingRenderer(1, 1, 1024);
		
		final var sample = sampler.getNextSample();
		
		final var estimate = renderer
				.estimate(new TracedSample(sample, new Ray(new Point3D(0, 3, -3), new Vector3D(0, -1, 1))), scene);
		
		final var falloff = 1d / (lightDistance * lightDistance);
		
		assertEquals(radiance.multiply(falloff).multiply(texture.getSpectrum(null)).getTotalPower(),
				estimate.getRadiance().getTotalPower(), 0.05 * radianceWatts);
	}
	
	@Test
	public void testDamnPlaneColoration() {
		
		final var planeTexture = new ConstantTexture(new RGB(0, 1, 0));
		final var plane = new Primitive(
				new PlaneShape(new TranslationTransform(+3, 0, 0), new RotationTransform(Vector3D.K, 90)),
				new LambertianMaterial(planeTexture));
		
		final var lightRadiance = SpectralPowerDistribution.fromBlackbody(5500, 125);
		final var light = new DiffuseLight(new SphereShape(0.5), lightRadiance);
		
		final var scene = new Scene(Arrays.asList(plane), Arrays.asList(light));
		final var sampler = new StratifiedSampler(0, 0, 1, 1, 1, 16, 16);
		final var renderer = new PathTracingRenderer(2, 1, 256);
		
		final var estimate = renderer
				.estimate(new TracedSample(sampler.getNextSample(), new Ray(new Point3D(2, 0, 0), Vector3D.I)), scene);
		final var estimatedRGB = estimate.getRadiance().toRGB();
		
		final var falloff = 1d / (3d * 3d);
		
		final var expectedRadiance = lightRadiance.multiply(falloff).multiply(planeTexture.getSpectrum(null));
		final var expectedRGB = expectedRadiance.toRGB();
		
		assertEquals(expectedRadiance.getTotalPower(), estimate.getRadiance().getTotalPower(),
				lightRadiance.getTotalPower() * 0.05);
		
		assertEquals("Estimate RGB(R) not as expected!", expectedRGB.getRed(), estimatedRGB.getRed(), 0.05);
		assertEquals("Estimate RGB(G) not as expected!", expectedRGB.getGreen(), estimatedRGB.getGreen(), 0.05);
		assertEquals("Estimate RGB(B) not as expected!", expectedRGB.getBlue(), estimatedRGB.getBlue(), 0.05);
	}
	
	// @Test
	public void indirectLightingFurnaceTest() {
		
		//
		// A sphere of radius 1 is situated 2 units away from a plane, whose normal is
		// oriented through the center of the sphere.
		//
		// An infinite light-source of 100 W m^-2 sr^-1 is situated surrounding the
		// scene.
		//
		// It is expected that, at its closest point to the plane, the sphere will
		// receive incident radiation only from the plane.
		//
		
		final var planeAlbedo = 0.5d;
		final var sphereAlbedo = 1d;
		
		final var planeTexture = new ConstantTexture(new RGB(planeAlbedo, planeAlbedo, planeAlbedo));
		final var sphereTexture = new ConstantTexture(new RGB(sphereAlbedo, sphereAlbedo, sphereAlbedo));
		
		final var sphereRadius = 1d;
		
		final var plane = new Primitive(new PlaneShape(new TranslationTransform(0, -1, 0)),
				new LambertianMaterial(planeTexture));
		final var sphere = new Primitive(new SphereShape(sphereRadius, new TranslationTransform(0, 1, 0)),
				new LambertianMaterial(sphereTexture));
		
		final var luminance = 125d;
		final var lightRadiance = (SpectralPowerDistribution) SpectralPowerDistribution.fromRGB(RGB.WHITE)
				.rescale(luminance);
		
		/** W m^-2 sr^-1 */
		final var radianceWatts = lightRadiance.getTotalPower();
		
		final var light = new InfiniteLight(lightRadiance);
		
		final var scene = new Scene(Arrays.asList(plane, sphere), Arrays.asList(light));
		
		final var sampler = new StratifiedSampler(0, 0, 1, 1, 4, 9, 25);
		
		final var renderer = new PathTracingRenderer(2, 128, 128);
		
		final var sample = sampler.getNextSample();
		
		final var estimate = renderer
				.estimate(new TracedSample(sample, new Ray(new Point3D(0, -0.1, -0.1), new Vector3D(0, 1, 1))), scene);
		
		//
		// Distance from point on plane to the center of the sphere.
		final var sphereDistance = Vector3D.from(plane.getObjectZero(), sphere.getObjectZero()).getMagnitude();
		
		//
		// Solid angle subtended by the sphere from the point on the plane nearest to
		// the sphere.
		final var sphereSolidAngle = 2d * PI
				* (1d - sqrt(pow(sphereDistance, 2) - pow(sphereRadius, 2)) / (sphereDistance));
		
		//
		// The infinite light-source is not subject to falloff, so the only radiance
		// that's subject to falloff is that from the plane to the sphere.
		final var falloff = 1d / pow(sphereDistance - sphereRadius, 2);
		
		//
		// The plane receives radiance from all directions *except* where the sphere
		// occludes that infinite light-source.
		final var planeRadianceFraction = 1d - (sphereSolidAngle / (2d * PI));
		
		final var expectedPlaneRadiance = lightRadiance.multiply(pow(PI, 2) / 2d).multiply(planeRadianceFraction)
				.multiply(planeTexture.getSpectrum(null));
		final var expectedSphereRadiance = expectedPlaneRadiance.multiply(falloff)
				.multiply(sphereTexture.getSpectrum(null));
		
		assertEquals(expectedSphereRadiance.getTotalPower(), estimate.getRadiance().getTotalPower(),
				0.075 * radianceWatts);
	}
	
	// @Test
	public void indirectLightingFurnaceTest2() {
		
		//
		// Same as indirectLightingFurnaceTest(), but rotated 90 degrees.
		//
		
		final var planeAlbedo = 0.5d;
		final var sphereAlbedo = 1d;
		
		final var planeTexture = new ConstantTexture(new RGB(planeAlbedo, planeAlbedo, planeAlbedo));
		final var sphereTexture = new ConstantTexture(new RGB(sphereAlbedo, sphereAlbedo, sphereAlbedo));
		
		final var sphereRadius = 1d;
		
		final var plane = new Primitive(
				new PlaneShape(new TranslationTransform(-1, 0, 0), new RotationTransform(Vector3D.K, 90)),
				new LambertianMaterial(planeTexture));
		final var sphere = new Primitive(new SphereShape(sphereRadius, new TranslationTransform(1, 0, 0)),
				new LambertianMaterial(sphereTexture));
		
		//
		// Infinite light-source strength ( W m^-2 sr^-1 )
		final var luminance = 125d;
		final var lightRadiance = (SpectralPowerDistribution) SpectralPowerDistribution.fromRGB(RGB.WHITE)
				.rescale(luminance);
		
		/** W m^-2 sr^-1 */
		final var lightRadiantIntensity = lightRadiance.getTotalPower();
		
		final var light = new InfiniteLight(lightRadiance);
		
		final var scene = new Scene(Arrays.asList(plane, sphere), Arrays.asList(light));
		
		final var sampler = new StratifiedSampler(0, 0, 1, 1, 4, 9, 25);
		
		final var renderer = new PathTracingRenderer(2, 128, 128);
		
		final var sample = sampler.getNextSample();
		
		final var estimate = renderer
				.estimate(new TracedSample(sample, new Ray(new Point3D(-0.1, 0, -0.1), new Vector3D(1, 0, 1))), scene);
		
		//
		// Distance from point on plane to the center of the sphere.
		final var sphereDistance = Vector3D.from(plane.getObjectZero(), sphere.getObjectZero()).getMagnitude();
		
		//
		// Solid angle subtended by the sphere from the point on the plane nearest to
		// the sphere.
		final var sphereSolidAngle = 2d * PI
				* (1d - sqrt(pow(sphereDistance, 2) - pow(sphereRadius, 2)) / (sphereDistance));
		
		//
		// The infinite light-source is not subject to falloff, so the only radiance
		// that's subject to falloff is that from the plane to the sphere.
		final var falloff = 1d / pow(sphereDistance - sphereRadius, 2);
		
		//
		// The plane receives radiance from all directions *except* where the sphere
		// occludes that infinite light-source.
		final var planeRadianceFraction = 1d - (sphereSolidAngle / (2d * PI));
		
		final var expectedPlaneRadiance = lightRadiance.multiply(pow(PI, 2) / 2d).multiply(planeRadianceFraction)
				.multiply(planeTexture.getSpectrum(null));
		final var expectedSphereRadiance = expectedPlaneRadiance.multiply(falloff)
				.multiply(sphereTexture.getSpectrum(null));
		
		assertEquals(expectedSphereRadiance.getTotalPower(), estimate.getRadiance().getTotalPower(),
				0.075 * lightRadiantIntensity);
	}
	
}
