package org.snowjak.rays.geometry.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

/**
 * Represents a vector of 2 values.
 * 
 * @author snowjak88
 */
public class Pair extends AbstractVector<Pair> implements Serializable {
	
	private static final long serialVersionUID = 8826976880964290469L;
	
	public static final Pair ZERO = new Pair(0, 0);
	
	/**
	 * Create a new Pair consisting of 2 values.
	 * 
	 * @param v1
	 * @param v2
	 */
	public Pair(double v1, double v2) {
		
		super(v1, v2);
	}
	
	/**
	 * Create a new Pair consisting of the first 2 values from the given
	 * <code>double[]</code> array. If the given array contains fewer than 2 values,
	 * the array is 0-padded to make up a length-2 array.
	 * 
	 * @param values
	 */
	public Pair(double... values) {
		
		super(Arrays.copyOf(values, 2));
	}
	
	/**
	 * Create an empty Pair with 2 0-values
	 */
	protected Pair() {
		
		super(2);
	}
	
	@Override
	public Pair apply(UnaryOperator<Double> operator) {
		
		return new Pair(AbstractVector.apply(getAll(), operator));
	}
	
	@Override
	public Pair apply(Pair other, BinaryOperator<Double> operator) {
		
		return new Pair(AbstractVector.apply(getAll(), other.getAll(), operator));
	}
	
}
