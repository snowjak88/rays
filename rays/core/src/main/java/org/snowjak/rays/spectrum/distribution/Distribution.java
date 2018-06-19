package org.snowjak.rays.spectrum.distribution;

import java.util.Map;
import java.util.Optional;

import org.apache.commons.math3.util.Pair;
import org.snowjak.rays.geometry.util.AbstractVector;

/**
 * A distribution is, basically, a mapping from one space onto another. It is
 * distinguished from a {@link Map} by being, in principle, a continuous mapping
 * (as opposed to a Map's discrete mapping).
 * 
 * @author snowjak88
 *
 */
public interface Distribution<Y extends AbstractVector<Y>> {
	
	/**
	 * For some point <code>x</code>, get the <code>y</code> which it maps on to.
	 * 
	 * @param x
	 * @return
	 * @throws IndexOutOfBoundsException
	 *             if the given point <code>x</code> is out of bounds
	 * @see #isInBounds(double)
	 */
	public Y get(double x) throws IndexOutOfBoundsException;
	
	/**
	 * Returns <code>true</code> if this distribution is defined at the given point
	 * <code>x</code>.
	 * 
	 * @param x
	 * @return
	 */
	public boolean isDefinedAt(double x);
	
	/**
	 * Compute the average value of this distribution across the given interval. If
	 * this distribution is bounded, the interval is trimmed to lie within its
	 * bounds (potentially to a 0-width interval).
	 * 
	 * @param start
	 * @param end
	 * @return
	 */
	public Y averageOver(double start, double end);
	
	/**
	 * @return <code>true</code> if this distribution has bounds outside of which it
	 *         cannot be evaluated
	 */
	public boolean isBounded();
	
	/**
	 * @return this distribution's lower and upper bounds, if they exist, or an
	 *         empty Optional otherwise
	 */
	public Optional<Pair<Double, Double>> getBounds();
	
	/**
	 * Returns <code>true</code> if the given point <code>x</code> is within this
	 * distribution's bounds (or if this distribution has no bounds at all).
	 * 
	 * @param x
	 * @return
	 */
	public default boolean isInBounds(double x) {
		
		if (!isBounded())
			return true;
		
		if (!getBounds().isPresent())
			return true;
		
		return (getBounds().get().getFirst() <= x) && (getBounds().get().getSecond() >= x);
	}
	
}
