package org.snowjak.rays.frontend;

import static org.apache.commons.math3.util.FastMath.*;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.RenderTask;
import org.snowjak.rays.Scene;
import org.snowjak.rays.Settings;
import org.snowjak.rays.film.Film;
import org.snowjak.rays.frontend.model.entity.Render;
import org.snowjak.rays.frontend.model.repository.RenderRepository;
import org.snowjak.rays.frontend.model.repository.SceneRepository;
import org.snowjak.rays.renderer.Renderer;
import org.snowjak.rays.sampler.Sampler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RenderCreationHandler {
	
	private static final Logger LOG = LoggerFactory.getLogger(RenderCreationHandler.class);
	
	@Autowired
	private RenderRepository renderRepository;
	
	@Autowired
	private SceneRepository sceneRepository;
	
	@Autowired
	private RenderMessageHandler renderMessageHandler;
	
	public boolean createRender(String samplerJson, String rendererJson, String filmJson, String sceneJson) {
		
		final var sampler = Settings.getInstance().getGson().fromJson(samplerJson, Sampler.class);
		final var film = Settings.getInstance().getGson().fromJson(filmJson, Film.class);
		
		final var entity = new Render();
		entity.setWidth(film.getWidth());
		entity.setHeight(film.getHeight());
		entity.setSpp(sampler.getSamplesPerPixel());
		entity.setSamplerJson(samplerJson);
		entity.setRendererJson(rendererJson);
		entity.setFilmJson(filmJson);
		
		LOG.info("Persisting new scene ...");
		
		var sceneEntity = new org.snowjak.rays.frontend.model.entity.Scene();
		sceneEntity.setJson(sceneJson);
		sceneEntity = sceneRepository.save(sceneEntity);
		
		LOG.info("Persisting new render (UUID={}) ...", entity.getUuid());
		
		entity.setScene(sceneEntity);
		renderRepository.save(entity);
		
		return true;
	}
	
	/**
	 * Decompose the given Render (specified by its UUID) into child Renders, each
	 * covering a fraction of the total sampling-space.
	 * <p>
	 * This will divide the sampling-space up into square regions, each with a
	 * maximum size of {@code n}x{@code n} (where {@code n = regionSize}).
	 * </p>
	 * 
	 * @param uuid
	 * @param regionSize
	 */
	@Transactional
	public void decomposeRender(UUID uuid, int regionSize) {
		
		LOG.info("Decomposing Render (UUID={}) -- region-size = {}", uuid.toString(), regionSize);
		
		if (regionSize < 1) {
			LOG.warn("Cannot decompose Render (UUID={}) -- given region-size ({}) is not a positive integer!",
					uuid.toString(), regionSize);
			return;
		}
		
		final var parentRender = renderRepository.findById(uuid.toString()).orElse(null);
		
		if (parentRender == null) {
			LOG.warn("Cannot decompose Render (UUID={}) -- UUID not recognized.", uuid.toString());
			return;
		}
		
		LOG.trace("Inflating Sampler from database ...");
		final var sampler = Settings.getInstance().getGson().fromJson(parentRender.getSamplerJson(), Sampler.class);
		
		for (int x = sampler.getXStart(); x <= sampler.getXEnd(); x += regionSize)
			for (int y = sampler.getYStart(); y <= sampler.getYEnd(); y += regionSize) {
				
				LOG.trace("Decomposing (UUID={}) -- child render at [{},{}]-[{},{}]", uuid.toString(), x, y,
						x + regionSize - 1, y + regionSize - 1);
				
				final var xEnd = min(x + regionSize - 1, sampler.getXEnd());
				final var yEnd = min(y + regionSize - 1, sampler.getYEnd());
				
				final var childSampler = sampler.partition(x, y, xEnd, yEnd);
				
				final var child = new Render();
				child.setSamplerJson(Settings.getInstance().getGson().toJson(childSampler));
				child.setRendererJson(parentRender.getRendererJson());
				child.setFilmJson(parentRender.getFilmJson());
				child.setScene(parentRender.getScene());
				
				LOG.trace("Saving child Render (UUID={})", child.getUuid());
				renderRepository.save(child);
			}
		
	}
	
	@Transactional(readOnly = true)
	public RenderTask constructTask(UUID uuid) {
		
		LOG.info("Constructing RenderTask from database (UUID={})", uuid.toString());
		
		final var render = renderRepository.findById(uuid.toString()).orElse(null);
		
		if (render == null) {
			LOG.warn("Cannot construct RenderTask from database (UUID={}) -- UUID not recognized.", uuid.toString());
			return null;
		}
		
		LOG.trace("Inflating Sampler from JSON: {}", render.getSamplerJson());
		final var sampler = Settings.getInstance().getGson().fromJson(render.getSamplerJson(), Sampler.class);
		
		LOG.trace("Inflating Renderer from JSON: {}", render.getRendererJson());
		final var renderer = Settings.getInstance().getGson().fromJson(render.getRendererJson(), Renderer.class);
		
		LOG.trace("Inflating Film from JSON: {}", render.getFilmJson());
		final var film = Settings.getInstance().getGson().fromJson(render.getFilmJson(), Film.class);
		
		LOG.trace("Inflating Scene from JSON ...");
		final var scene = Settings.getInstance().getGson().fromJson(render.getScene().getJson(), Scene.class);
		
		return new RenderTask(uuid, sampler, renderer, film, scene);
	}
	
	@Transactional(readOnly = true)
	public boolean sendRender(UUID uuid) {
		
		final var render = renderRepository.findById(uuid.toString()).orElse(null);
		if (render == null) {
			LOG.error("Cannot send Render (UUID={}) -- UUID not recognized.", uuid.toString());
			return false;
		}
		
		if (render.isParent()) {
			
			LOG.info("Sending all children of parent Render (UUID={}) ...", uuid.toString());
			render.getChildren().forEach(r -> sendRender(UUID.fromString(r.getUuid())));
			LOG.info("Finished sending children of parent Render (UUID={})", uuid.toString());
			
			return true;
			
		}
		
		final var task = constructTask(uuid);
		
		if (task == null)
			return false;
		
		LOG.info("Sending RenderTask to AMQP server ...");
		if (!renderMessageHandler.submitNewRender(task)) {
			
			LOG.error("RenderTask submission unsuccessful.");
			return false;
		}
		
		return true;
	}
	
}
