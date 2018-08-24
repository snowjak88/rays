package org.snowjak.rays;

import java.awt.image.RenderedImage;
import java.util.concurrent.Callable;

import org.snowjak.rays.film.Film;
import org.snowjak.rays.filter.Filter;
import org.snowjak.rays.renderer.Renderer;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.sampler.Sampler;

/**
 * Represents an entire rendering task.
 * 
 * @author snowjak88
 *
 */
public class RenderTask implements Callable<RenderedImage> {
	
	private Sampler sampler = null;
	private Renderer renderer = null;
	private Scene scene = null;
	
	//
	//
	// Notice here -- the Film instance (which is pretty heavyweight) is transient,
	// and so not eligible for JSON serialization.
	// Instead, we save those fields with which we can recreate Film from scratch,
	// if need be.
	private int width, height;
	private Filter filter;
	
	private transient Film film = null;
	
	public RenderTask(Sampler sampler, Renderer renderer, Film film, Scene scene) {
		
		this.sampler = sampler;
		this.renderer = renderer;
		this.film = film;
		this.scene = scene;
		
		this.width = film.getWidth();
		this.height = film.getHeight();
		this.filter = film.getFilter();
	}
	
	public Sampler getSampler() {
		
		return sampler;
	}
	
	public Renderer getRenderer() {
		
		return renderer;
	}
	
	public Film getFilm() {
		
		if (film == null)
			this.film = new Film(width, height, filter);
		
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
	public RenderedImage call() {
		
		renderer.render(sampler, getFilm(), scene);
		return getFilm().getImage();
	}
	
}