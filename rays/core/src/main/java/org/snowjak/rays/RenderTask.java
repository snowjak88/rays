package org.snowjak.rays;

import java.util.concurrent.Callable;

import org.snowjak.rays.film.Film;
import org.snowjak.rays.film.Film.Image;
import org.snowjak.rays.renderer.Renderer;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.sampler.Sampler;

/**
 * Represents an entire rendering task.
 * 
 * @author snowjak88
 *
 */
public class RenderTask implements Callable<Image> {
	
	private Sampler sampler = null;
	private Renderer renderer = null;
	private Scene scene = null;
	private Film film = null;
	
	public RenderTask(Sampler sampler, Renderer renderer, Film film, Scene scene) {
		
		this.sampler = sampler;
		this.renderer = renderer;
		this.film = film;
		this.scene = scene;
	}
	
	public Sampler getSampler() {
		
		return sampler;
	}
	
	public Renderer getRenderer() {
		
		return renderer;
	}
	
	public Film getFilm() {
		
		return film;
	}
	
	public Scene getScene() {
		
		return scene;
	}
	
	/**
	 * Execute this RenderTask. Blocks until rendering is complete -- i.e., the
	 * configured {@link Sampler} has no more {@link Sample}s to provide.
	 */
	@Override
	public Image call() {
		
		renderer.render(sampler, film, scene);
		return film.getImage();
	}
	
}