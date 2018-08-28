package org.snowjak.rays.frontend;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.snowjak.rays.frontend.model.entity.Scene;
import org.snowjak.rays.frontend.model.repository.RenderRepository;
import org.snowjak.rays.frontend.model.repository.SceneRepository;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(properties = { "rabbitmq.taskq=test-task" })
public class RenderCreationHandlerTest {
	
	@MockBean
	private RenderRepository renderRepository;
	
	@MockBean
	private SceneRepository sceneRepository;
	
	@MockBean
	private RenderMessageHandler messageHandler;
	
	@Autowired
	private RenderCreationHandler creationHandler;
	
	@MockBean
	private AmqpAdmin amqpAdmin;
	
	@Test
	public void testCreate_shallowSuccessful() {
		
		final var samplerJson = "{ \"type\": \"best-candidate\", \"xStart\": 0, \"yStart\": 0, \"xEnd\": 3, \"yEnd\": 2, \"samplesPerPixel\": 4, \"additional1DSamples\": 8, \"additional2DSamples\": 8 }";
		final var rendererJson = "{ \"type\": \"path-tracing\", \"maxDepth\": 4 }";
		final var filmJson = "{ \"width\": 4, \"height\": 3, \"filter\": { \"type\": \"box\", \"extent\": 0 } }";
		final var sceneJson = "{ \"primitives\": [ { \"shape\": { \"type\": \"sphere\", \"radius\": 0.5, \"worldToLocal\": [ { \"type\": \"translate\", \"dx\": -2 } ] }, \"material\": { \"type\": \"lambertian\", \"texture\": { \"type\": \"constant\", \"rgb\": [ 1, 0, 0 ] } } } ], \"lights\": [ { \"type\": \"point\", \"rgb\": [ 5, 5, 5 ], \"position\": { \"x\": 0, \"y\": 5, \"z\": 0 } } ], \"camera\": { \"type\": \"pinhole\", \"pixelWidth\": 4, \"pixelHeight\": 3, \"worldWidth\": 4, \"worldHeight\": 3, \"focalLength\": 6, \"worldToLocal\": [ { \"type\": \"translate\", \"dz\": -8 } ] } }";
		
		final var scene = new Scene();
		scene.setId(1);
		scene.setVersion(1);
		scene.setJson(sceneJson);
		
		when(sceneRepository.save(any())).thenReturn(scene);
		
		when(messageHandler.submitNewRender(any())).thenReturn(true);
		
		assertTrue(creationHandler.createRender(samplerJson, rendererJson, filmJson, sceneJson));
		
		verify(sceneRepository).save(any());
		verify(renderRepository).save(any());
		verify(messageHandler).submitNewRender(any());
	}
	
	@Test
	public void testCreate_shallowUnsuccessful() {
		
		final var samplerJson = "{ \"type\": \"best-candidate\", \"xStart\": 0, \"yStart\": 0, \"xEnd\": 3, \"yEnd\": 2, \"samplesPerPixel\": 4, \"additional1DSamples\": 8, \"additional2DSamples\": 8 }";
		final var rendererJson = "{ \"type\": \"path-tracing\", \"maxDepth\": 4 }";
		final var filmJson = " \"width\": 4, \"height\": 3, \"filter\": { \"type\": \"box\", \"extent\": 0 } }";
		final var sceneJson = "{ \"primitives\": [ { \"shape\": { \"type\": \"sphere\", \"radius\": 0.5, \"worldToLocal\": [ { \"type\": \"translate\", \"dx\": -2 } ] }, \"material\": { \"type\": \"lambertian\", \"texture\": { \"type\": \"constant\", \"rgb\": [ 1, 0, 0 ] } } } ], \"lights\": [ { \"type\": \"point\", \"rgb\": [ 5, 5, 5 ], \"position\": { \"x\": 0, \"y\": 5, \"z\": 0 } } ], \"camera\": { \"type\": \"pinhole\", \"pixelWidth\": 4, \"pixelHeight\": 3, \"worldWidth\": 4, \"worldHeight\": 3, \"focalLength\": 6, \"worldToLocal\": [ { \"type\": \"translate\", \"dz\": -8 } ] } }";
		
		final var scene = new Scene();
		scene.setId(1);
		scene.setVersion(1);
		scene.setJson(sceneJson);
		
		when(sceneRepository.save(any())).thenReturn(scene);
		
		when(messageHandler.submitNewRender(any())).thenReturn(true);
		
		assertFalse(creationHandler.createRender(samplerJson, rendererJson, filmJson, sceneJson));
		
		verify(sceneRepository, times(0)).save(any());
		verify(renderRepository, times(0)).save(any());
		verify(messageHandler, times(0)).submitNewRender(any());
	}
	
}
