package org.snowjak.rays.geometry.util;

import static org.apache.commons.math3.util.FastMath.max;
import static org.apache.commons.math3.util.FastMath.min;

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Represents a vector of <code>n</code> values.
 * 
 * @author snowjak88
 */
public abstract class NVector<T extends NVector<?>> implements Serializable {
	
	private static final long serialVersionUID = 2663668375502365368L;
	
	private final double[] values;
	
	public NVector(int n) {
		
		this.values = new double[n];
	}
	
	/**
	 * Create a new NVector with order <code>n</code> implicitly given by the length
	 * of the given array.
	 * <p>
	 * Please note that this constructor will <strong>not</strong> copy the given
	 * array of values before using it. This means that, if you pass in an array
	 * reference without first copying it manually, your immutable NVector could
	 * turn out to be very mutable indeed (when your code starts modifying the
	 * original array).
	 * </p>
	 * 
	 * @param values
	 */
	public NVector(double... values) {
		
		this.values = values;
	}
	
	/**
	 * Create a new NVector, taking the first <code>n</code> values from the given
	 * list of <code>values</code>. If <code>n > values.length</code>, 0-pad this
	 * NVector to make up the length.
	 * 
	 * @param n
	 * @param values
	 */
	public NVector(int n, double... values) {
		
		this.values = Arrays.copyOf(values, n);
	}
	
	/**
	 * @return the length of this NVector
	 */
	public int getN() {
		
		return this.values.length;
	}
	
	/**
	 * Return a copy of this NVector's values.
	 * 
	 * @return
	 */
	public double[] getAll() {
		
		return values;
	}
	
	/**
	 * Return the <code>i</code>th element of this NVector, where <code>i</code> is
	 * in [0, {@link #getN()}-1].
	 * 
	 * @param i
	 * @return
	 * @throws ArrayIndexOutOfBoundsException
	 *             if i is not within the bounds of this NVector
	 * @see #getN()
	 */
	public double get(int i) {
		
		return this.values[i];
	}
	
	/**
	 * Apply the given {@link UnaryOperator} to the given array, producing a second
	 * array as a result.
	 * 
	 * @param arraySupplier
	 * @param operand
	 * @param operator
	 * @return
	 */
	protected static double[] apply(double[] operand, UnaryOperator<Double> operator) {
		
		final double[] result = new double[operand.length];
		
		for (int i = 0; i < operand.length; i++)
			result[i] = operator.apply(operand[i]);
		
		return result;
	}
	
	/**
	 * Apply the given {@link BinaryOperator} to the given pair of arrays, producing
	 * a third array as a result.
	 * 
	 * @param arraySupplier
	 * @param operand
	 * @param operator
	 * @return
	 */
	protected static double[] apply(double[] operand1, double[] operand2, BinaryOperator<Double> operator) {
		
		final int longerLength = (operand1.length > operand2.length) ? operand1.length : operand2.length;
		
		final double[] op1 = (operand1.length == longerLength) ? operand1 : Arrays.copyOf(operand1, longerLength);
		final double[] op2 = (operand2.length == longerLength) ? operand2 : Arrays.copyOf(operand2, longerLength);
		final double[] result = new double[longerLength];
		
		for (int i = 0; i < longerLength; i++)
			result[i] = operator.apply(op1[i], op2[i]);
		
		return result;
	}
	
	/**
	 * Apply the given {@link UnaryOperator} to this NVector, producing another
	 * NVector as a result.
	 * 
	 * @param operator
	 * @return
	 */
	public abstract T apply(UnaryOperator<Double> operator);
	
	/**
	 * Apply the given {@link BinaryOperator} to this and another NVector, producing
	 * a third NVector as a result.
	 * <h1>Processing NVectors of different lengths</h1>
	 * <p>
	 * When applying an operation to two NVectors of differing lengths, the shorter
	 * of the two is padded with 0s to equal the length of the longer.
	 * </p>
	 * <p>
	 * Given two NVectors <code>v<sub>1</sub></code> and <code>v<sub>2</sub></code>,
	 * where
	 * 
	 * <pre>
	 * v<sub>1</sub> := { 1, 2 }
	 * v<sub>2</sub> := { 1, 2, 3 }
	 * operation := (d1, d2) -> d1 + d2
	 * </pre>
	 * 
	 * applying <code>operation</code> to <code>v<sub>1</sub></code> and
	 * <code>v<sub>2</sub></code> produces a third NVector
	 * <code>v<sub>3</sub> = { (1+1), (2+2), (3+0) } = { 2, 4, 3 }</code>
	 * </p>
	 * 
	 * @param other
	 * @param operator
	 * @return
	 */
	public abstract T apply(T other, BinaryOperator<Double> operator);
	
	/**
	 * Returns the negated form of this NVector.
	 * 
	 * <pre>
	 * v := { 1, 2, 3 }
	 * negate(v) := { -1, -2, -3 }
	 * </pre>
	 * 
	 * @return
	 */
	public T negate() {
		
		return this.apply((d) -> 0 - d);
	}
	
	/**
	 * Returns the reciprocal form of this NVector.
	 * 
	 * <pre>
	 * v := { 1, 2, 3 }
	 * reciprocal(v) := { 1/1, 1/2,  1/3 }
	 * </pre>
	 * 
	 * @return
	 */
	public T reciprocal() {
		
		return this.apply((d) -> 1d / d);
	}
	
	/**
	 * Normalize this NVector according to some magnitude-function (which evaluates
	 * the absolute-magnitude of this NVector).
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T normalize(Function<T, Double> magnitude) {
		
		return this.apply(d -> d / magnitude.apply((T) this));
	}
	
	/**
	 * Linearly interpolate from this NVector to another.
	 * 
	 * <pre>
	 * v := { 1, 2, 3 }
	 * u := { 2, 2, 2 }
	 * 
	 * linearlyInterpolate( v, u, 0.5 ) := { 1.5, 2, 2.5 }
	 * </pre>
	 * 
	 * @param other
	 * @param fraction
	 * @return
	 */
	public T linearInterpolateTo(T other, double fraction) {
		
		assert (this.getN() == other.getN());
		
		return this.apply(other, (v1, v2) -> (v2 - v1) * fraction + v1);
		
	}
	
	/**
	 * Returns the clamped form of this NVector.
	 * 
	 * <pre>
	 * v := { -1, 0, 1, 2, 3 }
	 * clamp(v, 0, 2) := { 0, 0, 1, 2, 2 }
	 * </pre>
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	public T clamp(double min, double max) {
		
		return this.apply((d) -> min(max(d, min), max));
	}
	
	/**
	 * Adds this NVector to another, producing a third NVector as a result.
	 * <p>
	 * See {@link #apply(NVector, BinaryOperator)} for information about behavior
	 * when adding NVectors of differing lengths.
	 * </p>
	 * 
	 * @param addend
	 * @return
	 */
	public T add(T addend) {
		
		return this.apply(addend, (d1, d2) -> d1 + d2);
	}
	
	/**
	 * Adds a constant value to every element in this NVector, producing another
	 * NVector as a result.
	 * 
	 * @param addend
	 * @return
	 */
	public T add(double addend) {
		
		return this.apply(d -> d + addend);
	}
	
	/**
	 * Subtract another NVector from this NVector, producing a third NVector as a
	 * result.
	 * <p>
	 * See {@link #apply(NVector, BinaryOperator)} for information about behavior
	 * when subtracting NVectors of differing lengths.
	 * </p>
	 * 
	 * @param subtrahend
	 * @return
	 */
	public T subtract(T subtrahend) {
		
		return this.apply(subtrahend, (d1, d2) -> d1 - d2);
	}
	
	/**
	 * Subtract a constant value from every element in this NVector, producing
	 * another NVector as a result.
	 * 
	 * @param subtrahend
	 * @return
	 */
	public T subtract(double subtrahend) {
		
		return this.apply(d -> d - subtrahend);
	}
	
	/**
	 * Multiply this NVector by another NVector, producing a third NVector as a
	 * result.
	 * <p>
	 * See {@link #apply(NVector, BinaryOperator)} for information about behavior
	 * when multiplying NVectors of differing lengths.
	 * </p>
	 * 
	 * @param multiplicand
	 * @return
	 */
	public T multiply(T multiplicand) {
		
		return this.apply(multiplicand, (d1, d2) -> d1 * d2);
	}
	
	/**
	 * Multiply every element in this NVector by a constant value, producing another
	 * NVector as a result.
	 * 
	 * @param multiplicand
	 * @return
	 */
	public T multiply(double multiplicand) {
		
		return this.apply(d -> d * multiplicand);
	}
	
	/**
	 * Divide this NVector by another NVector, producing a third NVector as a
	 * result.
	 * <p>
	 * See {@link #apply(NVector, BinaryOperator)} for information about behavior
	 * when dividing NVectors of differing lengths.
	 * </p>
	 * 
	 * @param divisor
	 * @return
	 */
	public T divide(T divisor) {
		
		return this.apply(divisor, (d1, d2) -> d1 / d2);
	}
	
	/**
	 * Divide every element in this NVector by a constant value, producing another
	 * NVector as a result.
	 * 
	 * @param divisor
	 * @return
	 */
	public T divide(double divisor) {
		
		return this.apply(d -> d / divisor);
	}
	
	@Override
	public String toString() {
		
		return getClass().getSimpleName() + ":" + Arrays.toString(values);
	}
	
	@Override
	public int hashCode() {
		
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(values);
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NVector<?> other = (NVector<?>) obj;
		if (!Arrays.equals(values, other.values))
			return false;
		return true;
	}
	
}
