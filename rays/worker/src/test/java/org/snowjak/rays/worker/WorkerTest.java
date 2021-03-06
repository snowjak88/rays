package org.snowjak.rays.worker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.snowjak.rays.Settings;
import org.snowjak.rays.film.Film.Image;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

@SpringBootTest
@TestPropertySource(properties = { "rabbitmq.resultq=test-result", "rabbitmq.progressq=" })
@RunWith(SpringRunner.class)
public class WorkerTest {
	
	@Configuration
	public static class Config {
		
		@Bean
		public RenderTaskReceiver renderTaskReceiver() {
			
			return new RenderTaskReceiver();
		}
		
		@Bean
		@Qualifier("renderTaskExecutor")
		public ListeningExecutorService renderTaskExecutor() {
			
			return MoreExecutors.newDirectExecutorService();
		}
		
		@Bean
		@Qualifier("renderResultExecutor")
		public ListeningExecutorService renderResultExecutor() {
			
			return MoreExecutors.newDirectExecutorService();
		}
	}
	
	@Autowired
	private RenderTaskReceiver receiver;
	
	@MockBean
	private RabbitTemplate rabbit;
	
	@Value("${rabbitmq.resultq}")
	private String resultQ;
	
	@Test
	public void testRenderTask() {
		
		String json = "";
		try (var reader = new BufferedReader(
				new InputStreamReader(WorkerTest.class.getClassLoader().getResourceAsStream("test-render.json")))) {
			
			final var jsonBuffer = new StringBuffer();
			while (reader.ready()) {
				jsonBuffer.append(reader.readLine());
				jsonBuffer.append(System.lineSeparator());
			}
			
			json = jsonBuffer.toString();
			
		} catch (IOException e) {
			fail("Unexpected exception: " + e.getClass().getName() + ": " + e.getMessage());
		}
		
		receiver.receive(json);
		
		final var resultImageCaptor = ArgumentCaptor.forClass(String.class);
		verify(rabbit).convertAndSend(eq("test-result"), resultImageCaptor.capture());
		
		assertNotNull(resultImageCaptor.getValue());
		
		final var resultImage = Settings.getInstance().getGson().fromJson(resultImageCaptor.getValue(), Image.class);
		assertNotNull(resultImage);
		assertEquals("3a313f08-8262-4f45-8cf6-60b9cb13601b", resultImage.getUuid().toString());
		
		try {
			final var png = resultImage.getBufferedImage();
			assertNotNull(png);
		} catch (Throwable t) {
			fail("Unexpected exception while converting result to PNG: " + t.getClass().getName() + ": "
					+ t.getMessage());
		}
	}
}
