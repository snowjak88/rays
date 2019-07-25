package org.snowjak.rays;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;

import org.snowjak.rays.film.Film;
import org.snowjak.rays.renderer.Renderer;
import org.snowjak.rays.sampler.Sampler;

public class Test {
	
	public static void main(String[] args) {
		
		String json = "";
		try (var reader = new BufferedReader(
				new InputStreamReader(Test.class.getClassLoader().getResourceAsStream("test-box_pt_4spp.json")))) {
			
			final var buffer = new StringBuffer();
			
			while (reader.ready()) {
				buffer.append(reader.readLine());
				buffer.append(System.lineSeparator());
			}
			
			json = buffer.toString();
			
			final var task = Settings.getInstance().getGson().fromJson(json, RenderTask.class);
			
			task.setProgressConsumer((pi) -> System.out.println("Render " + pi.getPercent() + "% complete ..."));
			
			ImageIO.write(task.call().getBufferedImage(), "png", new File("result.png"));
			
		} catch (IOException e) {
			
			e.printStackTrace();
			return;
			
		}
		
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
			
			renderer.render(sampler, film, scene, null);
		}
		
	}
	
}
