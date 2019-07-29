package org.snowjak.rays.frontend.service;

import static org.apache.commons.math3.util.FastMath.min;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.RenderTask;
import org.snowjak.rays.Scene;
import org.snowjak.rays.Settings;
import org.snowjak.rays.camera.Camera;
import org.snowjak.rays.film.Film;
import org.snowjak.rays.film.Film.Image;
import org.snowjak.rays.frontend.messages.backend.ReceivedNewRenderResult;
import org.snowjak.rays.frontend.messages.backend.ReceivedRenderProgressUpdate;
import org.snowjak.rays.frontend.messages.backend.commands.RequestRenderCreationFromSingleJson;
import org.snowjak.rays.frontend.messages.backend.commands.RequestRenderDecomposition;
import org.snowjak.rays.frontend.messages.backend.commands.RequestRenderDeletion;
import org.snowjak.rays.frontend.messages.frontend.ReceivedRenderChildrenUpdate;
import org.snowjak.rays.frontend.messages.frontend.ReceivedRenderCreation;
import org.snowjak.rays.frontend.messages.frontend.ReceivedRenderDeletion;
import org.snowjak.rays.frontend.messages.frontend.ReceivedRenderUpdate;
import org.snowjak.rays.renderer.Renderer;
import org.snowjak.rays.sampler.Sampler;
import org.snowjak.rays.support.model.entity.Render;
import org.snowjak.rays.support.model.entity.RenderSetup;
import org.snowjak.rays.support.model.repository.RenderRepository;
import org.snowjak.rays.support.model.repository.RenderSetupRepository;
import org.snowjak.rays.support.model.repository.SceneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonParseException;

@Service
public class RenderUpdateService {
	
	private static final Logger LOG = LoggerFactory.getLogger(RenderUpdateService.class);
	
	private final EventBus bus;
	
	/**
	 * See {@link #getUpdateLock(UUID)}
	 */
	private final Map<UUID, Lock> renderUpdateLocks = Collections.synchronizedMap(new HashMap<>());
	
	@Autowired
	private RenderRepository renderRepository;
	
	@Autowired
	private RenderSetupRepository renderSetupRepository;
	
	@Autowired
	private SceneRepository sceneRepository;
	
	@Autowired
	public RenderUpdateService(EventBus bus) {
		
		this.bus = bus;
		
		bus.register(this);
	}
	
	/**
	 * Get the Render-update lock corresponding to the given Render UUID.
	 * <p>
	 * It is suggested that you obtain the lock for the Render you wish to update
	 * before beginning your update-actions.
	 * </p>
	 * 
	 * @param uuid
	 * @return
	 */
	private Lock getUpdateLock(UUID uuid) {
		
		return renderUpdateLocks.computeIfAbsent(uuid, (id) -> new ReentrantLock());
	}
	
	@Subscribe
	@AllowConcurrentEvents
	public void requestRenderCreationFromSingleJson(RequestRenderCreationFromSingleJson request) {
		
		LOG.info("Creating a new Render from a single JSON document.");
		
		final var createdUUID = saveNewRender(request.getJson());
		
		LOG.debug("Posting new ReceivedRenderCreation ...");
		bus.post(new ReceivedRenderCreation(renderRepository.findById(createdUUID.toString()).orElse(null)));
		
		LOG.debug("Finished creating a new Render from a single JSON document (UUID={}).", createdUUID.toString());
		
		if (request.hasNextInChain()) {
			request.getNextInChain().setContext(createdUUID);
			bus.post(request.getNextInChain());
		}
	}
	
	@Subscribe
	@AllowConcurrentEvents
	public void requestRenderDecomposition(RequestRenderDecomposition request) {
		
		LOG.info("Requesting render decomposition (UUID={}, region-size={}) ...", request.getUuid().toString(),
				request.getRegionSize());
		
		final var render = renderRepository.findById(request.getUuid().toString()).orElse(null);
		if (render == null) {
			LOG.warn("Cannot decompose given render (UUID={}) -- UUID not recognized.", request.getUuid().toString());
			return;
		}
		
		getUpdateLock(request.getUuid()).lock();
		
		renderRepository.save(render);
		bus.post(new ReceivedRenderUpdate(render));
		
		final var childIDs = decomposeRender(request.getUuid(), request.getRegionSize());
		
		getUpdateLock(request.getUuid()).unlock();
		
		childIDs.stream().map(childID -> renderRepository.findById(childID.toString()).orElse(null))
				.filter(r -> r != null).forEach(childRender -> bus.post(new ReceivedRenderCreation(childRender)));
		bus.post(new ReceivedRenderUpdate(renderRepository.findById(request.getUuid().toString()).orElse(null)));
		bus.post(
				new ReceivedRenderChildrenUpdate(renderRepository.findById(request.getUuid().toString()).orElse(null)));
		
		LOG.debug("Completed render-decomposition into {} child-Renders.", childIDs.size());
		
		if (request.hasNextInChain()) {
			request.getNextInChain().setContext(childIDs);
			bus.post(request.getNextInChain());
		}
	}
	
	@Subscribe
	@AllowConcurrentEvents
	public void requestRenderDeletion(RequestRenderDeletion request) {
		
		LOG.info("Requesting render deletion (UUID={})", request.getUuid().toString());
		
		final var render = renderRepository.findById(request.getUuid().toString()).orElse(null);
		if (render == null) {
			LOG.warn("Cannot delete given render (UUID={}) -- UUID not recognized.", request.getUuid().toString());
			return;
		}
		
		getUpdateLock(request.getUuid()).lock();
		
		renderRepository.delete(render);
		
		getUpdateLock(request.getUuid()).unlock();
		
		LOG.debug("Deleted render (UUID={}).", request.getUuid().toString());
		bus.post(new ReceivedRenderDeletion(render.getUuid()));
		
		if (request.hasNextInChain()) {
			bus.post(request.getNextInChain());
		}
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
	private Collection<UUID> decomposeRender(UUID uuid, int regionSize) {
		
		LOG.info("Decomposing Render (UUID={}) -- region-size = {}", uuid.toString(), regionSize);
		
		if (regionSize < 1) {
			LOG.warn("Cannot decompose Render (UUID={}) -- given region-size ({}) is not a positive integer!",
					uuid.toString(), regionSize);
			return Collections.emptyList();
		}
		
		var parentRender = renderRepository.findById(uuid.toString()).orElse(null);
		
		if (parentRender == null) {
			LOG.warn("Cannot decompose Render (UUID={}) -- UUID not recognized.", uuid.toString());
			return Collections.emptyList();
		}
		
		if (parentRender.getChildren().size() > 0) {
			LOG.warn("Cannot decompose Render (UUID={}) -- render has already been decomposed!", uuid.toString());
			return parentRender.getChildren().stream().map(Render::getUuid).map(UUID::fromString)
					.collect(Collectors.toList());
		}
		
		final var childIdList = new LinkedList<UUID>();
		
		LOG.trace("Retrieving Render setup ...");
		final var renderSetup = parentRender.getSetup();
		
		LOG.trace("Inflating Sampler from database ...");
		final var sampler = Settings.getInstance().getGson().fromJson(renderSetup.getSamplerJson(), Sampler.class);
		
		final var regionStartX = sampler.getXStart() + parentRender.getOffsetX();
		final var regionStartY = sampler.getYStart() + parentRender.getOffsetY();
		final var regionEndX = regionStartX + parentRender.getWidth() - 1;
		final var regionEndY = regionStartY + parentRender.getHeight() - 1;
		
		for (int x1 = regionStartX; x1 <= regionEndX; x1 += regionSize)
			for (int y1 = regionStartY; y1 <= regionEndY; y1 += regionSize) {
				
				final var x2 = min(x1 + regionSize - 1, sampler.getXEnd());
				final var y2 = min(y1 + regionSize - 1, sampler.getYEnd());
				
				final var subregionWidth = (x2 - x1) + 1;
				final var subregionHeight = (y2 - y1) + 1;
				
				LOG.trace("Decomposing (UUID={}) -- child render at [{},{}]-[{},{}]", uuid.toString(), x1, y1, x2, y2);
				
				final var childRenderId = saveNewRender(renderSetup.getId(), parentRender.getUuid(), x1, y1,
						subregionWidth, subregionHeight);
				LOG.debug("Created child render (UUID={})", childRenderId.toString());
				
				getUpdateLock(UUID.fromString(parentRender.getUuid())).lock();
				
				final var childRender = renderRepository.findById(childRenderId.toString()).get();
				parentRender.getChildren().add(childRender);
				parentRender = renderRepository.save(parentRender);
				
				getUpdateLock(UUID.fromString(parentRender.getUuid())).unlock();
				
				childIdList.add(childRenderId);
			}
		
		getUpdateLock(UUID.fromString(parentRender.getUuid())).lock();
		
		parentRender = renderRepository.save(parentRender);
		
		getUpdateLock(UUID.fromString(parentRender.getUuid())).unlock();
		
		return childIdList;
	}
	
	@Transactional
	private Long saveNewRenderSetup(String samplerJson, String rendererJson, String filmJson, String cameraJson,
			long sceneId) {
		
		final var foundScene = sceneRepository.findById(sceneId);
		if (!foundScene.isPresent())
			return null;
		
		final var scene = foundScene.get();
		
		var renderSetup = new RenderSetup();
		
		renderSetup.setSamplerJson(samplerJson);
		renderSetup.setRendererJson(rendererJson);
		renderSetup.setFilmJson(filmJson);
		renderSetup.setCameraJson(cameraJson);
		renderSetup.setScene(scene);
		
		renderSetup = renderSetupRepository.save(renderSetup);
		
		return renderSetup.getId();
	}
	
	@Transactional
	private UUID saveNewRender(String renderJson) throws JsonParseException {
		
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
		final var cameraJson = Settings.getInstance().getGson().toJson(renderTask.getCamera());
		final var sceneJson = Settings.getInstance().getGson().toJson(renderTask.getScene());
		
		var sceneEntity = new org.snowjak.rays.support.model.entity.Scene();
		sceneEntity.setJson(sceneJson);
		sceneEntity = sceneRepository.save(sceneEntity);
		LOG.debug("Saving bundled scene as a new Scene entry (ID={})", sceneEntity.getId());
		
		final var setupID = saveNewRenderSetup(samplerJson, rendererJson, filmJson, cameraJson, sceneEntity.getId());
		LOG.debug("Saving render configuration as a new RenderSetup (ID={})", setupID);
		
		final var renderSetup = renderSetupRepository.findById(setupID).get();
		
		final var renderWidth = renderSetup.inflateFilm().getWidth();
		final var renderHeight = renderSetup.inflateFilm().getHeight();
		
		LOG.debug("Saving render entry (offset = ({},{}), image = {}x{})", renderTask.getOffsetX(),
				renderTask.getOffsetY(), renderWidth, renderHeight);
		final var renderID = saveNewRender(setupID, null, renderTask.getOffsetX(), renderTask.getOffsetY(), renderWidth,
				renderHeight);
		
		LOG.info("Saved JSON as new Render+Scene.");
		return renderID;
	}
	
	@Transactional
	private UUID saveNewRender(long renderSetupId, String parentID, int offsetX, int offsetY, int width, int height) {
		
		final var foundSetup = renderSetupRepository.findById(renderSetupId);
		if (!foundSetup.isPresent())
			return null;
		
		final var setup = foundSetup.get();
		
		final var parentRender = (parentID == null) ? null : renderRepository.findById(parentID).orElse(null);
		
		var render = new Render();
		
		render.setParent(parentRender);
		render.setSetup(setup);
		render.setOffsetX(offsetX);
		render.setOffsetY(offsetY);
		render.setWidth(width);
		render.setHeight(height);
		
		LOG.trace("Saving new render with setup-ID {}, offset by ({},{}), and a {}x{} image", renderSetupId, offsetX,
				offsetY, width, height);
		
		render = renderRepository.save(render);
		
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
		
		getUpdateLock(uuid).lock();
		LOG.debug("UUID={}: Creating RenderTask from database ...", uuid.toString());
		
		final var renderEntity = renderRepository.findById(uuid.toString());
		if (!renderEntity.isPresent()) {
			LOG.warn("Cannot inflate RenderTask from database -- UUID={} is unrecognied.", uuid.toString());
			getUpdateLock(uuid).unlock();
			return null;
		}
		
		final var renderSetup = renderEntity.get().getSetup();
		
		LOG.trace("UUID={}: Inflating Sampler settings from JSON ...", uuid.toString());
		Sampler sampler = null;
		try {
			sampler = Settings.getInstance().getGson().fromJson(renderSetup.getSamplerJson(), Sampler.class);
		} catch (JsonParseException e) {
			getUpdateLock(uuid).unlock();
			throw new JsonParseException("Cannot inflate Sampler settings from Render(UUID = " + uuid.toString() + ")",
					e);
		}
		
		LOG.trace("UUID={}: Inflating Renderer settings from JSON ...", uuid.toString());
		Renderer renderer = null;
		try {
			renderer = Settings.getInstance().getGson().fromJson(renderSetup.getRendererJson(), Renderer.class);
		} catch (JsonParseException e) {
			getUpdateLock(uuid).unlock();
			throw new JsonParseException("Cannot inflate Renderer settings from Render(UUID = " + uuid.toString() + ")",
					e);
		}
		
		LOG.trace("UUID={}: Inflating Film settings from JSON ...", uuid.toString());
		Film film = null;
		try {
			film = Settings.getInstance().getGson().fromJson(renderSetup.getFilmJson(), Film.class);
		} catch (JsonParseException e) {
			getUpdateLock(uuid).unlock();
			throw new JsonParseException("Cannot inflate Film settings from Render(UUID = " + uuid.toString() + ")", e);
		}
		
		LOG.trace("UUID={}: Retrieving associated Scene ...", uuid.toString());
		final var sceneEntity = renderSetup.getScene();
		
		LOG.trace("UUID={}: Inflating Scene from JSON ...", uuid.toString());
		Scene scene = null;
		try {
			scene = Settings.getInstance().getGson().fromJson(sceneEntity.getJson(), Scene.class);
		} catch (JsonParseException e) {
			getUpdateLock(uuid).unlock();
			throw new JsonParseException("Cannot inflate Scene (ID = " + sceneEntity.getId() + ")", e);
		}
		
		LOG.trace("UUID={}: Inflating Camera from JSON ...", uuid.toString());
		Camera camera = null;
		try {
			camera = Settings.getInstance().getGson().fromJson(renderSetup.getCameraJson(), Camera.class);
		} catch (JsonParseException e) {
			getUpdateLock(uuid).unlock();
			throw new JsonParseException("Cannot inflate Camera settings from Render(UUID = " + uuid.toString() + ")",
					e);
		}
		
		//
		// Do we need to modify the Sampler window to fit the Render's specified
		// width/height and offset-X/offset-Y?
		//
		final var offsetX = renderEntity.get().getOffsetX();
		final var offsetY = renderEntity.get().getOffsetY();
		final var width = renderEntity.get().getWidth();
		final var height = renderEntity.get().getHeight();
		
		sampler = sampler.partition(offsetX, offsetY, offsetX + width - 1, offsetY + height - 1);
		film = film.partition(offsetX, offsetY, offsetX + width - 1, offsetY + height - 1);
		
		final var renderTask = new RenderTask(uuid, sampler, renderer, film, scene, camera,
				renderEntity.get().getOffsetX(), renderEntity.get().getOffsetY());
		
		getUpdateLock(uuid).unlock();
		LOG.debug("UUID={}: Created RenderTask from database.");
		return renderTask;
		
	}
	
	@Subscribe
	public void receiveProgressUpdate(ReceivedRenderProgressUpdate renderProgressUpdate) {
		
		LOG.debug("UUID={}: Received progress update ({}%)", renderProgressUpdate.getInfo().getUuid(),
				renderProgressUpdate.getInfo().getPercent());
		
		LOG.trace("UUID={}: Checking current progress so far ...", renderProgressUpdate.getInfo().getUuid());
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
			final var updatedRenders = updateRenderProgress(render.getUuid(),
					renderProgressUpdate.getInfo().getPercent());
			
			for (var r : updatedRenders)
				if (r.getPercentComplete() % 10 == 0) {
					LOG.debug("Posting new ReceivedRenderUpdate ...");
					bus.post(new ReceivedRenderUpdate(r));
				}
			
		} else {
			LOG.trace("UUID={}: Updated progress ({}%) is less than current progress ({}%)",
					renderProgressUpdate.getInfo().getUuid(), renderProgressUpdate.getInfo().getPercent(),
					render.getPercentComplete());
		}
	}
	
	@Subscribe
	public void receiveRenderResult(ReceivedNewRenderResult newRenderResult) {
		
		LOG.info("UUID={}: Received result.", newRenderResult.getImage().getUuid().toString());
		
		final Render render = renderRepository.findById(newRenderResult.getImage().getUuid().toString()).orElse(null);
		
		if (render == null) {
			LOG.error("Received result for UUID={}, which doesn't exist in the database!",
					newRenderResult.getImage().getUuid().toString());
			return;
		}
		
		markRenderAsComplete(render.getUuid(), true);
		
		try {
			
			final var updatedRenders = saveImageToDatabase(newRenderResult.getImage(), render.getUuid());
			
			for (var r : updatedRenders) {
				
				//
				// Ensure that the JPA entity has lazy-loaded the saved image
				//
				r.getPngBase64();
				
				LOG.debug("Posting new ReceivedRenderUpdate ...");
				bus.post(new ReceivedRenderUpdate(r));
			}
			
		} catch (IOException e) {
			LOG.error("Could not save image to database!", e);
		}
	}
	
	@Transactional
	public Collection<Render> updateRenderProgress(String renderID, int percent) {
		
		final var renderList = new LinkedList<Render>();
		
		LOG.trace("UUID={}: Updating progress to {}%.", renderID, percent);
		
		getUpdateLock(UUID.fromString(renderID)).lock();
		
		var render = renderRepository.findById(renderID).get();
		render.setPercentComplete(percent);
		render = renderRepository.save(render);
		
		getUpdateLock(UUID.fromString(renderID)).unlock();
		
		renderList.add(render);
		
		if (render.isChild()) {
			LOG.trace("UUID={}: Calculating progress-increase for parent ...", renderID);
			final var parent = render.getParent();
			
			final var totalChildProgress = parent.streamChildren().mapToDouble(c -> (double) c.getPercentComplete())
					.sum() / ((double) parent.getChildren().size());
			renderList.addAll(updateRenderProgress(parent.getUuid(), (int) totalChildProgress));
		}
		
		LOG.trace("UUID={}: Finished updating progress.", renderID);
		
		return renderList;
	}
	
	@Transactional
	public void markRenderAsSubmitted(String renderID) {
		
		getUpdateLock(UUID.fromString(renderID)).lock();
		
		final var render = renderRepository.findById(renderID).get();
		
		final var now = Instant.now();
		LOG.info("UUID={}: Marking as submitted @ {}", renderID, now.toString());
		render.setSubmitted(now);
		
		renderRepository.save(render);
		
		getUpdateLock(UUID.fromString(renderID)).unlock();
		
		bus.post(new ReceivedRenderUpdate(render));
		
		if (render.isChild()) {
			LOG.trace("UUID={}: Marking parent as submitted ...", renderID);
			markRenderAsSubmitted(render.getParent().getUuid());
		}
		LOG.trace("UUID={}: Finished marking as submitted.");
	}
	
	@Transactional
	public void markRenderAsComplete(String renderID, boolean complete) {
		
		final var render = renderRepository.findById(renderID).get();
		
		if (render.isParent()) {
			LOG.debug("UUID={}: Checking if all children complete ...");
			if (!render.getChildren().stream().allMatch(cr -> cr.getCompleted() != null)) {
				LOG.debug("UUID={}: Not all children are complete yet. Cannot mark parent as complete.");
				return;
			}
		}
		
		final var now = Instant.now();
		LOG.info("UUID={}: Marking as complete = {} @ {}", renderID, complete, now.toString());
		render.setCompleted((complete) ? now : null);
		
		renderRepository.save(render);
		
		if (render.isChild()) {
			LOG.trace("UUID={}: Marking parent as complete ...", renderID);
			markRenderAsComplete(render.getParent().getUuid(), complete);
		}
		LOG.trace("UUID={}: Finished marking as complete.");
		
	}
	
	@Transactional
	public Collection<Render> saveImageToDatabase(Image image, String renderID) throws IOException {
		
		final var render = renderRepository.findById(renderID).orElse(null);
		if (render == null)
			return Collections.emptyList();
		
		return saveImageToDatabase(image, renderID, render.getOffsetX(), render.getOffsetY());
	}
	
	/**
	 * Save the given image to the Render specified by the given (UU)ID. The given
	 * image occupies only part of this Render's total image, and is offset from the
	 * {@code (0,0)} point by {@code [offsetX, offsetY]}.
	 * 
	 * @param image
	 * @param renderID
	 * @param offsetX
	 * @param offsetY
	 * @return
	 * @throws IOException
	 */
	@Transactional
	public Collection<Render> saveImageToDatabase(Image image, String renderID, int offsetX, int offsetY)
			throws IOException {
		
		final var renderList = new LinkedList<Render>();
		
		if (image == null) {
			getUpdateLock(UUID.fromString(renderID)).lock();
			
			var render = renderRepository.findById(renderID).get();
			
			render.setPngBase64(null);
			render = renderRepository.save(render);
			
			renderList.add(render);
			
			getUpdateLock(UUID.fromString(renderID)).unlock();
			
			return renderList;
		}
		
		getUpdateLock(UUID.fromString(renderID)).lock();
		var render = renderRepository.findById(renderID).get();
		
		//
		//
		//
		
		final BufferedImage existingImage, sumBufferedImage;
		final Image sumImage;
		
		if (render.getPngBase64() != null) {
			LOG.info("UUID={}: Adding received image to existing image.", render.getUuid());
			LOG.trace("UUID={}: Decoding existing image as PNG ...", render.getUuid());
			existingImage = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(render.getPngBase64())));
		} else {
			LOG.info("UUID={}: Saving received image to database.", render.getUuid());
			existingImage = new BufferedImage(render.getWidth(), render.getHeight(), BufferedImage.TYPE_INT_ARGB);
		}
		
		LOG.trace("UUID={}: Retrieving new image ...", render.getUuid());
		final var newImage = image.getBufferedImage();
		
		LOG.trace("UUID={}: Allocating sum-image buffer ...", render.getUuid());
		sumBufferedImage = new BufferedImage(render.getWidth(), render.getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		LOG.trace("UUID={}: Painting existing and new images onto buffer ...", render.getUuid());
		LOG.trace("UUID={}: New image is offset by ({},{}) ...", render.getUuid(), offsetX - render.getOffsetX(),
				offsetY - render.getOffsetY());
		final var g = sumBufferedImage.getGraphics();
		g.drawImage(existingImage, 0, 0, null);
		g.drawImage(newImage, offsetX - render.getOffsetX(), offsetY - render.getOffsetY(), null);
		
		LOG.trace("UUID={}: Saving sum-image as PNG ...", render.getUuid());
		final var sumImageBuffer = new ByteArrayOutputStream();
		ImageIO.write(sumBufferedImage, "png", sumImageBuffer);
		
		render.setPngBase64(Base64.getEncoder().encodeToString(sumImageBuffer.toByteArray()));
		sumImage = new Image(sumBufferedImage, UUID.fromString(render.getUuid()));
		
		//
		//
		//
		if (render.isChild()) {
			LOG.info("Also adding image to parent Render ...");
			renderList.addAll(saveImageToDatabase(sumImage, render.getParent().getUuid(), render.getOffsetX(),
					render.getOffsetY()));
		}
		
		render = renderRepository.save(render);
		
		render.getPngBase64();
		renderList.add(render);
		
		getUpdateLock(UUID.fromString(renderID)).unlock();
		
		return renderList;
	}
	
}
