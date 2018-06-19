package org.snowjak.rays.spectrum.distribution;

import org.snowjak.rays.geometry.util.AbstractVector;

/**
 * Represents a distribution that is bounded within a definite range.
 * 
 * @author snowjak88
 *
 * @param <Y>
 */
public interface BoundedDistribution<Y extends AbstractVector<Y>> extends Distribution<Y> {
	
	@Override
	default boolean isBounded() {
		
		return true;
	}
	
}
