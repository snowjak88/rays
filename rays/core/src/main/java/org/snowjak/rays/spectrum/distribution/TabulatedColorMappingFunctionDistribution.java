package org.snowjak.rays.spectrum.distribution;

import java.util.Arrays;
import java.util.List;
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
	
}
