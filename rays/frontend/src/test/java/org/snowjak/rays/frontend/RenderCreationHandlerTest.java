package org.snowjak.rays.frontend;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.snowjak.rays.frontend.model.entity.Render;
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
		
		assertTrue(creationHandler.createRender(samplerJson, rendererJson, filmJson, sceneJson));
		
		verify(sceneRepository).save(any());
		verify(renderRepository).save(any());
	}
	
	@Test
	public void testSendRender() {
		
		final var scene = new Scene();
		scene.setJson(
				"{ \"primitives\": [ { \"shape\": { \"type\": \"sphere\", \"radius\": 0.5, \"worldToLocal\": [ { \"type\": \"translate\", \"dx\": -2 } ] }, \"material\": { \"type\": \"lambertian\", \"texture\": { \"type\": \"constant\", \"rgb\": [ 1, 0, 0 ] } } } ], \"lights\": [ { \"type\": \"point\", \"rgb\": [ 5, 5, 5 ], \"position\": { \"x\": 0, \"y\": 5, \"z\": 0 } } ], \"camera\": { \"type\": \"pinhole\", \"pixelWidth\": 4, \"pixelHeight\": 3, \"worldWidth\": 4, \"worldHeight\": 3, \"focalLength\": 6, \"worldToLocal\": [ { \"type\": \"translate\", \"dz\": -8 } ] } }");
		
		final var render = new Render();
		render.setUuid(UUID.randomUUID().toString());
		render.setSamplerJson(
				"{ \"type\": \"best-candidate\", \"xStart\": 0, \"yStart\": 0, \"xEnd\": 3, \"yEnd\": 2, \"samplesPerPixel\": 4, \"additional1DSamples\": 8, \"additional2DSamples\": 8 }");
		render.setRendererJson("{ \"type\": \"path-tracing\", \"maxDepth\": 4 }");
		render.setFilmJson("{ \"width\": 4, \"height\": 3, \"filter\": { \"type\": \"box\", \"extent\": 0 } }");
		render.setScene(scene);
		
		when(renderRepository.findById(any())).thenReturn(Optional.of(render));
		when(messageHandler.submitNewRender(any())).thenReturn(true);
		
		assertTrue(creationHandler.sendRender(UUID.fromString(render.getUuid())));
		
		verify(messageHandler).submitNewRender(any());
	}
	
	@Test
	public void testSendRender_parent() {
		
		final var scene = new Scene();
		scene.setJson(
				"{ \"primitives\": [ { \"shape\": { \"type\": \"sphere\", \"radius\": 0.5, \"worldToLocal\": [ { \"type\": \"translate\", \"dx\": -2 } ] }, \"material\": { \"type\": \"lambertian\", \"texture\": { \"type\": \"constant\", \"rgb\": [ 1, 0, 0 ] } } } ], \"lights\": [ { \"type\": \"point\", \"rgb\": [ 5, 5, 5 ], \"position\": { \"x\": 0, \"y\": 5, \"z\": 0 } } ], \"camera\": { \"type\": \"pinhole\", \"pixelWidth\": 4, \"pixelHeight\": 3, \"worldWidth\": 4, \"worldHeight\": 3, \"focalLength\": 6, \"worldToLocal\": [ { \"type\": \"translate\", \"dz\": -8 } ] } }");
		
		final var parent = new Render();
		parent.setSamplerJson(
				"{ \"type\": \"best-candidate\", \"xStart\": 0, \"yStart\": 0, \"xEnd\": 3, \"yEnd\": 2, \"samplesPerPixel\": 4, \"additional1DSamples\": 8, \"additional2DSamples\": 8 }");
		parent.setRendererJson("{ \"type\": \"path-tracing\", \"maxDepth\": 4 }");
		parent.setFilmJson("{ \"width\": 4, \"height\": 3, \"filter\": { \"type\": \"box\", \"extent\": 0 } }");
		parent.setScene(scene);
		
		final var child1 = new Render();
		child1.setSamplerJson(
				"{ \"type\": \"best-candidate\", \"xStart\": 0, \"yStart\": 0, \"xEnd\": 2, \"yEnd\": 2, \"samplesPerPixel\": 4, \"additional1DSamples\": 8, \"additional2DSamples\": 8 }");
		child1.setRendererJson(parent.getRendererJson());
		child1.setFilmJson(parent.getFilmJson());
		child1.setScene(parent.getScene());
		
		final var child2 = new Render();
		child2.setSamplerJson(
				"{ \"type\": \"best-candidate\", \"xStart\": 3, \"yStart\": 2, \"xEnd\": 3, \"yEnd\": 2, \"samplesPerPixel\": 4, \"additional1DSamples\": 8, \"additional2DSamples\": 8 }");
		child2.setRendererJson(parent.getRendererJson());
		child2.setFilmJson(parent.getFilmJson());
		child2.setScene(parent.getScene());
		
		parent.setChildren(Arrays.asList(child1, child2));
		child1.setParent(parent);
		child2.setParent(parent);
		
		when(renderRepository.findById(eq(parent.getUuid()))).thenReturn(Optional.of(parent));
		when(renderRepository.findById(eq(child1.getUuid()))).thenReturn(Optional.of(child1));
		when(renderRepository.findById(eq(child2.getUuid()))).thenReturn(Optional.of(child2));
		
		when(messageHandler.submitNewRender(any())).thenReturn(true);
		
		assertTrue(creationHandler.sendRender(UUID.fromString(parent.getUuid())));
		
		verify(messageHandler, times(2)).submitNewRender(any());
	}
	
}
