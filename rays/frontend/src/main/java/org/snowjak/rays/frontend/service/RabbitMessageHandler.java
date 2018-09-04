package org.snowjak.rays.frontend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.RenderTask.ProgressInfo;
import org.snowjak.rays.Settings;
import org.snowjak.rays.film.Film.Image;
import org.snowjak.rays.frontend.events.Bus;
import org.snowjak.rays.frontend.messages.ReceivedNewRenderResult;
import org.snowjak.rays.frontend.messages.RenderProgressUpdate;
import org.snowjak.rays.frontend.messages.RequestRenderTaskSubmission;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonParseException;

@Component
public class RabbitMessageHandler {
	
	private static final Logger LOG = LoggerFactory.getLogger(RabbitMessageHandler.class);
	
	@Autowired
	private RabbitTemplate rabbit;
	
	@Value("${rabbitmq.taskq}")
	private String newRenderTaskQueue;
	
	@Autowired
	private RenderUpdateService renderUpdateService;
	
	public RabbitMessageHandler() {
		
		Bus.get().register(this);
	}
	
	@Subscribe
	public void sendRenderTask(RequestRenderTaskSubmission renderTaskSubmission) {
		
		LOG.info("UUID={}: Submitting new RenderTask ...", renderTaskSubmission.getUuid().toString());
		
		LOG.debug("UUID={}: Inflating RenderTask from database ...", renderTaskSubmission.getUuid().toString());
		final var task = renderUpdateService.getRenderTask(renderTaskSubmission.getUuid());
		
		LOG.debug("UUID={}: Converting to JSON ...", task.getUuid());
		final var json = Settings.getInstance().getGson().toJson(task);
		
		LOG.trace("UUID={}: JSON: {}", task.getUuid(), json);
		
		LOG.debug("UUID={}: Sending to RabbitMQ ...", task.getUuid());
		final var reply = rabbit.convertSendAndReceive(newRenderTaskQueue, json);
		
		if (reply instanceof Throwable) {
			LOG.error("UUID={}: Render-submission not successful -- worker returned error-code: {}", task.getUuid(),
					(Throwable) reply);
		}
		
		else if (reply instanceof String && ((String) reply).trim().equals("(successful)")) {
			LOG.info("UUID={}: Confirmed render-submission is successful.", task.getUuid());
		}
		
		else {
			LOG.info("UUID={}: Received unknown reply: {}", reply.toString());
		}
	}
	
	@RabbitListener(queues = "${rabbitmq.progressq}")
	public void receiveProgress(String json) throws JsonParseException {
		
		LOG.trace("Received progress-notification: {}", json);
		final var progress = Settings.getInstance().getGson().fromJson(json, ProgressInfo.class);
		
		LOG.trace("UUID={}: Received progress update ({}%)", progress.getUuid(), progress.getPercent());
		
		Bus.get().post(new RenderProgressUpdate(progress));
	}
	
	@RabbitListener(queues = "${rabbitmq.resultq}")
	public void receiveResult(String json) throws JsonParseException {
		
		final var result = Settings.getInstance().getGson().fromJson(json, Image.class);
		
		Bus.get().post(new ReceivedNewRenderResult(result));
	}
	
}
