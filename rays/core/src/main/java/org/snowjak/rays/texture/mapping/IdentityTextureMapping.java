package org.snowjak.rays.texture.mapping;

import org.snowjak.rays.geometry.Point2D;
import org.snowjak.rays.interact.DescribesSurface;
import org.snowjak.rays.interact.SurfaceDescriptor;

/**
 * Represents an "identify" {@link TextureMapping} -- i.e., a "passthrough"
 * mapping that leaves 2-D coordinates unmodified (although it <em>does</em>
 * clamp them to {@code [0,1)}).
 * 
 * @author snowjak88
 *
 */
public class IdentityTextureMapping implements TextureMapping {
	
	@Override
	public <S extends DescribesSurface<S>> Point2D transform(SurfaceDescriptor<S> surfaceDescriptor) {
		
		return new Point2D(surfaceDescriptor.getParam().clamp(0, 1).getAll());
	}
	
}
