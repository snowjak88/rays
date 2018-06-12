package org.snowjak.rays.spectrum.distribution;

import java.util.function.Supplier;
import java.util.stream.DoubleStream;

import org.apache.commons.math3.util.Pair;

/**
 * Represents a distribution of some quantity.
 * 
 * @author snowjak88
 *
 */
public interface Distribution<V> {
	
	/**
	 * Get this distribution's value for some key, or <code>null</code> if that
	 * value does not exist.
	 * 
	 * @param k
	 * @return
	 */
	public V get(Double key);
	
	/**
	 * Get the key corresponding to this distribution's low-end (or
	 * <code>null</code> if not applicable).
	 * 
	 * @return
	 */
	public Double getLowKey();
	
	/**
	 * Get the key corresponding to this distribution's high-end (or
	 * <code>null</code> if not applicable).
	 * 
	 * @return
	 */
	public Double getHighKey();
	
	/**
	 * Compute an average value for this distribution from
	 * <code>intervalStart</code> to <code>intervalEnd</code> (inclusive), or
	 * <code>null</code> if not applicable.
	 * 
	 * @param intervalStart
	 * @param intervalEnd
	 * @return
	 */
	public V averageOver(Double intervalStart, Double intervalEnd);
	
	/**
	 * Convert this distribution to a tabulated form (with <code>sampleCount</code>
	 * table entries from <code>lowKey</code> to <code>highKey</code>, inclusive).
	 * If this distribution is already in a tabulated form, resample it.
	 * 
	 * @param supplier
	 * @param lowKey
	 * @param highKey
	 * @param sampleCount
	 */
	public default <R extends TabulatedDistribution<V>> R toTabulatedForm(Supplier<R> supplier, Double lowKey,
			Double highKey, int sampleCount) {
		
		if (sampleCount <= 0)
			return supplier.get();
		
		if (sampleCount == 1) {
			final var table = supplier.get();
			table.addEntry((this.getLowKey() + this.getHighKey()) / 2d, this.averageOver(getLowKey(), getHighKey()));
			return table;
		}
		
		final var stepSize = (highKey - lowKey) / ((double) sampleCount - 1d);
		final var table = supplier.get();
		DoubleStream.iterate(lowKey, d -> d < highKey, d -> d + stepSize)
				.mapToObj(d -> new Pair<>(d + stepSize / 2d, this.averageOver(d, d + stepSize)))
				.forEach(p -> table.addEntry(p.getKey(), p.getValue()));
		return table;
	}
	
}
