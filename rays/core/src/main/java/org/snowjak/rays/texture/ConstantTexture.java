package org.snowjak.rays.texture;

import org.snowjak.rays.spectrum.colorspace.RGB;

/**
 * A ConstantTexture will always yield the same color.
 * 
 * @author snowjak88
 *
 */
public class ConstantTexture {
	
	private final RGB rgb;
	
	public ConstantTexture(RGB rgb) {
		
		this.rgb = rgb;
	}
	
	public RGB getRgb() {
		
		return rgb;
	}
	
}
