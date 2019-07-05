package org.snowjak.rays.spectrum.distribution;

import static org.apache.commons.math3.util.FastMath.ceil;
import static org.apache.commons.math3.util.FastMath.floor;
import static org.apache.commons.math3.util.FastMath.max;
import static org.apache.commons.math3.util.FastMath.min;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.apache.commons.math3.util.Pair;
import org.snowjak.rays.Settings;
import org.snowjak.rays.geometry.util.AbstractVector;

public abstract class TabulatedDistribution<D extends TabulatedDistribution<D, Y>, Y extends AbstractVector<Y>>
		implements BoundedDistribution<Y> {
	
	private final Pair<Double, Double> bounds;
	private final double indexStep;
	private final Y[] entries;
	
	private NavigableMap<Double, Y> __table = null;
	
	/**
	 * Load a TabulatedDistribution from a CSV-formatted {@link InputStream}.
	 * 
	 * @param csvStream
	 * @param constructor
	 * @param csvParser
	 * @param arrayAllocater
	 * @return
	 * @throws IOException
	 */
	public static <D extends TabulatedDistribution<D, Y>, Y extends AbstractVector<Y>> D
			
			loadFromCSV(InputStream csvStream, BiFunction<Pair<Double, Double>, Y[], D> constructor,
					Function<String, Pair<Double, Y>> csvParser, Function<Integer, Y[]> arrayAllocater)
					throws IOException {
		
		try (var reader = new BufferedReader(new InputStreamReader(csvStream))) {
			
			List<Pair<Double, Y>> pairs = new LinkedList<>();
			
			while (reader.ready()) {
				final var nextLine = reader.readLine();
				if (nextLine != null && !nextLine.isEmpty())
					pairs.add(csvParser.apply(nextLine));
			}
			
			final double minX = pairs.stream().mapToDouble(p -> p.getFirst()).min().orElse(0d);
			final double maxX = pairs.stream().mapToDouble(p -> p.getFirst()).max().orElse(0d);
			
			final Y[] values = pairs.stream().map(p -> p.getSecond()).toArray(len -> arrayAllocater.apply(len));
			
			return constructor.apply(new Pair<>(minX, maxX), values);
		}
	}
	
	/**
	 * Write a TabulatedDistribution out to a CSV-formatter {@link OutputStream}.
	 * 
	 * @param csvStream
	 * @throws IOException
	 */
	public void saveToCSV(OutputStream csvStream, BiFunction<Double, Y, String> csvWriter) throws IOException {
		
		try (var writer = new BufferedWriter(new OutputStreamWriter(csvStream))) {
			
			final var lineIterator = IntStream.range(0, entries.length).mapToDouble(i -> getPoint(i))
					.mapToObj(d -> csvWriter.apply(d, this.get(d))).iterator();
			
			while (lineIterator.hasNext()) {
				writer.write(lineIterator.next());
				writer.newLine();
			}
		}
	}
	
	/**
	 * Create a new {@link TabulatedDistribution} by sampling the given
	 * {@link BoundedDistribution} <code>sampleCount</code> times across its entire
	 * bound.
	 * 
	 * @param sample
	 * @param sampleCount
	 */
	public TabulatedDistribution(BoundedDistribution<Y> sample, int sampleCount) {
		
		this(sample, sample.getBounds().get(), sampleCount);
	}
	
	/**
	 * Create a new {@link TabulatedDistribution} by sampling the given
	 * {@link Distribution} <code>sampleCount</code> times across the given
	 * interval.
	 * 
	 * @param sample
	 * @param sampleCount
	 */
	public TabulatedDistribution(Distribution<Y> sample, Pair<Double, Double> interval, int sampleCount) {
		
		assert (sample != null);
		assert (interval != null);
		assert (interval.getFirst() <= interval.getSecond());
		assert (sampleCount > 1);
		
		this.bounds = interval;
		this.indexStep = (bounds.getSecond() - bounds.getFirst()) / ((double) sampleCount - 1d);
		this.entries = IntStream.range(0, sampleCount).mapToObj(i -> sample.get(this.getPoint(i)))
				.toArray(len -> this.getArray(len));
	}
	
	/**
	 * Create a new {@link TabulatedDistribution} with the given bounds, backed by a
	 * table containing the given <code>values</code>.
	 * 
	 * @param lowerBound
	 * @param upperBound
	 * @param entryCount
	 * @param initialValue
	 */
	public TabulatedDistribution(double lowerBound, double upperBound, Y[] values) {
		
		this(new Pair<>(lowerBound, upperBound), values);
	}
	
	/**
	 * Create a new {@link TabulatedDistribution} with the given bounds, backed by a
	 * table containing the given <code>values</code>.
	 * 
	 * @param lowerBound
	 * @param upperBound
	 * @param entryCount
	 * @param initialValue
	 */
	public TabulatedDistribution(Pair<Double, Double> bounds, Y[] values) {
		
		assert (bounds != null);
		assert (bounds.getFirst() <= bounds.getSecond());
		assert (values.length > 1);
		
		this.bounds = bounds;
		this.indexStep = (bounds.getSecond() - bounds.getFirst()) / ((double) values.length - 1d);
		this.entries = values;
	}
	
	/**
	 * Create a new {@link TabulatedDistribution} with the given bounds, backed by a
	 * table containing <code>entryCount</code> entries, all initialized to
	 * {@link #getZero()}.
	 * 
	 * @param lowerBound
	 * @param upperBound
	 * @param entryCount
	 * @param initialValue
	 */
	public TabulatedDistribution(double lowerBound, double upperBound, int entryCount) {
		
		this(new Pair<>(lowerBound, upperBound), entryCount);
	}
	
	/**
	 * Create a new {@link TabulatedDistribution} with the given bounds, backed by a
	 * table containing <code>entryCount</code> entries, all initialized to
	 * {@link #getZero()}.
	 * 
	 * @param lowerBound
	 * @param upperBound
	 * @param entryCount
	 * @param initialValue
	 */
	public TabulatedDistribution(Pair<Double, Double> bounds, int entryCount) {
		
		assert (bounds != null);
		assert (bounds.getFirst() <= bounds.getSecond());
		assert (entryCount > 1);
		
		this.bounds = bounds;
		this.indexStep = (bounds.getSecond() - bounds.getFirst()) / ((double) entryCount - 1d);
		this.entries = this.getArray(entryCount);
		Arrays.fill(entries, getZero());
	}
	
	@Override
	public Y get(double x) throws IndexOutOfBoundsException {
		
		if (!isInBounds(x))
			throw new IndexOutOfBoundsException();
		
		final double index = getIndex(x);
		final double fraction = index - floor(index);
		
		if (Settings.getInstance().nearlyEqual(fraction, 0d))
			return entries[(int) floor(index)];
		
		if (Settings.getInstance().nearlyEqual(fraction, 1d))
			return entries[(int) ceil(index)];
		
		return entries[(int) floor(index)].multiply(1d - fraction).add(entries[(int) ceil(index)].multiply(fraction));
	}
	
	@Override
	public boolean isDefinedAt(double x) {
		
		return isInBounds(x);
	}
	
	@Override
	public Y averageOver(double start, double end) {
		
		assert (start <= end);
		
		final double start_inside = max(bounds.getFirst(), start);
		final double end_inside = min(bounds.getSecond(), end);
		
		if (Settings.getInstance().nearlyEqual(start_inside, end_inside))
			return getZero();
		
		final double start_full = ceil((start_inside - bounds.getFirst()) / indexStep) + bounds.getFirst();
		final double end_full = floor((end_inside - bounds.getFirst()) / indexStep) + bounds.getFirst();
		
		Y totalArea = getZero();
		
		if (!Settings.getInstance().nearlyEqual(start_full, end_full))
			totalArea = totalArea.add(DoubleStream.iterate(start_full, d -> d < end_full, d -> d + indexStep)
					.mapToObj(x -> get(x).add(get(x + indexStep)).divide(2d).multiply(indexStep))
					.reduce(getZero(), (v1, v2) -> v1.add(v2)));
		
		if (!Settings.getInstance().nearlyEqual(start_inside, start_full))
			totalArea = totalArea
					.add(get(start_inside).add(get(start_full)).divide(2d).multiply(start_full - start_inside));
		
		if (!Settings.getInstance().nearlyEqual(end_inside, end_full))
			totalArea = totalArea.add(get(end_inside).add(get(end_full)).divide(2d).multiply(end_inside - end_full));
		
		return totalArea.divide(end_inside - start_inside);
	}
	
	@Override
	public Optional<Pair<Double, Double>> getBounds() {
		
		return Optional.of(bounds);
	}
	
	/**
	 * @return the number of entries in this tabulated distribution
	 */
	public int size() {
		
		return this.entries.length;
	}
	
	/**
	 * Return a new TabulatedDistribution produced by modifying this distribution's
	 * bounds. The resulting {@link TabulatedDistribution} will have the same number
	 * of table-entries as this distribution.
	 * 
	 * @param newBounds
	 * @return
	 */
	public D resize(Pair<Double, Double> newBounds) {
		
		return resize(newBounds, this.entries.length);
	}
	
	/**
	 * Return a new TabulatedDistribution produced by modifying this distribution's
	 * number of entries. The resulting {@link TabulatedDistribution} will have the
	 * same bounds as this distribution.
	 * 
	 * @param newEntryCount
	 * @return
	 */
	public D resize(int newEntryCount) {
		
		return resize(this.bounds, newEntryCount);
	}
	
	/**
	 * Return a new TabulatedDistribution produced by modifying this distribution's
	 * bounds. The resulting {@link TabulatedDistribution} will have the specified
	 * number of table-entries.
	 * 
	 * @param newBounds
	 * @param newEntryCount
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public D resize(Pair<Double, Double> newBounds, int newEntryCount) {
		
		assert (newBounds != null);
		assert (newBounds.getFirst() <= newBounds.getSecond());
		assert (newEntryCount > 1);
		
		if (Settings.getInstance().nearlyEqual(bounds.getFirst(), newBounds.getFirst())
				&& Settings.getInstance().nearlyEqual(bounds.getSecond(), newBounds.getSecond())
				&& entries.length == newEntryCount)
			return (D) this;
		
		final double newStepSize = (newBounds.getSecond() - newBounds.getFirst()) / ((double) newEntryCount - 1d);
		final Y[] newValues = this.getArray(newEntryCount);
		
		double currentX = newBounds.getFirst();
		for (int i = 0; i < newValues.length; i++) {
			newValues[i] = (this.isInBounds(currentX)) ? this.get(currentX) : this.getZero();
			currentX += newStepSize;
		}
		
		return this.getNewInstance(newBounds, newValues);
	}
	
	/**
	 * Get the entry-table-index corresponding to the given <code>x</code> point.
	 * <p>
	 * Note that this method performs no bounds-checking. As such, it's result may
	 * be outside the range <code>[0, table.length )</code>. You should treat any
	 * returned indices appropriately.
	 * </p>
	 * 
	 * @param x
	 * @return
	 */
	protected double getIndex(double x) {
		
		return ((x - bounds.getFirst()) / indexStep);
	}
	
	/**
	 * Get the point <code>x</code> associated with the given entry-table-index
	 * <code>index</code>.
	 * 
	 * @param index
	 * @return
	 */
	protected double getPoint(double index) {
		
		return (index * indexStep) + bounds.getFirst();
		
	}
	
	/**
	 * Get an instance of Y initialized to all zeros.
	 * 
	 * @return
	 */
	protected abstract Y getZero();
	
	/**
	 * Allocate a typed array of the specified length.
	 * 
	 * @param len
	 * @return
	 */
	protected abstract Y[] getArray(int len);
	
	/**
	 * Wraps an implementation constructor. Returns a new instance, given the
	 * specified bounds and entry-values.
	 * 
	 * @param bounds
	 * @param values
	 * @return
	 */
	protected abstract D getNewInstance(Pair<Double, Double> bounds, Y[] values);
	
	/**
	 * Compute the average of a number of values.
	 * 
	 * @param values
	 * @return
	 */
	protected Y average(Collection<Y> values) {
		
		assert (values != null);
		assert (values.size() > 0);
		
		return values.stream().reduce((v1, v2) -> v1.add(v2)).get().divide(values.size());
	}
	
	/**
	 * Return the array of entries backing this {@link TabulatedDistribution}. These
	 * entries are ordered by this distribution's bounds (see {@link #getBounds()}).
	 * Each index maps to a specific <code>x</code>, with each <code>x</code> being
	 * an equal distance ({@link #getIndexStep()}) apart.
	 * 
	 * @return
	 */
	protected Y[] getEntries() {
		
		return this.entries;
	}
	
	/**
	 * The indices to this distribution's table map onto a set of distinct points
	 * <code>x0, x1, x2, ...</code>, where each <code>x</code> is the same distance
	 * apart.
	 * 
	 * @return
	 */
	protected double getIndexStep() {
		
		return this.indexStep;
	}
	
	/**
	 * Return a {@link NavigableMap} which represents this distribution's
	 * table-entries.
	 * <p>
	 * In general, you should probably only use this method if you absolutely must.
	 * </p>
	 * 
	 * @return
	 */
	public NavigableMap<Double, Y> getTable() {
		
		if (__table == null)
			__table = new TreeMap<>(IntStream.range(0, entries.length).mapToDouble(i -> getPoint(i)).boxed()
					.collect(Collectors.toMap(d -> d, d -> this.get(d))));
		
		return __table;
	}
	
}
