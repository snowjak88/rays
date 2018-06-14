package org.snowjak.rays;

import java.util.Arrays;
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.DoubleStream;

import org.snowjak.rays.geometry.util.Triplet;

/**
 * Various helper methods too trivial to merit their own container classes.
 * 
 * @author snowjak88
 *
 */
public class Util {
	
	/**
	 * Compute a definite integral -- or, rather, a Riemann sum -- over a specified
	 * interval. Uses the midpoint rule.
	 * 
	 * <pre>
	 *                 Sum( x in [intervalStart, intervalEnd] | f(x) )
	 * integrate(f) := -------------------------------------------------
	 *                                  intervalWidth
	 * 
	 * intervalWidth := ( intervalEnd - intervalStart ) / intervalCount
	 * </pre>
	 * 
	 * @param intervalStart
	 *            integration interval start point
	 * @param intervalEnd
	 *            integration interval end point
	 * @param intervalCount
	 *            number of slices the integration interval is divided into
	 * @param f
	 *            the function to integrate
	 * @return the definite integral of <code>f</code> across the specified interval
	 */
	public static double integrate(double intervalStart, double intervalEnd, int intervalCount, DoubleUnaryOperator f) {
		
		final double start = (intervalEnd > intervalStart) ? intervalStart : intervalEnd;
		final double end = (intervalEnd > intervalStart) ? intervalEnd : intervalStart;
		final double intervalStep = (end - start) / ((double) intervalCount);
		
		return DoubleStream.iterate(start + intervalStep / 2d, d -> d <= end - intervalStep / 2d, d -> d + intervalStep)
				.map(d -> f.applyAsDouble(d)).reduce(0d, (d1, d2) -> d1 + d2) * intervalStep;
	}
	
	/**
	 * Analagous to {@link #integrate(double, double, int, DoubleFunction)},
	 * allowing you to compute three integrals simultaneously (as components of a
	 * Triplet).
	 * 
	 * @param intervalStart
	 *            integration interval start point
	 * @param intervalEnd
	 *            integration interval end point
	 * @param intervalCount
	 *            number of slices the integration interval is divided into
	 * @param f
	 *            the function to integrate
	 * @return the definite integral of <code>f</code> across the specified interval
	 */
	public static Triplet integrateTriplet(double intervalStart, double intervalEnd, int intervalCount,
			DoubleFunction<Triplet> f) {
		
		final DoubleUnaryOperator xF = (x) -> f.apply(x).get(0), yF = (y) -> f.apply(y).get(1),
				zF = (z) -> f.apply(z).get(2);
		
		return new Triplet(integrate(intervalStart, intervalEnd, intervalCount, xF),
				integrate(intervalStart, intervalEnd, intervalCount, yF),
				integrate(intervalStart, intervalEnd, intervalCount, zF));
	}
	
	/**
	 * Compute the average of a collection of Double values.
	 * 
	 * @param doubles
	 * @return
	 */
	public static Double average(Double... doubles) {
		
		if (doubles == null || doubles.length < 1)
			return 0d;
		
		return Arrays.stream(doubles).reduce(0d, (d1, d2) -> d1 + d2) / doubles.length;
	}
	
	/**
	 * Compute the average of a collection of Triplets (by averaging each component
	 * separately).
	 * 
	 * @param doubles
	 * @return
	 */
	public static Triplet average(Triplet... triplets) {
		
		if (triplets == null || triplets.length < 1)
			return new Triplet(0, 0, 0);
		
		return Arrays.stream(triplets).reduce(new Triplet(), (t1, t2) -> t1.add(t2)).divide(triplets.length);
	}
	
	/**
	 * Shuffle an array in-place.
	 * 
	 * @param array
	 */
	public static void shuffle(Object[] array) {
		
		for (int i = 0; i < array.length - 1; i++) {
			
			int newIndex;
			do {
				newIndex = Settings.RND.nextInt(array.length - i) + i;
			} while (newIndex == i);
			
			final Object temp = array[i];
			array[i] = array[newIndex];
			array[newIndex] = temp;
		}
	}
}
