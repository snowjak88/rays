package org.snowjak.rays;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.snowjak.rays.film.Film;
import org.snowjak.rays.film.Film.Image;
import org.snowjak.rays.renderer.Renderer;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.sampler.Sampler;

/**
 * Represents an entire rendering task.
 * <p>
 * This is capable of reporting its period progress (in the form of
 * {@link ProgressInfo} objects). To receive this reporting, you must call
 * {@link #setProgressConsumer(Consumer)} with an appropriate {@link Consumer}
 * instance. This object will then report every whole-number increase of
 * progress-percentage.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class RenderTask implements Callable<Image> {
	
	private UUID uuid = null;
	private Sampler sampler = null;
	private Renderer renderer = null;
	private Scene scene = null;
	private Film film = null;
	
	private transient Consumer<ProgressInfo> progressConsumer = null;
	
	public RenderTask(Sampler sampler, Renderer renderer, Film film, Scene scene) {
		
		this(UUID.randomUUID(), sampler, renderer, film, scene);
	}
	
	public RenderTask(UUID uuid, Sampler sampler, Renderer renderer, Film film, Scene scene) {
		
		this.uuid = uuid;
		this.sampler = sampler;
		this.renderer = renderer;
		this.film = film;
		this.scene = scene;
	}
	
	public Consumer<ProgressInfo> getProgressConsumer() {
		
		return progressConsumer;
	}
	
	public void setProgressConsumer(Consumer<ProgressInfo> progressConsumer) {
		
		this.progressConsumer = progressConsumer;
	}
	
	public UUID getUuid() {
		
		return uuid;
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
		
		final Consumer<Integer> consumer = (getProgressConsumer() == null) ? null
				: (progress) -> getProgressConsumer().accept(new ProgressInfo(getUuid(), progress));
		
		renderer.render(sampler, film, scene, consumer);
		return film.getImage(getUuid());
	}
	
	public static class ProgressInfo {
		
		private UUID uuid;
		private int percent;
		
		public ProgressInfo(UUID uuid, int percent) {
			
			this.uuid = uuid;
			this.percent = percent;
		}
		
		public UUID getUuid() {
			
			return uuid;
		}
		
		public int getPercent() {
			
			return percent;
		}
		
	}
	
}