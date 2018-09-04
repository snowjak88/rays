package org.snowjak.rays.frontend.service;

import static org.apache.commons.math3.util.FastMath.max;
import static org.apache.commons.math3.util.FastMath.min;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.RenderTask;
import org.snowjak.rays.Scene;
import org.snowjak.rays.Settings;
import org.snowjak.rays.film.Film;
import org.snowjak.rays.film.Film.Image;
import org.snowjak.rays.frontend.events.Bus;
import org.snowjak.rays.frontend.messages.ReceivedNewRenderResult;
import org.snowjak.rays.frontend.messages.RenderCreated;
import org.snowjak.rays.frontend.messages.RenderProgressUpdate;
import org.snowjak.rays.frontend.messages.RenderUpdated;
import org.snowjak.rays.frontend.model.entity.Render;
import org.snowjak.rays.frontend.model.repository.RenderRepository;
import org.snowjak.rays.frontend.model.repository.SceneRepository;
import org.snowjak.rays.renderer.Renderer;
import org.snowjak.rays.sampler.Sampler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonParseException;

@Service
public class RenderUpdateService {
	
	private static final Logger LOG = LoggerFactory.getLogger(RenderUpdateService.class);
	
	@Autowired
	private RenderRepository renderRepository;
	
	@Autowired
	private SceneRepository sceneRepository;
	
	public RenderUpdateService() {
		
		Bus.get().register(this);
	}
	
	@Transactional
	public UUID saveNewRender(String renderJson) throws JsonParseException {
		
		LOG.info("Saving a new Render+Scene from a JSON descriptor.");
		
		LOG.trace("Inflating RenderTask from JSON ...");
		RenderTask renderTask = null;
		try {
			renderTask = Settings.getInstance().getGson().fromJson(renderJson, RenderTask.class);
		} catch (JsonParseException e) {
			throw new JsonParseException("Cannot inflate RenderTask from JSON.", e);
		}
		
		final var samplerJson = Settings.getInstance().getGson().toJson(renderTask.getSampler());
		final var rendererJson = Settings.getInstance().getGson().toJson(renderTask.getRenderer());
		final var filmJson = Settings.getInstance().getGson().toJson(renderTask.getFilm());
		final var sceneJson = Settings.getInstance().getGson().toJson(renderTask.getScene());
		
		var sceneEntity = new org.snowjak.rays.frontend.model.entity.Scene();
		sceneEntity.setJson(sceneJson);
		LOG.debug("Saving bundled scene as a new Scene entry (ID={})", sceneEntity.getId());
		sceneEntity = sceneRepository.save(sceneEntity);
		
		final var renderID = saveNewRender(samplerJson, rendererJson, filmJson, sceneEntity.getId());
		
		LOG.info("Saved JSON as new Render+Scene.");
		return renderID;
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
	 * @return a collection of all created child Render UUIDs
	 */
	@Transactional
	public Collection<UUID> decomposeRender(UUID uuid, int regionSize) {
		
		LOG.info("Decomposing Render (UUID={}) -- region-size = {}", uuid.toString(), regionSize);
		
		if (regionSize < 1) {
			LOG.warn("Cannot decompose Render (UUID={}) -- given region-size ({}) is not a positive integer!",
					uuid.toString(), regionSize);
			return Collections.emptyList();
		}
		
		final var parentRender = renderRepository.findById(uuid.toString()).orElse(null);
		final var childIdList = new LinkedList<UUID>();
		
		if (parentRender == null) {
			LOG.warn("Cannot decompose Render (UUID={}) -- UUID not recognized.", uuid.toString());
			return Collections.emptyList();
		}
		
		LOG.trace("Inflating Sampler from database ...");
		final var sampler = Settings.getInstance().getGson().fromJson(parentRender.getSamplerJson(), Sampler.class);
		
		for (int x1 = sampler.getXStart(); x1 <= sampler.getXEnd(); x1 += regionSize)
			for (int y1 = sampler.getYStart(); y1 <= sampler.getYEnd(); y1 += regionSize) {
				
				final var x2 = min(x1 + regionSize - 1, sampler.getXEnd());
				final var y2 = min(y1 + regionSize - 1, sampler.getYEnd());
				
				LOG.trace("Decomposing (UUID={}) -- child render at [{},{}]-[{},{}]", uuid.toString(), x1, y1, x2, y2);
				
				final var childSampler = sampler.partition(x1, y1, x2, y2);
				final var childSamplerJson = Settings.getInstance().getGson().toJson(childSampler);
				
				final var childRenderId = saveNewRender(childSamplerJson, parentRender.getRendererJson(),
						parentRender.getFilmJson(), parentRender.getScene().getId());
				LOG.debug("Created child render (UUID={})", childRenderId.toString());
				
				final var childRender = renderRepository.findById(childRenderId.toString()).get();
				parentRender.getChildren().add(childRender);
				childRender.setParent(parentRender);
				
				childIdList.add(childRenderId);
			}
		
		return childIdList;
	}
	
	private UUID saveNewRender(String samplerJson, String rendererJson, String filmJson, long sceneId) {
		
		final var foundScene = sceneRepository.findById(sceneId);
		if (!foundScene.isPresent())
			return null;
		
		final var scene = foundScene.get();
		
		var render = new Render();
		LOG.debug("Saving new Render entry (UUID={})", render.getUuid());
		render.setSamplerJson(samplerJson);
		render.setRendererJson(rendererJson);
		render.setFilmJson(filmJson);
		render.setScene(scene);
		render = renderRepository.save(render);
		
		Bus.get().post(new RenderCreated(UUID.fromString(render.getUuid())));
		
		return UUID.fromString(render.getUuid());
	}
	
	/**
	 * Convert a {@link Render} entity (represented by its UUID) into a
	 * {@link RenderTask}, or <code>null</code> if such conversion is not possible
	 * -- e.g., because the UUID is not recognized, or the Render/Scene entities
	 * have malformed JSON.
	 * 
	 * @param uuid
	 * @return
	 */
	@Transactional(readOnly = true)
	public RenderTask getRenderTask(UUID uuid) throws JsonParseException {
		
		LOG.debug("UUID={}: Creating RenderTask from database ...", uuid.toString());
		
		final var renderEntity = renderRepository.findById(uuid.toString());
		if (!renderEntity.isPresent()) {
			LOG.warn("Cannot inflate RenderTask from database -- UUID={} is unrecognied.", uuid.toString());
			return null;
		}
		
		LOG.trace("UUID={}: Inflating Sampler settings from JSON ...", uuid.toString());
		Sampler sampler = null;
		try {
			sampler = Settings.getInstance().getGson().fromJson(renderEntity.get().getSamplerJson(), Sampler.class);
		} catch (JsonParseException e) {
			throw new JsonParseException("Cannot inflate Sampler settings from Render(UUID = " + uuid.toString() + ")",
					e);
		}
		
		LOG.trace("UUID={}: Inflating Renderer settings from JSON ...", uuid.toString());
		Renderer renderer = null;
		try {
			renderer = Settings.getInstance().getGson().fromJson(renderEntity.get().getRendererJson(), Renderer.class);
		} catch (JsonParseException e) {
			throw new JsonParseException("Cannot inflate Renderer settings from Render(UUID = " + uuid.toString() + ")",
					e);
		}
		
		LOG.trace("UUID={}: Inflating Film settings from JSON ...", uuid.toString());
		Film film = null;
		try {
			film = Settings.getInstance().getGson().fromJson(renderEntity.get().getFilmJson(), Film.class);
		} catch (JsonParseException e) {
			throw new JsonParseException("Cannot inflate Film settings from Render(UUID = " + uuid.toString() + ")", e);
		}
		
		LOG.trace("UUID={}: Retrieving associated Scene ...", uuid.toString());
		final var sceneEntity = renderEntity.get().getScene();
		
		LOG.trace("UUID={}: Inflating Scene from JSON ...", uuid.toString());
		Scene scene = null;
		try {
			scene = Settings.getInstance().getGson().fromJson(sceneEntity.getJson(), Scene.class);
		} catch (JsonParseException e) {
			throw new JsonParseException("Cannot inflate Scene (ID = " + sceneEntity.getId() + ")", e);
		}
		
		final var renderTask = new RenderTask(uuid, sampler, renderer, film, scene);
		
		LOG.debug("UUID={}: Created RenderTask from database.");
		return renderTask;
		
	}
	
	@Subscribe
	@Transactional
	public void receiveProgressUpdate(RenderProgressUpdate renderProgressUpdate) {
		
		LOG.debug("UUID={}: Received progress update ({}%)", renderProgressUpdate.getInfo().getUuid(),
				renderProgressUpdate.getInfo().getPercent());
		
		LOG.trace("UUID={}: Checking current progress so far ...");
		final Render render = renderRepository.findById(renderProgressUpdate.getInfo().getUuid().toString())
				.orElse(null);
		
		if (render == null) {
			LOG.error("Received progress-update for UUID={}, which doesn't exist in the database!",
					renderProgressUpdate.getInfo().getUuid());
			return;
		}
		
		if (renderProgressUpdate.getInfo().getPercent() > render.getPercentComplete()) {
			LOG.trace("UUID={}: Updating progress to {}%", renderProgressUpdate.getInfo().getUuid(),
					renderProgressUpdate.getInfo().getPercent());
			render.setPercentComplete(renderProgressUpdate.getInfo().getPercent());
		} else {
			LOG.trace("UUID={}: Updated progress ({}%) is less than current progress ({}%)",
					renderProgressUpdate.getInfo().getUuid(), renderProgressUpdate.getInfo().getPercent(),
					render.getPercentComplete());
		}
		
		Bus.get().post(new RenderUpdated(UUID.fromString(render.getUuid())));
	}
	
	@Subscribe
	@Transactional
	public void receiveRenderResult(ReceivedNewRenderResult newRenderResult) {
		
		LOG.info("UUID={}: Received result.", newRenderResult.getImage().getUuid().toString());
		
		final Render render = renderRepository.findById(newRenderResult.getImage().getUuid().toString()).orElse(null);
		
		if (render == null) {
			LOG.error("Received result for UUID={}, which doesn't exist in the database!",
					newRenderResult.getImage().getUuid().toString());
			return;
		}
		
		try {
			saveImageToDatabase(newRenderResult.getImage(), render.getUuid());
		} catch (IOException e) {
			LOG.error("Could not save image to database!", e);
		}
		
		Bus.get().post(new RenderUpdated(UUID.fromString(render.getUuid())));
	}
	
	@Transactional
	private void saveImageToDatabase(Image result, String renderID) throws IOException {
		
		final var render = renderRepository.findById(renderID).get();
		
		LOG.debug("UUID={}: Checking if current render has a partial image to add to ...", render.getUuid());
		if (render.getPngBase64() != null) {
			
			LOG.info("UUID={}: Adding received image to existing image.", render.getUuid());
			
			LOG.trace("UUID={}: Decoding existing image as PNG ...", render.getUuid());
			final var existingImage = ImageIO
					.read(new ByteArrayInputStream(Base64.getDecoder().decode(render.getPngBase64())));
			
			LOG.trace("UUID={}: Retrieving new image ...", render.getUuid());
			final var newImage = result.getBufferedImage();
			
			LOG.trace("UUID={}: Allocating sum-image buffer ...", render.getUuid());
			final var sumImage = new BufferedImage(max(existingImage.getWidth(), newImage.getWidth()),
					max(existingImage.getHeight(), newImage.getHeight()), BufferedImage.TYPE_INT_ARGB);
			
			LOG.trace("UUID={}: Painting existing and new images onto buffer ...", render.getUuid());
			final var g = sumImage.getGraphics();
			g.drawImage(existingImage, 0, 0, null);
			g.drawImage(newImage, 0, 0, null);
			
			LOG.trace("UUID={}: Saving sum-image as PNG ...", render.getUuid());
			final var sumImageBuffer = new ByteArrayOutputStream();
			ImageIO.write(sumImage, "png", sumImageBuffer);
			
			ImageIO.write(sumImage, "png", new File("result.png"));
			
			render.setPngBase64(Base64.getEncoder().encodeToString(sumImageBuffer.toByteArray()));
			renderRepository.save(render);
			
		} else {
			
			LOG.info("UUID={}: Saving received image to database.", render.getUuid());
			
			render.setPngBase64(result.getPng());
			renderRepository.save(render);
			
		}
		
		if (render.isChild()) {
			LOG.info("Also adding image to parent Render ...");
			saveImageToDatabase(result, render.getParent().getUuid());
		}
	}
	
}
