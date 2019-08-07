package org.snowjak.rays.texture;

import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.annotations.UIType;
import org.snowjak.rays.interact.DescribesSurface;
import org.snowjak.rays.interact.SurfaceDescriptor;
import org.snowjak.rays.spectrum.colorspace.RGB;
import org.snowjak.rays.texture.mapping.TextureMapping;

/**
 * A ConstantTexture will always yield the same color.
 * 
 * @author snowjak88
 *
 */
@UIType(type = "constant", fields = { @UIField(name = "rgb", type = RGB.class),
		@UIField(name = "mapping", type = TextureMapping.class) })
public class ConstantTexture extends Texture {
	
	private RGB rgb;
	
	public ConstantTexture(RGB rgb) {
		
		this.rgb = rgb;
	}
	
	@Override
	public <S extends DescribesSurface<S>> RGB getRGB(SurfaceDescriptor<S> surfaceDescriptor) {
		
		return rgb;
	}
	
}
