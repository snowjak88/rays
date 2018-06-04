package org.snowjak.rays.geometry;

import java.io.Serializable;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.snowjak.rays.geometry.util.Triplet;

/**
 * A Normal3D is distinct from a {@link Vector3D} in that a Normal3D does not
 * inherently represent magnitude, only direction.
 * 
 * @author snowjak88
 */
public class Normal3D extends Triplet implements Serializable {
	
	private static final long serialVersionUID = -8278436657012042876L;
	
	public static Normal3D from(Triplet t) {
		
		if (Normal3D.class.isAssignableFrom(t.getClass()))
			return (Normal3D) t;
		
		return new Normal3D(t.get(0), t.get(1), t.get(2));
	}
	
	public Normal3D(double x, double y, double z) {
		
		super(x, y, z);
	}
	
	/**
	 * Create a new Normal3D whose coordinates consist of the first 3 values from
	 * the given <code>double[]</code> array. If the given array contains fewer than
	 * 3 values, the array is 0-padded to make up a length-3 array.
	 * 
	 * @param values
	 */
	public Normal3D(double... coordinates) {
		
		super(coordinates);
	}
	
	/**
	 * Initialize an empty Normal3D.
	 */
	protected Normal3D() {
		
		super();
	}
	
	public double getX() {
		
		return get(0);
	}
	
	protected void setX(double x) {
		
		getAll()[0] = x;
	}
	
	public double getY() {
		
		return get(1);
	}
	
	protected void setY(double y) {
		
		getAll()[1] = y;
	}
	
	public double getZ() {
		
		return get(2);
	}
	
	protected void setZ(double z) {
		
		getAll()[2] = z;
	}
	
	@Override
	public Normal3D negate() {
		
		return Normal3D.from(super.negate());
	}
	
	@Override
	public Normal3D reciprocal() {
		
		return Normal3D.from(super.reciprocal());
	}
	
	@Override
	public Normal3D add(Triplet addend) {
		
		return Normal3D.from(super.add(addend));
	}
	
	@Override
	public Normal3D add(double addend) {
		
		return Normal3D.from(super.add(addend));
	}
	
	@Override
	public Normal3D subtract(Triplet subtrahend) {
		
		return Normal3D.from(super.subtract(subtrahend));
	}
	
	@Override
	public Normal3D subtract(double subtrahend) {
		
		return Normal3D.from(super.subtract(subtrahend));
	}
	
	@Override
	public Normal3D multiply(Triplet multiplicand) {
		
		return Normal3D.from(super.multiply(multiplicand));
	}
	
	@Override
	public Normal3D multiply(double multiplicand) {
		
		return Normal3D.from(super.multiply(multiplicand));
	}
	
	@Override
	public Normal3D divide(Triplet divisor) {
		
		return Normal3D.from(super.divide(divisor));
	}
	
	@Override
	public Normal3D divide(double divisor) {
		
		return Normal3D.from(super.divide(divisor));
	}
	
}
