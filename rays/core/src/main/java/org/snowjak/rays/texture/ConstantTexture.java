package org.snowjak.rays.texture;

import org.snowjak.rays.interact.DescribesSurface;
import org.snowjak.rays.interact.SurfaceDescriptor;
import org.snowjak.rays.spectrum.colorspace.RGB;

/**
 * A ConstantTexture will always yield the same color.
 * 
 * @author snowjak88
 *
 */
public class ConstantTexture extends Texture {
	
	private final RGB rgb;
	
	public ConstantTexture(RGB rgb) {
		
		this.rgb = rgb;
	}
	
	@Override
	public <S extends DescribesSurface<S>> RGB getRGB(SurfaceDescriptor<S> surfaceDescriptor) {
		
		return rgb;
	}
	
}
