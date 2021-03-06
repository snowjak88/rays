package org.snowjak.rays.texture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Test;
import org.snowjak.rays.Settings;
import org.snowjak.rays.geometry.Point2D;
import org.snowjak.rays.interact.SurfaceDescriptor;
import org.snowjak.rays.shape.Shape;
import org.snowjak.rays.spectrum.colorspace.RGB;

public class ImageTextureTest {
	
	@Test
	public void testGetRGB() {
		
		try {
			
			final var img = ImageIO
					.read(ImageTextureTest.class.getClassLoader().getResourceAsStream("test-texture-2x2.png"));
			final var texture = new ImageTexture(img);
			
			final var s00 = new SurfaceDescriptor<Shape>(null, null, null, new Point2D(0.333, 0.333));
			final var s10 = new SurfaceDescriptor<Shape>(null, null, null, new Point2D(0.666, 0.333));
			final var s01 = new SurfaceDescriptor<Shape>(null, null, null, new Point2D(0.333, 0.666));
			final var s11 = new SurfaceDescriptor<Shape>(null, null, null, new Point2D(0.666, 0.666));
			
			assertEquals("(0,0)(R) is not as expected!", RGB.RED.getRed(), texture.getRGB(s00).getRed(), 0.005);
			assertEquals("(0,0)(G) is not as expected!", RGB.RED.getGreen(), texture.getRGB(s00).getGreen(), 0.005);
			assertEquals("(0,0)(B) is not as expected!", RGB.RED.getBlue(), texture.getRGB(s00).getBlue(), 0.005);
			
			assertEquals("(1,0)(R) is not as expected!", RGB.GREEN.getRed(), texture.getRGB(s10).getRed(), 0.005);
			assertEquals("(1,0)(G) is not as expected!", RGB.GREEN.getGreen(), texture.getRGB(s10).getGreen(), 0.005);
			assertEquals("(1,0)(B) is not as expected!", RGB.GREEN.getBlue(), texture.getRGB(s10).getBlue(), 0.005);
			
			assertEquals("(0,1)(R) is not as expected!", RGB.BLUE.getRed(), texture.getRGB(s01).getRed(), 0.005);
			assertEquals("(0,1)(G) is not as expected!", RGB.BLUE.getGreen(), texture.getRGB(s01).getGreen(), 0.005);
			assertEquals("(0,1)(B) is not as expected!", RGB.BLUE.getBlue(), texture.getRGB(s01).getBlue(), 0.005);
			
			assertEquals("(1,1)(R) is not as expected!", RGB.WHITE.getRed(), texture.getRGB(s11).getRed(), 0.005);
			assertEquals("(1,1)(G) is not as expected!", RGB.WHITE.getGreen(), texture.getRGB(s11).getGreen(), 0.005);
			assertEquals("(1,1)(B) is not as expected!", RGB.WHITE.getBlue(), texture.getRGB(s11).getBlue(), 0.005);
			
		} catch (IOException e) {
			fail("Unexpected exception: " + e.getClass().getSimpleName() + ": " + e.getMessage());
		}
	}
	
	@Test
	public void testDeserializePngData() {
		
		final var json = "{\"type\":\"image\",\"png\":\"iVBORw0KGgoAAAANSUhEUgAAAAIAAAACCAIAAAD91JpzAAAAEklEQVR4XmP4z8DAAMIM/4EAAB/uBfs4L1ZQAAAAAElFTkSuQmCC\",\"mapping\":{\"type\":\"identity\"}}";
		
		final var result = Settings.getInstance().getGson().fromJson(json, Texture.class);
		
		assertTrue(ImageTexture.class.isAssignableFrom(result.getClass()));
		final var imgText = (ImageTexture) result;
		
		final var s00 = new SurfaceDescriptor<Shape>(null, null, null, new Point2D(0.333, 0.333));
		final var s10 = new SurfaceDescriptor<Shape>(null, null, null, new Point2D(0.666, 0.333));
		final var s01 = new SurfaceDescriptor<Shape>(null, null, null, new Point2D(0.333, 0.666));
		final var s11 = new SurfaceDescriptor<Shape>(null, null, null, new Point2D(0.666, 0.666));
		
		assertEquals("(0,0)(R) is not as expected!", RGB.RED.getRed(), imgText.getRGB(s00).getRed(), 0.005);
		assertEquals("(0,0)(G) is not as expected!", RGB.RED.getGreen(), imgText.getRGB(s00).getGreen(), 0.005);
		assertEquals("(0,0)(B) is not as expected!", RGB.RED.getBlue(), imgText.getRGB(s00).getBlue(), 0.005);
		
		assertEquals("(1,0)(R) is not as expected!", RGB.GREEN.getRed(), imgText.getRGB(s10).getRed(), 0.005);
		assertEquals("(1,0)(G) is not as expected!", RGB.GREEN.getGreen(), imgText.getRGB(s10).getGreen(), 0.005);
		assertEquals("(1,0)(B) is not as expected!", RGB.GREEN.getBlue(), imgText.getRGB(s10).getBlue(), 0.005);
		
		assertEquals("(0,1)(R) is not as expected!", RGB.BLUE.getRed(), imgText.getRGB(s01).getRed(), 0.005);
		assertEquals("(0,1)(G) is not as expected!", RGB.BLUE.getGreen(), imgText.getRGB(s01).getGreen(), 0.005);
		assertEquals("(0,1)(B) is not as expected!", RGB.BLUE.getBlue(), imgText.getRGB(s01).getBlue(), 0.005);
		
		assertEquals("(1,1)(R) is not as expected!", RGB.WHITE.getRed(), imgText.getRGB(s11).getRed(), 0.005);
		assertEquals("(1,1)(G) is not as expected!", RGB.WHITE.getGreen(), imgText.getRGB(s11).getGreen(), 0.005);
		assertEquals("(1,1)(B) is not as expected!", RGB.WHITE.getBlue(), imgText.getRGB(s11).getBlue(), 0.005);
		
	}
	
	@Test
	public void testDeserializeUrl() {
		
		final var url = ImageTextureTest.class.getClassLoader().getResource("test-texture-2x2.png");
		final var json = String.format("{\"type\":\"image\",\"url\":\"%s\",\"mapping\":{\"type\":\"identity\"}}",
				url.toString());
		
		final var result = Settings.getInstance().getGson().fromJson(json, Texture.class);
		
		assertTrue(ImageTexture.class.isAssignableFrom(result.getClass()));
		final var imgText = (ImageTexture) result;
		
		final var s00 = new SurfaceDescriptor<Shape>(null, null, null, new Point2D(0.333, 0.333));
		final var s10 = new SurfaceDescriptor<Shape>(null, null, null, new Point2D(0.666, 0.333));
		final var s01 = new SurfaceDescriptor<Shape>(null, null, null, new Point2D(0.333, 0.666));
		final var s11 = new SurfaceDescriptor<Shape>(null, null, null, new Point2D(0.666, 0.666));
		
		assertEquals("(0,0)(R) is not as expected!", RGB.RED.getRed(), imgText.getRGB(s00).getRed(), 0.005);
		assertEquals("(0,0)(G) is not as expected!", RGB.RED.getGreen(), imgText.getRGB(s00).getGreen(), 0.005);
		assertEquals("(0,0)(B) is not as expected!", RGB.RED.getBlue(), imgText.getRGB(s00).getBlue(), 0.005);
		
		assertEquals("(1,0)(R) is not as expected!", RGB.GREEN.getRed(), imgText.getRGB(s10).getRed(), 0.005);
		assertEquals("(1,0)(G) is not as expected!", RGB.GREEN.getGreen(), imgText.getRGB(s10).getGreen(), 0.005);
		assertEquals("(1,0)(B) is not as expected!", RGB.GREEN.getBlue(), imgText.getRGB(s10).getBlue(), 0.005);
		
		assertEquals("(0,1)(R) is not as expected!", RGB.BLUE.getRed(), imgText.getRGB(s01).getRed(), 0.005);
		assertEquals("(0,1)(G) is not as expected!", RGB.BLUE.getGreen(), imgText.getRGB(s01).getGreen(), 0.005);
		assertEquals("(0,1)(B) is not as expected!", RGB.BLUE.getBlue(), imgText.getRGB(s01).getBlue(), 0.005);
		
		assertEquals("(1,1)(R) is not as expected!", RGB.WHITE.getRed(), imgText.getRGB(s11).getRed(), 0.005);
		assertEquals("(1,1)(G) is not as expected!", RGB.WHITE.getGreen(), imgText.getRGB(s11).getGreen(), 0.005);
		assertEquals("(1,1)(B) is not as expected!", RGB.WHITE.getBlue(), imgText.getRGB(s11).getBlue(), 0.005);
		
	}
}
