package org.snowjak.rays.spectrum.distribution;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;
import org.snowjak.rays.TriFunction;
import org.snowjak.rays.geometry.util.Triplet;

/**
 * Represents a {@link ColorMappingFunctionDistribution}, backed by a
 * {@link TabulatedDistribution}.
 * 
 * @author snowjak88
 *
 */
public class TabulatedColorMappingFunctionDistribution extends TabulatedDistribution<Triplet>
		implements ColorMappingFunctionDistribution {
	
	public TabulatedColorMappingFunctionDistribution() {
		
		super();
	}
	
	public TabulatedColorMappingFunctionDistribution(Map<Double, Triplet> table, BlendMethod<Triplet> blendMethod) {
		
		super(table, blendMethod);
	}
	
	public TabulatedColorMappingFunctionDistribution(Map<Double, Triplet> table) {
		
		super(table);
	}
	
	public <T extends TabulatedColorMappingFunctionDistribution> TabulatedColorMappingFunctionDistribution(T toCopy) {
		
		super(toCopy);
	}
	
	@Override
	public TriFunction<Triplet, Triplet, Double, Triplet> getLinearInterpolationFunction() {
		
		return (t1, t2, fraction) -> t1.linearInterpolateTo(t2, fraction);
	}
	
	@Override
	public Pair<Double, Triplet> parseEntry(String csvLine) {
		
		try {
			final List<Double> pieces = Arrays.stream(csvLine.split(",")).map(s -> Double.parseDouble(s))
					.collect(Collectors.toList());
			
			return new Pair<>(pieces.get(0), new Triplet(pieces.get(1), pieces.get(2), pieces.get(3)));
		} catch (Throwable t) {
			throw new RuntimeException("Could not parse CSV-line \"" + csvLine + "\"!", t);
		}
	}
	
	@Override
	public String writeEntry(Double key, Triplet entry) {
		
		return Arrays.asList(key, entry.get(0), entry.get(1), entry.get(2)).stream().map(d -> Double.toString(d))
				.collect(Collectors.joining(","));
	}
	
	@Override
	public Triplet averageOver(Double intervalStart, Double intervalEnd) {
		
		final double start, end;
		if (intervalStart > intervalEnd) {
			start = intervalEnd;
			end = intervalStart;
		} else {
			start = intervalStart;
			end = intervalEnd;
		}
		
		if ((end < getTable().firstKey()) || (start > getTable().lastKey()))
			return new Triplet(0, 0, 0);
		
		final var intervalStartingEntry = getTable().ceilingEntry(start);
		final var intervalEndingEntry = getTable().floorEntry(end);
		
		final var beforeIntervalEntry = getTable().lowerEntry(start);
		final var afterIntervalEntry = getTable().higherEntry(end);
		
		var currentKey = intervalStartingEntry.getKey();
		var nextKey = getTable().higherKey(currentKey);
		
		Triplet totalArea = new Triplet(0, 0, 0);
		
		if (beforeIntervalEntry != null && intervalStartingEntry.getKey() > intervalStart)
			totalArea = totalArea.add(get(intervalStart).add(intervalStartingEntry.getValue()).multiply(0.5)
					.multiply(intervalStartingEntry.getKey() - intervalStart));
		
		while (nextKey != null && nextKey <= intervalEndingEntry.getKey()) {
			
			totalArea = totalArea.add(get(currentKey).add(get(nextKey)).multiply(0.5).multiply(nextKey - currentKey));
			
			currentKey = nextKey;
			nextKey = getTable().higherKey(currentKey);
		}
		
		if (afterIntervalEntry != null && intervalEndingEntry.getKey() < intervalEnd)
			totalArea = totalArea.add(intervalEndingEntry.getValue().add(get(intervalEnd)).multiply(0.5)
					.multiply(intervalEnd - intervalEndingEntry.getKey()));
		
		return totalArea.divide(intervalEnd - intervalStart);
	}
	
}
