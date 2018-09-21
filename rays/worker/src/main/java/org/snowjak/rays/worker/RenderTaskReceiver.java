package org.snowjak.rays.worker;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.RenderTask;
import org.snowjak.rays.Settings;
import org.snowjak.rays.film.Film.Image;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.gson.JsonParseException;

@Component
public class RenderTaskReceiver {
	
	private static final Logger LOG = LoggerFactory.getLogger(RenderTaskReceiver.class);
	private final Map<UUID, ListenableFuture<Image>> futures = Collections.synchronizedMap(new HashMap<>());
	private final Set<UUID> deletedUUIDs = Collections.synchronizedSet(new HashSet<>());
	
	@Value("${rabbitmq.resultq}")
	private String renderResultQueueName = null;
	
	@Value("${rabbitmq.progressq}")
	private String renderProgressQueueName = null;
	
	@Value("${rabbitmq.taskq}")
	private String renderTaskQueueName;
	
	@Autowired
	@Qualifier("renderTaskExecutor")
	private ListeningExecutorService taskExecutor;
	
	@Autowired
	@Qualifier("renderResultExecutor")
	private ListeningExecutorService resultExecutor;
	
	@Autowired
	private RabbitTemplate rabbit;
	
	@RabbitListener(priority = "1", concurrency = "1", queues = "${rabbitmq.taskq}")
	public void receive(String taskJson) {
		
		try {
			LOG.info("Received new render-task.");
			
			LOG.trace("JSON: {}", taskJson);
			
			LOG.debug("Parsing from JSON ...");
			final var task = Settings.getInstance().getGson().fromJson(taskJson, RenderTask.class);
			
			if (deletedUUIDs.contains(task.getUuid())) {
				LOG.info("Received render-task is already flagged as to-be-deleted. Not executing.");
				deletedUUIDs.remove(task.getUuid());
				return;
			}
			
			if (renderProgressQueueName != null && !renderProgressQueueName.trim().isEmpty())
				task.setProgressConsumer((progress) -> {
					final var json = Settings.getInstance().getGson().toJson(progress);
					rabbit.convertAndSend(renderProgressQueueName, json);
				});
			
			LOG.debug("UUID={}: Parsed successfully", task.getUuid());
			
			LOG.debug("UUID={}: Submitting to executor ...", task.getUuid());
			final var future = taskExecutor.submit(task);
			futures.put(task.getUuid(), future);
			
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
			}, resultExecutor);
			
		} catch (JsonParseException e) {
			LOG.error("JSON -> RenderTask parse error!", e);
			throw e;
		}
		
	}
	
	@RabbitListener(priority = "2", concurrency = "1", bindings = @QueueBinding(value = @Queue(""), exchange = @Exchange(name = "${rabbitmq.deleteExchange}", type = "fanout")))
	public void receiveDelete(UUID uuid) {
		
		LOG.info("Received deletion for render UUID={}", uuid);
		
		if (!futures.containsKey(uuid)) {
			LOG.info("Received deletion for render UUID={}, but given UUID is not recognized! Saving for later.", uuid);
			deletedUUIDs.add(uuid);
			return;
		}
		
		final var future = futures.get(uuid);
		if (future.isDone()) {
			LOG.info("Cannot cancel render UUID={}: already done!", uuid);
			return;
		}
		
		if (future.isCancelled()) {
			LOG.info("Cannot cancel render UUID={}: already cancelled!", uuid);
			return;
		}
		
		if (future.cancel(true))
			LOG.info("Cancellation of render UUID={} was successful.", uuid);
		else
			LOG.info("Cancellation of render UUID={} was not successful!", uuid);
		
	}
	
}
