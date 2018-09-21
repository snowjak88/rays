package org.snowjak.rays.frontend.service;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.RenderTask.ProgressInfo;
import org.snowjak.rays.Settings;
import org.snowjak.rays.film.Film.Image;
import org.snowjak.rays.frontend.messages.backend.ReceivedNewRenderResult;
import org.snowjak.rays.frontend.messages.backend.ReceivedRenderProgressUpdate;
import org.snowjak.rays.frontend.messages.backend.commands.RequestMultipleRenderTaskSubmission;
import org.snowjak.rays.frontend.messages.backend.commands.RequestRenderDeletion;
import org.snowjak.rays.frontend.messages.backend.commands.RequestSingleRenderTaskSubmission;
import org.snowjak.rays.frontend.model.repository.RenderRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonParseException;

@Component
public class RabbitMessageHandler {
	
	private static final Logger LOG = LoggerFactory.getLogger(RabbitMessageHandler.class);
	
	private final EventBus bus;
	
	@Autowired
	private RabbitTemplate rabbit;
	
	@Value("${rabbitmq.taskq}")
	private String newRenderTaskQueue;
	
	@Value("${rabbitmq.deleteExchange}")
	private String renderDeletionExchange;
	
	@Autowired
	private RenderRepository renderRepository;
	
	@Autowired
	private RenderUpdateService renderUpdateService;
	
	@Autowired
	public RabbitMessageHandler(EventBus bus) {
		
		this.bus = bus;
		bus.register(this);
	}
	
	@Subscribe
	@AllowConcurrentEvents
	public void requestMultipleRenderTaskSubmission(RequestMultipleRenderTaskSubmission renderTaskSubmission) {
		
		LOG.info("Submitting {} new RenderTasks ...", renderTaskSubmission.getContext().size());
		
		for (var uuid : renderTaskSubmission.getContext()) {
			
			submitRenderTask(uuid);
			
			LOG.info("Completed submission for UUID={}.", uuid.toString());
			
			if (renderTaskSubmission.hasNextInChain())
				bus.post(renderTaskSubmission.getNextInChain());
			
		}
		
		LOG.debug("Finished submitting {} new RenderTasks.", renderTaskSubmission.getContext().size());
	}
	
	@Subscribe
	@AllowConcurrentEvents
	public void requestSingleRenderTaskSubmission(RequestSingleRenderTaskSubmission renderTaskSubmission) {
		
		LOG.info("New RenderTask submission request for UUID={} ...", renderTaskSubmission.getUuid().toString());
		
		submitRenderTask(renderTaskSubmission.getContext());
		
		LOG.info("Completed submission request for UUID={}.", renderTaskSubmission.getUuid().toString());
		
		if (renderTaskSubmission.hasNextInChain())
			bus.post(renderTaskSubmission.getNextInChain());
	}
	
	private void submitRenderTask(UUID uuid) {
		
		LOG.info("UUID={}: Submitting new RenderTask ...", uuid.toString());
		
		LOG.debug("UUID={}: Retrieving Render record from database ...", uuid.toString());
		final var entity = renderRepository.findById(uuid.toString()).orElse(null);
		if (entity == null) {
			LOG.error("Cannot submit new RenderTask -- cannot find Render entity (UUID={})", uuid.toString());
			return;
		}
		
		LOG.debug("UUID={}: Resetting render-progress to 0 ...", uuid.toString());
		renderUpdateService.updateRenderProgress(uuid.toString(), 0);
		
		LOG.debug("UUID={}: Setting as incomplete ...", uuid.toString());
		renderUpdateService.markRenderAsComplete(uuid.toString(), false);
		
		try {
			LOG.debug("UUID={}: Clearing any saved image ...", uuid.toString());
			renderUpdateService.saveImageToDatabase(null, uuid.toString());
		} catch (IOException e) {
			// Do nothing
		}
		
		if (entity.isParent()) {
			LOG.info("UUID={}: Render has {} child-Renders. Submitting children for processing ...", uuid.toString(),
					entity.getChildren().size());
			
			entity.getChildren().forEach(cr -> submitRenderTask(UUID.fromString(cr.getUuid())));
			
			LOG.info("UUID={}: All children submitted for processing.", uuid.toString());
			return;
		}
		
		LOG.debug("UUID={}: Inflating RenderTask from database ...", uuid.toString());
		final var task = renderUpdateService.getRenderTask(uuid);
		
		if (task == null) {
			LOG.error("Cannot submit new RenderTask -- cannot inflate RenderTask from Render entity (UUID={})",
					uuid.toString());
			return;
		}
		
		LOG.debug("UUID={}: Submitting new RenderTask ...", uuid.toString());
		
		LOG.debug("UUID={}: Converting to JSON ...", task.getUuid());
		final var json = Settings.getInstance().getGson().toJson(task);
		
		LOG.trace("UUID={}: JSON: {}", task.getUuid(), json);
		
		LOG.debug("UUID={}: Sending to RabbitMQ ...", task.getUuid());
		rabbit.convertAndSend(newRenderTaskQueue, json);
		
		LOG.debug("UUID={}: Marking as submitted ...", task.getUuid());
		renderUpdateService.markRenderAsSubmitted(task.getUuid().toString());
		
		LOG.info("UUID={}: Submitted new RenderTask.", uuid.toString());
	}
	
	@Subscribe
	public void handleRenderDeletion(RequestRenderDeletion request) {
		
		LOG.debug("Render (UUID={}) is being deleted -- notifying workers ...", request.getUuid());
		
		LOG.trace("Inflating render (UUID={}) from database ...", request.getUuid());
		final var entity = renderRepository.findById(request.getUuid().toString()).orElse(null);
		
		if (entity == null) {
			LOG.warn(
					"Render UUID={} not found in the database -- cannot handle explicit deletion of children. Child renders may be currently processing on workers.",
					request.getUuid());
		} else {
			
			if (!entity.getChildren().isEmpty()) {
				LOG.debug("Also signalling {} child-renders for deletion ...", entity.getChildren().size());
				entity.getChildren().forEach(
						c -> this.handleRenderDeletion(new RequestRenderDeletion(UUID.fromString(c.getUuid()))));
			}
		}
		
		rabbit.convertAndSend(renderDeletionExchange, "", request.getUuid());
	}
	
	@RabbitListener(queues = "${rabbitmq.progressq}")
	public void receiveProgress(String json) throws JsonParseException {
		
		LOG.trace("Received progress-notification: {}", json);
		final var progress = Settings.getInstance().getGson().fromJson(json, ProgressInfo.class);
		
		LOG.trace("UUID={}: Received progress update ({}%)", progress.getUuid(), progress.getPercent());
		
		bus.post(new ReceivedRenderProgressUpdate(progress));
	}
	
	@RabbitListener(queues = "${rabbitmq.resultq}")
	public void receiveResult(String json) throws JsonParseException {
		
		final var result = Settings.getInstance().getGson().fromJson(json, Image.class);
		
		bus.post(new ReceivedNewRenderResult(result));
	}
	
}
