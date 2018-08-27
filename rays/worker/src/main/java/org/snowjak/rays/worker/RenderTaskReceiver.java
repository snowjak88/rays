package org.snowjak.rays.worker;

import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.RenderTask;
import org.snowjak.rays.Settings;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.gson.JsonParseException;

@Component
public class RenderTaskReceiver {
	
	private static final Logger LOG = LoggerFactory.getLogger(RenderTaskReceiver.class);
	
	@Value("${rabbitmq.resultq}")
	private String renderResultQueueName = null;
	
	@Value("${rabbitmq.progressq}")
	private String renderProgressQueueName = null;
	
	@Autowired
	private ListeningExecutorService executor;
	
	@Autowired
	private RabbitTemplate rabbit;
	
	public String receive(String taskJson) {
		
		try {
			LOG.info("Received new render-task.");
			LOG.debug("Parsing from JSON ...");
			final var task = Settings.getInstance().getGson().fromJson(taskJson, RenderTask.class);
			
			if (renderProgressQueueName != null && !renderProgressQueueName.trim().isEmpty())
				task.setProgressConsumer((progress) -> {
					rabbit.convertAndSend(renderProgressQueueName, progress);
				});
			
			LOG.debug("UUID={}: Parsed successfully", task.getUuid());
			
			LOG.debug("UUID={}: Submitting to executor ...", task.getUuid());
			final var future = executor.submit(task);
			
			future.addListener(() -> {
				try {
					LOG.info("UUID={}: Render complete", task.getUuid());
					LOG.debug("UUID={}: Retrieving result ...", task.getUuid());
					
					final var result = future.get();
					
					LOG.info("UUID={}: Sending result", result.getUuid());
					final var resultJson = Settings.getInstance().getGson().toJson(result);
					
					rabbit.convertAndSend(renderResultQueueName, resultJson);
					
				} catch (InterruptedException | ExecutionException e) {
					LOG.error("Error retrieving render-result!", e);
				}
			}, executor);
			
		} catch (JsonParseException e) {
			LOG.error("JSON -> RenderTask parse error!", e);
			return e.getMessage();
		}
		
		return null;
		
	}
	
}
