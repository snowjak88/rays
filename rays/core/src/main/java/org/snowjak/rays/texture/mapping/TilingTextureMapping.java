package org.snowjak.rays.texture.mapping;

import static org.apache.commons.math3.util.FastMath.floor;

import org.snowjak.rays.geometry.Point2D;
import org.snowjak.rays.interact.DescribesSurface;
import org.snowjak.rays.interact.SurfaceDescriptor;

/**
 * TextureMapping which retains only the fractional portions of the U/V
 * coordinates -- effectively tiling the range {@code [0,1)} across the
 * available 2D space.
 * 
 * @author snowjak88
 *
 */
public class TilingTextureMapping implements TextureMapping {
	
	@Override
	public <T extends DescribesSurface> Point2D transform(SurfaceDescriptor<T> surfaceDescriptor) {
		
		final double x = surfaceDescriptor.getParam().getX(), y = surfaceDescriptor.getParam().getY();
		return new Point2D(x - floor(x), y - floor(y));
	}
	
}
