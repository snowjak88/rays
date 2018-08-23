package org.snowjak.rays.renderer;

import org.snowjak.rays.Scene;
import org.snowjak.rays.sample.EstimatedSample;
import org.snowjak.rays.sample.TracedSample;

/**
 * A path-tracing renderer will recursively spawn rays at each
 * reflection/transmission event until a certain number of reflections ("depth")
 * has been reached, or no further interactions are found.
 * 
 * @author snowjak88
 *
 */
public class PathTracingRenderer extends Renderer {
	
	private int maxDepth;
	
	public PathTracingRenderer(int maxDepth) {
		
		this.maxDepth = maxDepth;
	}
	
	@Override
	public EstimatedSample estimate(TracedSample sample, Scene scene) {
		
		//
		// If we've exceeded our allowed depth, return.
		//
		if (sample.getRay().getDepth() >= maxDepth)
			return EstimatedSample.zero(sample);
			
		//
		// Attempt to find an Interaction between the given ray and the scene.
		//
		final var interaction = scene.getInteraction(sample.getRay());
		
		if (interaction == null)
			return EstimatedSample.zero(sample);
			
		//
		//
		//
		return EstimatedSample.zero(sample);
	}
	
}
