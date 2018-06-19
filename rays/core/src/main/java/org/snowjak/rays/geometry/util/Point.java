package org.snowjak.rays.geometry.util;

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

/**
 * Represents a vector of 1 value.
 * 
 * @author snowjak88
 *
 */
public class Point extends AbstractVector<Point> implements Serializable {
	
	private static final long serialVersionUID = -8630565775972347207L;
	
	public static final Point ZERO = new Point(0);
	
	/**
	 * Create a new Point composed of 1 value.
	 * 
	 * @param v
	 */
	public Point(double v) {
		
		super(v);
	}
	
	/**
	 * Create a new Point consisting of the first value from the given
	 * <code>double[]</code> array. If the array is empty, this Point's value is
	 * initialized to 0.
	 * 
	 * @param v
	 */
	public Point(double... v) {
		
		super(Arrays.copyOf(v, 1));
	}
	
	/**
	 * Create a new Point with a 0-value.
	 */
	public Point() {
		
		super(1);
	}
	
	@Override
	public Point apply(UnaryOperator<Double> operator) {
		
		return new Point(AbstractVector.apply(getAll(), operator));
	}
	
	@Override
	public Point apply(Point other, BinaryOperator<Double> operator) {
		
		return new Point(AbstractVector.apply(getAll(), other.getAll(), operator));
	}
	
}
