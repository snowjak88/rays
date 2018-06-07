package org.snowjak.rays.spectrum.distribution;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;
import org.snowjak.rays.TriFunction;

/**
 * Represents a {@link Distribution} backed by a table of measurements.
 * 
 * @author snowjak88
 *
 * @param <K>
 * @param <V>
 */
public abstract class TabulatedDistribution<V> implements Distribution<V> {
	
	private NavigableMap<Double, V> table;
	private BlendMethod<V> blendMethod;
	
	/**
	 * Load a {@link TabulatedDistribution} from an {@link InputStream} (expected to
	 * feed CSV-formatted data).
	 * 
	 * @param supplier
	 * @param csv
	 * @return
	 * @throws IOException
	 */
	public static <R extends TabulatedDistribution<V>, V> R loadFromCSV(Supplier<R> supplier, InputStream csv)
			throws IOException {
		
		final var distribution = supplier.get();
		
		try (var reader = new BufferedReader(new InputStreamReader(csv))) {
			while (reader.ready()) {
				final var entry = distribution.parseEntry(reader.readLine());
				distribution.addEntry(entry.getKey(), entry.getValue());
			}
		}
		
		return distribution;
	}
	
	/**
	 * Write this {@link TabulatedDistribution}, formatted as a CSV file, to the
	 * given {@link OutputStream}.
	 * 
	 * @param distribution
	 * @param csv
	 * @throws IOException
	 */
	public void saveToCSV(OutputStream csv) throws IOException {
		
		try (var writer = new BufferedWriter(new OutputStreamWriter(csv))) {
			
			boolean isFirstLineDone = false;
			for (String line : this.getAll().stream().map(p -> this.writeEntry(p.getKey(), p.getValue()))
					.collect(Collectors.toList())) {
				if (isFirstLineDone)
					writer.newLine();
				
				writer.write(line);
				
				isFirstLineDone = true;
			}
		}
		
	}
	
	/**
	 * Construct a new (empty) {@link TabulatedDistribution}, using the default
	 * linear-interpolation {@link LinearBlendMethod}.
	 */
	public TabulatedDistribution() {
		
		this(Collections.emptyMap());
	}
	
	/**
	 * Construct a new {@link TabulatedDistribution}, using the default
	 * linear-interpolation {@link LinearBlendMethod}.
	 * 
	 * @param table
	 */
	public TabulatedDistribution(Map<Double, V> table) {
		
		this(table, null);
		this.setBlendMethod(new LinearBlendMethod<V>(getLinearInterpolationFunction()));
	}
	
	/**
	 * Construct a new {@link TabulatedDistribution}, using the given
	 * {@link BlendMethod}.
	 * 
	 * @param table
	 * @param blendMethod
	 */
	public TabulatedDistribution(Map<Double, V> table, BlendMethod<V> blendMethod) {
		
		this.table = new TreeMap<>(table);
		this.blendMethod = blendMethod;
	}
	
	@Override
	public V get(Double k) {
		
		return this.blendMethod.get(this, k);
	}
	
	/**
	 * @return all entries stored in this {@link TabulatedDistribution}
	 */
	public Collection<Pair<Double, V>> getAll() {
		
		return this.table.entrySet().stream().map(e -> new Pair<>(e.getKey(), e.getValue()))
				.collect(Collectors.toList());
	}
	
	@Override
	public Double getLowKey() {
		
		return table.firstKey();
	}
	
	@Override
	public Double getHighKey() {
		
		return table.lastKey();
	}
	
	/**
	 * Add an entry to this {@link TabulatedDistribution}.
	 * 
	 * @param key
	 * @param value
	 */
	public void addEntry(Double key, V value) {
		
		this.table.put(key, value);
	}
	
	/**
	 * @return the {@link BlendMethod} currently used by this
	 *         {@link TabulatedDistribution}
	 */
	public BlendMethod<V> getBlendMethod() {
		
		return blendMethod;
	}
	
	/**
	 * Set the {@link BlendMethod} to be used by this {@link TabulatedDistribution}
	 * 
	 * @param blendMethod
	 */
	public void setBlendMethod(BlendMethod<V> blendMethod) {
		
		this.blendMethod = blendMethod;
	}
	
	/**
	 * Defines the linear-interpolation {@link TriFunction} to use, should the user
	 * desire to use a {@link LinearBlendMethod}.
	 */
	public abstract TriFunction<V, V, Double, V> getLinearInterpolationFunction();
	
	/**
	 * Given a single line of CSV data, parse a single distribution-entry.
	 * 
	 * @param csvLine
	 * @return
	 */
	public abstract Pair<Double, V> parseEntry(String csvLine);
	
	/**
	 * Given a single key/value pair from this distribution, format it into a line
	 * of CSV data.
	 * 
	 * @param key
	 * @param entry
	 * @return
	 */
	public abstract String writeEntry(Double key, V entry);
	
	/**
	 * Defines the method whereby we derive intermediate values (between stored
	 * measurements) for a {@link TabulatedDistribution}.
	 * 
	 * @author snowjak88
	 *
	 * @param <K>
	 * @param <V>
	 */
	public interface BlendMethod<V> {
		
		/**
		 * Get the blended value from the given {@link TabulatedDistribution}, or
		 * <code>null</code> if no value is available.
		 * 
		 * @param table
		 * @param key
		 * @return
		 */
		public V get(TabulatedDistribution<V> table, Double key);
	}
	
	public static class NearestBlendMethod<V> implements BlendMethod<V> {
		
		@Override
		public V get(TabulatedDistribution<V> table, Double key) {
			
			if (table.table == null || table.table.isEmpty())
				return null;
			
			final var lower = table.table.floorEntry(key);
			final var upper = table.table.ceilingEntry(key);
			
			if (lower == null && upper == null)
				return null;
			
			if (upper == null)
				return lower.getValue();
			if (lower == null)
				return upper.getValue();
			
			final double lowerDistance = key - lower.getKey();
			final double upperDistance = upper.getKey() - key;
			return (lowerDistance <= upperDistance) ? lower.getValue() : upper.getValue();
		}
		
	}
	
	public static class LinearBlendMethod<V> implements BlendMethod<V> {
		
		private TriFunction<V, V, Double, V> interpolation;
		
		/**
		 * Construct a new {@link LinearBlendMethod}. You must provide an implementation
		 * of linear-interpolation for your chosen value-type.
		 * 
		 * @param interpolation
		 */
		public LinearBlendMethod(TriFunction<V, V, Double, V> interpolation) {
			
			this.interpolation = interpolation;
		}
		
		@Override
		public V get(TabulatedDistribution<V> table, Double key) {
			
			if (table.table == null || table.table.isEmpty())
				return null;
			
			final var lower = table.table.floorEntry(key);
			final var upper = table.table.ceilingEntry(key);
			
			if (lower == null && upper == null)
				return null;
			
			if (upper == null)
				return lower.getValue();
			if (lower == null)
				return upper.getValue();
			
			if (lower.getKey().equals(upper.getKey()))
				return lower.getValue();
			
			final double fraction = (key - lower.getKey()) / (upper.getKey() - lower.getKey());
			return interpolation.apply(lower.getValue(), upper.getValue(), fraction);
		}
		
	}
	
}
