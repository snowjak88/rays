package org.snowjak.rays.texture;

import org.snowjak.rays.interact.DescribesSurface;
import org.snowjak.rays.interact.SurfaceDescriptor;
import org.snowjak.rays.spectrum.Spectrum;
import org.snowjak.rays.spectrum.colorspace.RGB;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;
import org.snowjak.rays.texture.mapping.IdentityTextureMapping;
import org.snowjak.rays.texture.mapping.TextureMapping;

/**
 * A Texture maps {@link SurfaceDescriptor}s (usually via
 * {@link SurfaceDescriptor#getParam()}) into colors ({@link RGB} triplets).
 * Optionally, you may specify a {@link TextureMapping} (which defaults to
 * {@link IdentityTextureMapping}).
 * 
 * @author snowjak88
 *
 */
public abstract class Texture {
	
	private final TextureMapping mapping;
	private static final SpectralPowerDistribution WHITE_SPD = SpectralPowerDistribution.fromRGB(RGB.WHITE);
	
	/**
	 * Construct a new Texture with the default {@link IdentityTextureMapping}.
	 */
	public Texture() {
		
		this(null);
	}
	
	/**
	 * Construct a new Texture with the given {@link TextureMapping}.
	 * 
	 * @param textureMapping
	 */
	public Texture(TextureMapping textureMapping) {
		
		this.mapping = (textureMapping != null) ? textureMapping : new IdentityTextureMapping();
	}
	
	public TextureMapping getTextureMapping() {
		
		return mapping;
	}
	
	/**
	 * Get the color mapped to the given {@link SurfaceDescriptor} (using the
	 * configured {@link TextureMapping}, if any).
	 * 
	 * @param surfaceDescriptor
	 * @return
	 */
	public abstract <S extends DescribesSurface<S>> RGB getRGB(SurfaceDescriptor<S> surfaceDescriptor);
	
	/**
	 * Get the color mapped to the given {@link SurfaceDescriptor}, expressed as a
	 * {@link Spectrum}.
	 * <p>
	 * <strong>Note</strong> that the Spectrum returned by this method is
	 * <em>not</em> directly displayable, but instead must be multiplied by another
	 * Spectrum (giving the incident radiance) to yield a display-able value.
	 * </p>
	 * 
	 * @param interaction
	 * @return
	 */
	public <S extends DescribesSurface<S>> Spectrum getSpectrum(SurfaceDescriptor<S> surfaceDescriptor) {
		
		return SpectralPowerDistribution.fromRGB(getRGB(surfaceDescriptor)).divide(WHITE_SPD);
	}
	
}
