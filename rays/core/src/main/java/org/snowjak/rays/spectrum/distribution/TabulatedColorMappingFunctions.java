package org.snowjak.rays.spectrum.distribution;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

import org.apache.commons.math3.util.Pair;
import org.snowjak.rays.geometry.util.Triplet;
import org.snowjak.rays.spectrum.ColorMappingFunctions;

public class TabulatedColorMappingFunctions extends TabulatedDistribution<TabulatedColorMappingFunctions, Triplet>
		implements ColorMappingFunctions {
	
	private static final Pattern __surrounding_doublequotes_pattern = Pattern.compile("\"(.*)\"");
	
	/**
	 * Load a TabulatedColorMappingFunctions from a CSV-formatted
	 * {@link InputStream}.
	 * 
	 * @param csvStream
	 * @return
	 * @throws IOException
	 */
	public static TabulatedColorMappingFunctions loadFromCSV(InputStream csvStream) throws IOException {
		
		return TabulatedDistribution.loadFromCSV(csvStream,
				(bounds, values) -> new TabulatedColorMappingFunctions(bounds, values),
				line -> TabulatedColorMappingFunctions.parseCSVLine(line), len -> new Triplet[len]);
	}
	
	public TabulatedColorMappingFunctions(BoundedDistribution<Triplet> sample, int sampleCount) {
		
		super(sample, sampleCount);
	}
	
	public TabulatedColorMappingFunctions(Distribution<Triplet> sample, Pair<Double, Double> interval,
			int sampleCount) {
		
		super(sample, interval, sampleCount);
	}
	
	public TabulatedColorMappingFunctions(double lowerBound, double upperBound, int entryCount) {
		
		super(lowerBound, upperBound, entryCount);
	}
	
	public TabulatedColorMappingFunctions(double lowerBound, double upperBound, Triplet[] values) {
		
		super(lowerBound, upperBound, values);
	}
	
	public TabulatedColorMappingFunctions(Pair<Double, Double> bounds, int entryCount) {
		
		super(bounds, entryCount);
	}
	
	public TabulatedColorMappingFunctions(Pair<Double, Double> bounds, Triplet[] values) {
		
		super(bounds, values);
	}
	
	@Override
	protected Triplet getZero() {
		
		return new Triplet();
	}
	
	@Override
	protected Triplet[] getArray(int len) {
		
		return new Triplet[len];
	}
	
	@Override
	protected TabulatedColorMappingFunctions getNewInstance(Pair<Double, Double> bounds, Triplet[] values) {
		
		return new TabulatedColorMappingFunctions(bounds, values);
	}
	
	protected static Pair<Double, Triplet> parseCSVLine(String line) throws NumberFormatException {
		
		final var parts = line.split(",");
		
		for (int i = 0; i < parts.length; i++) {
			final var m = __surrounding_doublequotes_pattern.matcher(parts[i]);
			if (m.matches())
				parts[i] = m.group(1);
		}
		
		return new Pair<>(Double.parseDouble(parts[0]),
				new Triplet(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3])));
	}
	
	protected static String buildCSVLine(double point, Triplet value) {
		
		return Double.toString(point) + "," + Double.toString(value.get(0)) + "," + Double.toString(value.get(1)) + ","
				+ Double.toString(value.get(2));
	}
	
}
