package org.snowjak.rays;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import javax.imageio.ImageIO;

import org.snowjak.rays.camera.PinholeCamera;
import org.snowjak.rays.film.Film;
import org.snowjak.rays.filter.BoxFilter;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.light.Light;
import org.snowjak.rays.light.PointLight;
import org.snowjak.rays.material.LambertianMaterial;
import org.snowjak.rays.material.PerfectMirrorMaterial;
import org.snowjak.rays.renderer.PathTracingRenderer;
import org.snowjak.rays.sampler.PseudorandomSampler;
import org.snowjak.rays.shape.SphereShape;
import org.snowjak.rays.spectrum.colorspace.RGB;
import org.snowjak.rays.texture.ConstantTexture;
import org.snowjak.rays.transform.TranslationTransform;

public class Test {
	
	public static void main(String[] args) {
		
		final var primitives = Arrays.asList(
				new Primitive(new SphereShape(0.5, new TranslationTransform(0, 0, 0)), new PerfectMirrorMaterial()),
				new Primitive(new SphereShape(0.5, new TranslationTransform(-1, 0, 0)),
						new LambertianMaterial(new ConstantTexture(RGB.GREEN))),
				new Primitive(new SphereShape(0.5, new TranslationTransform(+1, 0, 0)),
						new LambertianMaterial(new ConstantTexture(RGB.RED))));
		
		final var camera = new PinholeCamera(400, 300, 4, 3, 6, new TranslationTransform(0, 0, -12));
		
		final Collection<Light> lights = Arrays.asList(new PointLight(new Point3D(0, 3, -2),
				Settings.getInstance().getIlluminatorSpectralPowerDistribution().multiply(6)));
		
		final var scene = new Scene(primitives, camera, lights);
		
		final var sampler = new PseudorandomSampler(0, 0, 399, 299, 4, 8, 8);
		final var film = new Film(400, 300, new BoxFilter(0));
		new PathTracingRenderer(3).render(sampler, film, scene);
		
		try {
			
			ImageIO.write(film.getImage(), "png", new File("result.png"));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
