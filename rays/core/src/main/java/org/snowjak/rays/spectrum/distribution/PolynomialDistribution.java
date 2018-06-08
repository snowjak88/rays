package org.snowjak.rays.spectrum.distribution;

import static org.apache.commons.math3.util.FastMath.max;
import static org.apache.commons.math3.util.FastMath.min;
import static org.apache.commons.math3.util.FastMath.signum;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;

/**
 * Represents a {@link Distribution} backed by a polynomial.
 * <p>
 * E.g., if you construct a PolynomialDistribution with the coefficients
 * 
 * <pre>
 * { 1, 2, 3, 4 }
 * </pre>
 * 
 * the implicit polynomial is initialized as
 * 
 * <pre>
 * p(x) := 1 + 2x + 3x^2 + 4x^3
 * </pre>
 * </p>
 * 
 * @author snowjak88
 *
 */
public class PolynomialDistribution implements Distribution<Double> {
	
	private List<Double> coefficients;
	
	/**
	 * Construct a new {@link PolynomialDistribution} with the given coefficients
	 * (ordered from 0-exponent onward).
	 * 
	 * @param coefficients
	 */
	public PolynomialDistribution(double... coefficients) {
		
		this(Arrays.stream(coefficients).boxed().collect(Collectors.toList()));
	}
	
	/**
	 * Construct a new {@link PolynomialDistribution} with the given coefficients
	 * (ordered from 0-exponent onward).
	 * 
	 * @param coefficients
	 */
	public PolynomialDistribution(Double... coefficients) {
		
		this(Arrays.asList(coefficients));
	}
	
	/**
	 * Construct a new {@link PolynomialDistribution} with the given coefficients
	 * (ordered from 0-exponent onward).
	 * 
	 * @param coefficients
	 */
	public PolynomialDistribution(List<Double> coefficients) {
		
		this.coefficients = coefficients;
	}
	
	/**
	 * Compute a tabulated form of this PolynomialDistribution.
	 * 
	 * @param rangeStart
	 *            the first key to include
	 * @param rangeEnd
	 *            the last key to include
	 * @param interval
	 *            the interval between keys
	 * @return
	 */
	public TabulatedDistribution<Double> toTable(double rangeStart, double rangeEnd, double interval) {
		
		final double start = min(rangeStart, rangeEnd);
		final double end = max(rangeStart, rangeEnd);
		final double interv = signum(rangeEnd - rangeStart) * interval;
		
		//@formatter:off
		return new TabulatedSpectralPowerDistribution(
				DoubleStream.iterate(start, (d) -> d <= end, (d) -> d += interv)
					.mapToObj(k -> new Pair<Double, Double>(k, this.get(k)))
					.collect(Collectors.toMap(Pair::getKey, Pair::getValue)));
		//@formatter:on
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
