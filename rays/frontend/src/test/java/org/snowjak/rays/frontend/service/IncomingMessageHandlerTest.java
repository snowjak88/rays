package org.snowjak.rays.frontend.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.snowjak.rays.frontend.model.entity.Render;
import org.snowjak.rays.frontend.model.repository.RenderRepository;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "rabbitmq.resultq=test-result", "rabbitmq.progressq=test-progress" })
public class IncomingMessageHandlerTest {
	
	@MockBean
	private RenderRepository renderRepository;
	
	@Autowired
	private IncomingMessageHandler messageHandler;
	
	@MockBean
	private AmqpAdmin amqpAdmin;
	
	@Test
	public void testReceiveProgress_update() {
		
		final var render = mock(Render.class);
		when(render.getUuid()).thenReturn("3a313f08-8262-4f45-8cf6-60b9cb13601b");
		when(render.getPercentComplete()).thenReturn(50);
		
		when(renderRepository.findById("3a313f08-8262-4f45-8cf6-60b9cb13601b")).thenReturn(Optional.of(render));
		
		messageHandler.receiveProgress("{\"uuid\":\"3a313f08-8262-4f45-8cf6-60b9cb13601b\",\"percent\":60}");
		
		verify(render).setPercentComplete(60);
	}
	
	@Test
	public void testReceiveProgress_noUpdate() {
		
		final var render = mock(Render.class);
		when(render.getUuid()).thenReturn("3a313f08-8262-4f45-8cf6-60b9cb13601b");
		when(render.getPercentComplete()).thenReturn(50);
		
		when(renderRepository.findById("3a313f08-8262-4f45-8cf6-60b9cb13601b")).thenReturn(Optional.of(render));
		
		messageHandler.receiveProgress("{\"uuid\":\"3a313f08-8262-4f45-8cf6-60b9cb13601b\",\"percent\":40}");
		
		verify(render, times(0)).setPercentComplete(40);
	}
	
	@Test
	public void testReceiveResult() {
		
		final var render = mock(Render.class);
		when(render.getUuid()).thenReturn("3a313f08-8262-4f45-8cf6-60b9cb13601b");
		when(render.getPercentComplete()).thenReturn(50);
		when(render.getPngBase64()).thenReturn(null);
		
		when(renderRepository.findById("3a313f08-8262-4f45-8cf6-60b9cb13601b")).thenReturn(Optional.of(render));
		
		messageHandler
				.receiveResult("{\"uuid\":\"3a313f08-8262-4f45-8cf6-60b9cb13601b\",\"png\":\"1234567890abcdef\"}");
		
		verify(render).setPngBase64("1234567890abcdef");
	}
	
}
