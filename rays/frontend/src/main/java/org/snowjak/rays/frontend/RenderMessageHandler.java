package org.snowjak.rays.frontend;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.Settings;
import org.snowjak.rays.film.Film.Image;
import org.snowjak.rays.RenderTask.ProgressInfo;
import org.snowjak.rays.frontend.model.entity.Render;
import org.snowjak.rays.frontend.model.entity.Result;
import org.snowjak.rays.frontend.model.repository.RenderRepository;
import org.snowjak.rays.frontend.model.repository.ResultRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonParseException;

@Component
public class RenderMessageHandler {
	
	private static final Logger LOG = LoggerFactory.getLogger(RenderMessageHandler.class);
	
	@Autowired
	private RenderRepository renderRepository;
	
	@Autowired
	private ResultRepository resultRepository;
	
	@RabbitListener(queues = "${rabbitmq.progressq}")
	public void receiveProgress(String json) throws JsonParseException {
		
		final var progress = Settings.getInstance().getGson().fromJson(json, ProgressInfo.class);
		
		LOG.debug("UUID={}: Received progress update ({}%)", progress.getUuid(), progress.getPercent());
		
		LOG.trace("UUID={}: Checking current progress so far ...");
		final Optional<Render> render = renderRepository.findById(progress.getUuid().toString());
		
		if (!render.isPresent()) {
			LOG.error("Received progress-update for UUID={} which doesn't exist in the database!", progress.getUuid());
			return;
		}
		
		if (progress.getPercent() > render.get().getPercentComplete()) {
			LOG.debug("UUID={}: Updating progress to {}%", progress.getUuid(), progress.getPercent());
			render.get().setPercentComplete(progress.getPercent());
		} else {
			LOG.trace("UUID={}: Updated progress ({}%) is less than current progress ({}%)", progress.getUuid(),
					progress.getPercent(), render.get().getPercentComplete());
		}
	}
	
	@RabbitListener(queues = "${rabbitmq.resultq}")
	public void receiveResult(String json) throws JsonParseException {
		
		final var result = Settings.getInstance().getGson().fromJson(json, Image.class);
		
		LOG.info("UUID={}: Received result.", result.getUuid());
		
		final Optional<Render> render = renderRepository.findById(result.getUuid().toString());
		
		if (!render.isPresent()) {
			LOG.error("Received result for UUID={}, which doesn't exist in the database!", result.getUuid());
			return;
		}
		
		var resultEntity = new Result();
		resultEntity.setPngBase64(result.getPng());
		resultEntity.setRender(render.get());
		resultEntity = resultRepository.save(resultEntity);
		
		render.get().setResult(resultEntity);
	}
	
}
