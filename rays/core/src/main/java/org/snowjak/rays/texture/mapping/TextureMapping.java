package org.snowjak.rays.texture.mapping;

import org.snowjak.rays.geometry.Point2D;
import org.snowjak.rays.interact.DescribesSurface;
import org.snowjak.rays.interact.SurfaceDescriptor;

/**
 * A TextureMapping defines a transform from a {@link SurfaceDescriptor} to a
 * U/V texture-coordinate pair (where each coordinate is in {@code [0,1)}).
 * 
 * @author snowjak88
 *
 */
public interface TextureMapping {
	
	public <T extends DescribesSurface> Point2D transform(SurfaceDescriptor<T> surfaceDescriptor);
	
}
