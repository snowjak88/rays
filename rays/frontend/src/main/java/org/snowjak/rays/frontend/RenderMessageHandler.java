package org.snowjak.rays.frontend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.RenderTask;
import org.snowjak.rays.Settings;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

@Component
public class RenderMessageHandler {
	
	private static final Logger LOG = LoggerFactory.getLogger(RenderMessageHandler.class);
	
	@Autowired
	private RabbitTemplate rabbit;
	
	@Value("${rabbitmq.taskq}")
	private String newRenderTaskQueue;
	
	public boolean submitNewRender(RenderTask task) {
		
		LOG.info("UUID={}: Submitting new RenderTask ...", task.getUuid());
		
		LOG.debug("UUID={}: Converting to JSON ...", task.getUuid());
		final var json = Settings.getInstance().getGson().toJson(task);
		
		LOG.trace("UUID={}: JSON: {}", task.getUuid(), json);
		
		LOG.debug("UUID={}: Sending to RabbitMQ ...", task.getUuid());
		final var reply = rabbit.convertSendAndReceiveAsType(newRenderTaskQueue, json,
				new ParameterizedTypeReference<String>() {
				});
		
		if (reply == null || !reply.trim().isEmpty()) {
			LOG.error("UUID={}: Render-submission not successful -- worker returned error-code: {}", task.getUuid(),
					reply);
			return false;
		}
		
		LOG.info("UUID={}: Confirmed render-submission is successful.", task.getUuid());
		
		return true;
	}
	
}
