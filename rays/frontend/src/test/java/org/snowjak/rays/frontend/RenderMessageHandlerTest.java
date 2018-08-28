package org.snowjak.rays.frontend;

import static org.mockito.Mockito.*;

import java.util.Optional;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.snowjak.rays.frontend.model.entity.Render;
import org.snowjak.rays.frontend.model.entity.Result;
import org.snowjak.rays.frontend.model.repository.RenderRepository;
import org.snowjak.rays.frontend.model.repository.ResultRepository;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "rabbitmq.resultq=test-result", "rabbitmq.progressq=test-progress" })
public class RenderMessageHandlerTest {
	
	@MockBean
	private RenderRepository renderRepository;
	
	@MockBean
	private ResultRepository resultRepository;
	
	@Autowired
	private RenderMessageHandler messageHandler;
	
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
		when(render.getResult()).thenReturn(null);
		
		when(renderRepository.findById("3a313f08-8262-4f45-8cf6-60b9cb13601b")).thenReturn(Optional.of(render));
		
		messageHandler.receiveResult("{\"uuid\":\"3a313f08-8262-4f45-8cf6-60b9cb13601b\",\"png\":\"1234567890abcdef\"}");
		
		final var resultCaptor = ArgumentCaptor.forClass(Result.class);
		
		verify(resultRepository).save(resultCaptor.capture());
		assertEquals("1234567890abcdef", resultCaptor.getValue().getPngBase64());
	}
	
}
