package org.snowjak.rays.frontend;

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

import com.google.gson.JsonParseException;

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
		
		try {
			LOG.info("Creating new Render from JSON descriptors.");
			
			LOG.debug("Inflating Render objects from given JSON descriptors.");
			
			LOG.trace("Inflating Sampler from JSON: {}", samplerJson);
			final var sampler = Settings.getInstance().getGson().fromJson(samplerJson, Sampler.class);
			
			LOG.trace("Inflating Renderer from JSON: {}", rendererJson);
			final var renderer = Settings.getInstance().getGson().fromJson(rendererJson, Renderer.class);
			
			LOG.trace("Inflating Film from JSON: {}", filmJson);
			final var film = Settings.getInstance().getGson().fromJson(filmJson, Film.class);
			
			LOG.trace("Inflating Scene from JSON ...");
			final var scene = Settings.getInstance().getGson().fromJson(sceneJson, Scene.class);
			
			LOG.debug("Creating new RenderTask ..");
			final RenderTask task = new RenderTask(sampler, renderer, film, scene);
			
			final var entity = new Render();
			entity.setUuid(task.getUuid().toString());
			entity.setSamplerJson(samplerJson);
			entity.setRendererJson(rendererJson);
			entity.setFilmJson(filmJson);
			
			LOG.info("Persisting new scene ...");
			
			var sceneEntity = new org.snowjak.rays.frontend.model.entity.Scene();
			sceneEntity.setJson(sceneJson);
			sceneEntity = sceneRepository.save(sceneEntity);
			
			LOG.info("Persisting new render (UUID={}) ...", task.getUuid());
			
			entity.setScene(sceneEntity);
			renderRepository.save(entity);
			
			LOG.info("Sending RenderTask to AMQP server ...");
			if (!renderMessageHandler.submitNewRender(task)) {
				
				LOG.error("RenderTask submission unsuccessful.");
				return false;
			}
			
		} catch (JsonParseException e) {
			return false;
		}
		
		return true;
	}
	
}
