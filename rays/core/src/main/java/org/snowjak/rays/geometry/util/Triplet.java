package org.snowjak.rays.geometry.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

/**
 * Represents a vector of 3 values.
 * 
 * @author snowjak88
 */
public class Triplet extends AbstractVector<Triplet> implements Serializable {

	private static final long serialVersionUID = 709936070558428071L;

	/**
	 * Create a new Triplet consisting of 3 values.
	 * 
	 * @param v1
	 * @param v2
	 * @param v3
	 */
	public Triplet(double v1, double v2, double v3) {
		super(v1, v2, v3);
	}

	/**
	 * Create a new Triplet consisting of the first 2 values from the given
	 * <code>double[]</code> array. If the given array contains fewer than 2
	 * values, the array is 0-padded to make up a length-2 array.
	 * 
	 * @param values
	 */
	public Triplet(double... values) {
		super(Arrays.copyOf(values, 3));
	}

	/**
	 * Initialize an empty Triplet with 3 0-values
	 */
	protected Triplet() {
		super(3);
	}

	@Override
	public Triplet apply(UnaryOperator<Double> operator) {

		return new Triplet(AbstractVector.apply(getAll(), operator));
	}

	@Override
	public Triplet apply(Triplet other, BinaryOperator<Double> operator) {

		return new Triplet(AbstractVector.apply(getAll(), other.getAll(), operator));
	}

}
