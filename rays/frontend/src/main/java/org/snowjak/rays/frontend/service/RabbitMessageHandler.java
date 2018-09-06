package org.snowjak.rays.frontend.service;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.RenderTask.ProgressInfo;
import org.snowjak.rays.Settings;
import org.snowjak.rays.film.Film.Image;
import org.snowjak.rays.frontend.messages.backend.ReceivedNewRenderResult;
import org.snowjak.rays.frontend.messages.backend.ReceivedRenderProgressUpdate;
import org.snowjak.rays.frontend.messages.backend.commands.RequestMultipleRenderTaskSubmission;
import org.snowjak.rays.frontend.messages.backend.commands.RequestSingleRenderTaskSubmission;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
	
	@Autowired
	private RenderUpdateService renderUpdateService;
	
	@Autowired
	public RabbitMessageHandler(@Qualifier("backendEventBus") EventBus bus) {
		
		this.bus = bus;
		bus.register(this);
	}
	
	@Subscribe
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
	public void requestSingleRenderTaskSubmission(RequestSingleRenderTaskSubmission renderTaskSubmission) {
		
		LOG.info("New RenderTask submission request for UUID={} ...", renderTaskSubmission.getUuid().toString());
		
		submitRenderTask(renderTaskSubmission.getContext());
		
		LOG.info("Completed submission request for UUID={}.", renderTaskSubmission.getUuid().toString());
		
		if (renderTaskSubmission.hasNextInChain())
			bus.post(renderTaskSubmission.getNextInChain());
	}
	
	public void submitRenderTask(UUID uuid) {
		
		LOG.info("UUID={}: Submitting new RenderTask ...", uuid.toString());
		
		LOG.debug("UUID={}: Inflating RenderTask from database ...", uuid.toString());
		final var task = renderUpdateService.getRenderTask(uuid);
		
		LOG.debug("UUID={}: Submitting new RenderTask ...", uuid.toString());
		
		LOG.debug("UUID={}: Converting to JSON ...", task.getUuid());
		final var json = Settings.getInstance().getGson().toJson(task);
		
		LOG.trace("UUID={}: JSON: {}", task.getUuid(), json);
		
		LOG.debug("UUID={}: Sending to RabbitMQ ...", task.getUuid());
		rabbit.convertAndSend(newRenderTaskQueue, json);
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
