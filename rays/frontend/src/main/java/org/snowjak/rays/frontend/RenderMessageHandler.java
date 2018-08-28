package org.snowjak.rays.frontend;

import static org.apache.commons.math3.util.FastMath.max;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.RenderTask;
import org.snowjak.rays.RenderTask.ProgressInfo;
import org.snowjak.rays.Settings;
import org.snowjak.rays.film.Film.Image;
import org.snowjak.rays.frontend.events.RenderProgressUpdateEvent;
import org.snowjak.rays.frontend.events.RenderResultUpdateEvent;
import org.snowjak.rays.frontend.model.entity.Render;
import org.snowjak.rays.frontend.model.repository.RenderRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonParseException;

@Component
public class RenderMessageHandler {
	
	private static final Logger LOG = LoggerFactory.getLogger(RenderMessageHandler.class);
	
	@Autowired
	private RenderRepository renderRepository;
	
	@Autowired
	private ApplicationEventPublisher eventPublisher;
	
	@Autowired
	private RabbitTemplate rabbit;
	
	@Value("${rabbitmq.taskq}")
	private String newRenderTaskQueue;
	
	@RabbitListener(queues = "${rabbitmq.progressq}")
	@Transactional
	public void receiveProgress(String json) throws JsonParseException {
		
		final var progress = Settings.getInstance().getGson().fromJson(json, ProgressInfo.class);
		
		LOG.debug("UUID={}: Received progress update ({}%)", progress.getUuid(), progress.getPercent());
		
		LOG.trace("UUID={}: Checking current progress so far ...");
		final Render render = renderRepository.findById(progress.getUuid().toString()).orElse(null);
		
		if (render == null) {
			LOG.error("Received progress-update for UUID={}, which doesn't exist in the database!", progress.getUuid());
			return;
		}
		
		if (progress.getPercent() > render.getPercentComplete()) {
			LOG.debug("UUID={}: Updating progress to {}%", progress.getUuid(), progress.getPercent());
			render.setPercentComplete(progress.getPercent());
		} else {
			LOG.trace("UUID={}: Updated progress ({}%) is less than current progress ({}%)", progress.getUuid(),
					progress.getPercent(), render.getPercentComplete());
		}
		
		eventPublisher.publishEvent(new RenderProgressUpdateEvent(UUID.fromString(render.getUuid())));
	}
	
	@RabbitListener(queues = "${rabbitmq.resultq}")
	@Transactional
	public void receiveResult(String json) throws JsonParseException {
		
		final var result = Settings.getInstance().getGson().fromJson(json, Image.class);
		
		LOG.info("UUID={}: Received result.", result.getUuid());
		
		final Render render = renderRepository.findById(result.getUuid().toString()).orElse(null);
		
		if (render == null) {
			LOG.error("Received result for UUID={}, which doesn't exist in the database!", result.getUuid());
			return;
		}
		
		try {
			saveImageToDatabase(result, render);
		} catch (IOException e) {
			LOG.error("Could not save image to database!", e);
		}
		
		eventPublisher.publishEvent(new RenderResultUpdateEvent(UUID.fromString(render.getUuid())));
	}
	
	@Transactional
	private void saveImageToDatabase(Image result, Render render) throws IOException {
		
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
			
			render.setPngBase64(Base64.getEncoder().encodeToString(sumImageBuffer.toByteArray()));
			
		} else {
			
			LOG.info("UUID={}: Saving received image to database.", render.getUuid());
			
			render.setPngBase64(result.getPng());
		}
		
		if (render.isChild()) {
			LOG.info("Also adding image to parent Render ...");
			saveImageToDatabase(result, render.getParent());
		}
	}
	
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
