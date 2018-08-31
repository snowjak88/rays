package org.snowjak.rays;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.annotations.UIType;
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
@UIType(fields = { @UIField(name = "sampler", type = Sampler.class), @UIField(name = "renderer", type = Renderer.class),
		@UIField(name = "film", type = Film.class), @UIField(name = "scene", type = Scene.class) })
public class RenderTask implements Callable<Image> {
	
	private static final Logger LOG = System.getLogger(RenderTask.class.getName());
	
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
		
		LOG.log(Level.INFO, "Executing RenderTask:");
		LOG.log(Level.INFO, "UUID = {0}", getUuid().toString());
		LOG.log(Level.INFO, "Sampler {0} ({1}x{2} @ {3}spp)", getSampler().getClass().getSimpleName(),
				getSampler().getXEnd() - getSampler().getXStart() + 1d,
				getSampler().getYEnd() - getSampler().getYStart() + 1d, getSampler().getSamplesPerPixel());
		LOG.log(Level.INFO, "Renderer = {0}", getRenderer().getClass().getSimpleName());
		LOG.log(Level.INFO, "Film = {0}", getFilm().getClass().getSimpleName());
		LOG.log(Level.INFO, "Scene: {0} primitives, {1} lights",
				getScene().getAccelerationStructure().getPrimitives().size(), getScene().getLights().size());
		LOG.log(Level.INFO, "Camera: {0}", getScene().getCamera().getClass().getSimpleName());
		LOG.log(Level.INFO, "Reporting progress? == {0}", (consumer != null));
		
		renderer.render(sampler, film, scene, consumer);
		
		LOG.log(Level.INFO, "RenderTask complete! UUID={0}", getUuid());
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