package org.snowjak.rays.texture;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;

import javax.imageio.ImageIO;

import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.annotations.UIType;
import org.snowjak.rays.geometry.Point2D;
import org.snowjak.rays.interact.DescribesSurface;
import org.snowjak.rays.interact.SurfaceDescriptor;
import org.snowjak.rays.spectrum.colorspace.RGB;
import org.snowjak.rays.texture.mapping.TextureMapping;

/**
 * A {@link Texture} backed by an image.
 * <p>
 * </p>
 * 
 * @author snowjak88
 *
 */
@UIType(type = "image", fields = { @UIField(name = "url", type = String.class),
		@UIField(name = "mapping", type = TextureMapping.class) })
public class ImageTexture extends Texture {
	
	private String png;
	private URL url;
	private transient BufferedImage image;
	
	public ImageTexture(BufferedImage image) {
		
		this(image, null);
	}
	
	public ImageTexture(BufferedImage image, TextureMapping textureMapping) {
		
		super(textureMapping);
		
		try {
			
			final var imageDataOS = new ByteArrayOutputStream();
			ImageIO.write(image, "png", ImageIO.createImageOutputStream(imageDataOS));
			
			png = Base64.getEncoder().encodeToString(imageDataOS.toByteArray());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		assert (image != null);
		this.image = image;
	}
	
	public ImageTexture(String pngBase64) {
		
		this(pngBase64, null);
	}
	
	public ImageTexture(String pngBase64, TextureMapping textureMapping) {
		
		super(textureMapping);
		
		this.png = pngBase64;
	}
	
	public ImageTexture(URL image) {
		
		this(image, null);
	}
	
	public ImageTexture(URL image, TextureMapping textureMapping) {
		
		super(textureMapping);
		
		this.url = image;
		try {
			this.image = ImageIO.read(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * <strong>Note</strong> that, if after applying the configured TextureMapping,
	 * the resulting 2D coordinates lie outside the configured image, this method
	 * will return {@link RGB#BLACK}.
	 * </p>
	 */
	@Override
	public <S extends DescribesSurface<S>> RGB getRGB(SurfaceDescriptor<S> surfaceDescriptor) {
		
		final Point2D imgPoint = getTextureMapping().transform(surfaceDescriptor);
		
		final int imgX = (int) (imgPoint.getX() * (double) getImage().getWidth());
		final int imgY = (int) (imgPoint.getY() * (double) getImage().getHeight());
		
		if (imgX < 0 || imgX >= getImage().getWidth() || imgY < 0 || imgY >= getImage().getHeight())
			return RGB.BLACK;
		
		return RGB.fromPacked(getImage().getRGB(imgX, imgY));
	}
	
	public BufferedImage getImage() {
		
		if (image == null)
			try {
				
				if (this.png != null && !this.png.isEmpty()) {
					final var imageDataIS = new ByteArrayInputStream(Base64.getDecoder().decode(this.png));
					image = ImageIO.read(imageDataIS);
				}
				
				if (this.url != null) {
					image = ImageIO.read(url);
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		return image;
	}
	
}
