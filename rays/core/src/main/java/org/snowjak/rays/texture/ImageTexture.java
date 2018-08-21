package org.snowjak.rays.texture;

import java.awt.image.BufferedImage;

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
public class ImageTexture extends Texture {
	
	private final BufferedImage image;
	
	public ImageTexture(BufferedImage image) {
		
		this(image, null);
	}
	
	public ImageTexture(BufferedImage image, TextureMapping textureMapping) {
		
		super(textureMapping);
		
		assert (image != null);
		this.image = image;
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
		
		final int imgX = (int) (imgPoint.getX() * (double) image.getWidth());
		final int imgY = (int) (imgPoint.getY() * (double) image.getHeight());
		
		if (imgX < 0 || imgX >= image.getWidth() || imgY < 0 || imgY >= image.getHeight())
			return RGB.BLACK;
		
		return RGB.fromPacked(image.getRGB(imgX, imgY));
	}
	
}
