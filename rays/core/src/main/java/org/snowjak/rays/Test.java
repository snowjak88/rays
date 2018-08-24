package org.snowjak.rays;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import org.snowjak.rays.camera.PinholeCamera;
import org.snowjak.rays.film.Film;
import org.snowjak.rays.filter.BoxFilter;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.light.Light;
import org.snowjak.rays.light.PointLight;
import org.snowjak.rays.material.LambertianMaterial;
import org.snowjak.rays.material.Material;
import org.snowjak.rays.material.PerfectMirrorMaterial;
import org.snowjak.rays.renderer.PathTracingRenderer;
import org.snowjak.rays.renderer.Renderer;
import org.snowjak.rays.sampler.PseudorandomSampler;
import org.snowjak.rays.sampler.Sampler;
import org.snowjak.rays.shape.SphereShape;
import org.snowjak.rays.spectrum.colorspace.RGB;
import org.snowjak.rays.texture.ConstantTexture;
import org.snowjak.rays.transform.TranslationTransform;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

public class Test {
	
	public static void main(String[] args) {
		
		final var primitives = new LinkedList<Primitive>();
		for (double x = -6; x <= 6; x += 1)
			for (double y = -6; y <= 6; y += 1)
				for (double z = 0; z <= 6; z += 1) {
					
					final Material material;
					if (((int) (x + y + z)) % 2 == 0)
						material = new PerfectMirrorMaterial(
								new ConstantTexture(new RGB((x + 6d) / 12d, (y + 6d) / 12d, z / 6d)));
					else
						material = new LambertianMaterial(
								new ConstantTexture(new RGB((x + 6d) / 12d, (y + 6d) / 12d, z / 6d)));
					
					primitives.add(new Primitive(new SphereShape(0.5, new TranslationTransform(x, y, z)), material));
				}
			
		final var camera = new PinholeCamera(400, 300, 4, 3, 6, new TranslationTransform(0, 0, -6));
		
		final Collection<Light> lights = new LinkedList<Light>();
		for (double x = -5; x <= 5; x += 1)
			for (double y = -5; y <= 5; y += 1)
				for (double z = 1; z <= 5; z += 1)
					lights.add(new PointLight(new Point3D(x, y, z),
							Settings.getInstance().getIlluminatorSpectralPowerDistribution().multiply(5d)));
				
		final var scene = new Scene(primitives, camera, lights);
		
		final var executor = MoreExecutors
				.listeningDecorator(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
		Runtime.getRuntime().addShutdownHook(new Thread(() -> executor.shutdownNow()));
		
		final var futures = new LinkedList<ListenableFuture<?>>();
		
		final var film = new Film(400, 300, new BoxFilter(0));
		for (int x = 0; x < 400; x++) {
			
			final var task = new RenderRunnable(new PseudorandomSampler(x, 0, x, 299, 8, 8, 8),
					new PathTracingRenderer(4), film, scene);
			
			futures.add(executor.submit(task));
		}
		
		Futures.whenAllComplete(futures).run(() -> {
			try {
				
				ImageIO.write(film.getImage().getBufferedImage(), "png", new File("result.png"));
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			executor.shutdown();
		}, executor);
		
	}
	
	public static class RenderRunnable implements Runnable {
		
		private final Sampler sampler;
		private final Renderer renderer;
		private final Film film;
		private final Scene scene;
		
		public RenderRunnable(Sampler sampler, Renderer renderer, Film film, Scene scene) {
			
			this.sampler = sampler;
			this.renderer = renderer;
			this.film = film;
			this.scene = scene;
		}
		
		@Override
		public void run() {
			
			renderer.render(sampler, film, scene);
		}
		
	}
	
}
