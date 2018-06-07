package org.snowjak.rays.spectrum.distribution;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;

public class Polynomial1DDistribution implements Distribution<Double> {
	
	private List<Double> coefficients;
	
	/**
	 * Construct a new {@link Polynomial1DDistribution} with the given coefficients
	 * (ordered from 0-exponent onward).
	 * 
	 * @param coefficients
	 */
	public Polynomial1DDistribution(Double... coefficients) {
		
		this(Arrays.asList(coefficients));
	}
	
	/**
	 * Construct a new {@link Polynomial1DDistribution} with the given coefficients
	 * (ordered from 0-exponent onward).
	 * 
	 * @param coefficients
	 */
	public Polynomial1DDistribution(List<Double> coefficients) {
		
		this.coefficients = coefficients;
	}
	
	/**
	 * Evaluate this polynomial for the given parameter.
	 */
	@Override
	public Double get(Double key) {
		
		if (coefficients == null || coefficients.isEmpty())
			return 0d;
		
		//@formatter:off
		return IntStream.range(0, coefficients.size())
				.mapToObj(i -> new Pair<>(i, coefficients.get(i)))
				.map(p -> p.getValue() * FastMath.pow(key, p.getKey()))
				.reduce(0d, (d1, d2) -> d1 + d2);
		//@formatter:on
	}
	
	@Override
	public Double getLowKey() {
		
		return null;
	}
	
	@Override
	public Double getHighKey() {
		
		return null;
	}
	
}
