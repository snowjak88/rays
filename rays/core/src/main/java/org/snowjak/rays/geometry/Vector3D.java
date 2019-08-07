package org.snowjak.rays.geometry;

import static org.apache.commons.math3.util.FastMath.acos;
import static org.apache.commons.math3.util.FastMath.atan2;
import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.pow;
import static org.apache.commons.math3.util.FastMath.sin;
import static org.apache.commons.math3.util.FastMath.sqrt;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Random;

import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.annotations.UIType;
import org.snowjak.rays.geometry.util.Triplet;
import org.snowjak.rays.serialization.IsLoadable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

/**
 * Represents a vector in 3-space -- direction plus magnitude.
 * 
 * <h3>JSON</h3>
 * <p>
 * A Vector3D may be serialized to JSON in the following format.
 * </p>
 * 
 * <pre>
 * ...
 * {
 *     "x": <em>x</em>,
 *     "y": <em>y</em>,
 *     "z": <em>z</em>
 * }
 * ...
 * </pre>
 * 
 * @author snowjak88
 */
@UIType(fields = { @UIField(name = "x", type = Double.class, defaultValue = "0"),
		@UIField(name = "y", type = Double.class, defaultValue = "0"),
		@UIField(name = "z", type = Double.class, defaultValue = "0") })
public class Vector3D extends Triplet implements Serializable {
	
	private static final long serialVersionUID = -997240497037355891L;
	
	/**
	 * Represents the zero-vector {@code [0, 0, 0]}
	 */
	public static final Vector3D ZERO = new Vector3D(0, 0, 0);
	/**
	 * Represents the basis vector {@code [1, 0, 0]}
	 */
	public static final Vector3D I = new Vector3D(1, 0, 0);
	/**
	 * Represents the basis vector {@code [0, 1, 0]}
	 */
	public static final Vector3D J = new Vector3D(0, 1, 0);
	/**
	 * Represents the basis vector {@code [0, 0, 1]}
	 */
	public static final Vector3D K = new Vector3D(0, 0, 1);
	
	private transient double magnitude = -1d, magnitudeSq = -1d;
	
	/**
	 * Convert the given {@link Triplet} into a Vector3D.
	 * 
	 * @param t
	 * @return
	 */
	public static Vector3D from(Triplet t) {
		
		if (Vector3D.class.isAssignableFrom(t.getClass()))
			return (Vector3D) t;
		
		return new Vector3D(t.get(0), t.get(1), t.get(2));
	}
	
	/**
	 * Construct a Vector3D in Cartesian coordinates from the given polar
	 * coordinates. The given Triplet's components are assumed to be of the form
	 * <code>[ r, theta, phi ]</code>, where:
	 * <ul>
	 * <li><strong>r</strong>: radius</li>
	 * <li><strong>theta</strong>: elevation</li>
	 * <li><strong>phi</strong>: azimuth</li>
	 * </ul>
	 * 
	 * @param t
	 * @return
	 */
	public static Vector3D fromPolar(Triplet t) {
		
		return fromPolar(t.get(0), t.get(1), t.get(2));
	}
	
	/**
	 * Construct a Vector3D in Cartesian coordinates from the given polar
	 * coordinates.
	 * 
	 * @param r
	 * @param theta
	 * @param phi
	 * @return
	 */
	public static Vector3D fromPolar(double r, double theta, double phi) {
		
		return new Vector3D(r * sin(theta) * cos(phi), r * sin(theta) * sin(phi), r * cos(theta));
	}
	
	/**
	 * Create a Vector3D pointing from one Point3D to another. This Vector3D is
	 * <strong>not</strong> normalized.
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public static Vector3D from(Point3D from, Point3D to) {
		
		return new Vector3D(to.get(0) - from.get(0), to.get(1) - from.get(1), to.get(2) - from.get(2));
	}
	
	public Vector3D(double x, double y, double z) {
		
		super(x, y, z);
	}
	
	/**
	 * Create a new Vector3D3D whose coordinates consist of the first 3 values from
	 * the given <code>double[]</code> array. If the given array contains fewer than
	 * 3 values, the array is 0-padded to make up a length-3 array.
	 * 
	 * @param values
	 */
	public Vector3D(double... coordinates) {
		
		super(coordinates);
	}
	
	/**
	 * Private constructor for Vector3D, for when we already know magnitude and
	 * magnitude-squared.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param magnitude
	 * @param magnitudeSq
	 */
	private Vector3D(double x, double y, double z, double magnitude, double magnitudeSq) {
		
		super(x, y, z);
		this.magnitude = magnitude;
		this.magnitudeSq = magnitudeSq;
	}
	
	/**
	 * Create an empty Vector3D equivalent to {@link Vector3D#ZERO}
	 */
	protected Vector3D() {
		
		super(3);
	}
	
	/**
	 * @return the normalized form of this Vector3D
	 */
	public Vector3D normalize() {
		
		if (getMagnitude() == 0d)
			return this;
		return new Vector3D(getX() / getMagnitude(), getY() / getMagnitude(), getZ() / getMagnitude(), 1.0, 1.0);
	}
	
	/**
	 * Compute the dot-product of this and another Vector3D.
	 * 
	 * @param other
	 * @return
	 */
	public double dotProduct(Vector3D other) {
		
		return this.getX() * other.getX() + this.getY() * other.getY() + this.getZ() * other.getZ();
	}
	
	/**
	 * Compute the dot-product of this Vector3D and a Normal3D. Both this and the
	 * Normal3D are normalized prior to computing the dot-product.
	 * 
	 * @param normal
	 * @return
	 */
	public double dotProduct(Normal3D normal) {
		
		return this.normalize().dotProduct(Vector3D.from(normal).normalize());
	}
	
	/**
	 * Compute the (left-handed) cross-product of this and another Vector3D.
	 * 
	 * @param other
	 * @return
	 */
	public Vector3D crossProduct(Vector3D other) {
		
		return new Vector3D(this.getY() * other.getZ() - this.getZ() * other.getY(),
				this.getZ() * other.getX() - this.getX() * other.getZ(),
				this.getX() * other.getY() - this.getY() * other.getX());
	}
	
	/**
	 * Create one possible orthogonal Vector to this Vector. The new Vector is
	 * normalized after creation.
	 */
	public Vector3D orthogonal() {
		
		final Random rnd = new Random(System.currentTimeMillis());
		final double newX, newY, newZ;
		
		if (this.getZ() == 0d) {
			
			if (this.getX() == 0d) {
				
				newX = rnd.nextGaussian();
				newZ = rnd.nextGaussian();
				newY = (-this.getX() * newX - this.getZ() * newZ) / this.getY();
				
			} else {
				
				newY = rnd.nextGaussian();
				newZ = rnd.nextGaussian();
				newX = (-this.getY() * newY - this.getZ() * newZ) / this.getX();
			}
			
		} else {
			
			newX = rnd.nextGaussian();
			newY = rnd.nextGaussian();
			newZ = (-this.getX() * newX - this.getY() * newY) / this.getZ();
		}
		
		return new Vector3D(newX, newY, newZ).normalize();
	}
	
	/**
	 * Convert this Vector3D from Cartesian to polar coordinates (where the returned
	 * Triplet's components are of the form <code>[ r, theta, phi ]</code>, where:
	 * <ul>
	 * <li><strong>r</strong>: radius</li>
	 * <li><strong>theta</strong>: elevation</li>
	 * <li><strong>phi</strong>: azimuth</li>
	 * </ul>
	 * 
	 * @return
	 */
	public Triplet toPolar() {
		
		return new Triplet(getMagnitude(), atan2(getY(), getX()), acos(getZ() / getMagnitude()));
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
	
	public double getMagnitude() {
		
		if (magnitude < 0d)
			magnitude = sqrt(getMagnitudeSq());
		return magnitude;
	}
	
	protected void setMagnitude(double magnitude) {
		
		this.magnitude = magnitude;
	}
	
	public double getMagnitudeSq() {
		
		if (magnitudeSq < 0d)
			magnitudeSq = pow(getX(), 2) + pow(getY(), 2) + pow(getZ(), 2);
		return magnitudeSq;
	}
	
	protected void setMagnitudeSq(double magnitudeSq) {
		
		this.magnitudeSq = magnitudeSq;
	}
	
	@Override
	public Vector3D negate() {
		
		return new Vector3D(-getX(), -getY(), -getZ(), getMagnitude(), getMagnitudeSq());
	}
	
	@Override
	public Vector3D reciprocal() {
		
		return Vector3D.from(super.reciprocal());
	}
	
	@Override
	public Vector3D add(Triplet addend) {
		
		return Vector3D.from(super.add(addend));
	}
	
	@Override
	public Vector3D add(double addend) {
		
		return Vector3D.from(super.add(addend));
	}
	
	@Override
	public Vector3D subtract(Triplet subtrahend) {
		
		return Vector3D.from(super.subtract(subtrahend));
	}
	
	@Override
	public Vector3D subtract(double subtrahend) {
		
		return Vector3D.from(super.subtract(subtrahend));
	}
	
	@Override
	public Vector3D multiply(Triplet multiplicand) {
		
		return Vector3D.from(super.multiply(multiplicand));
	}
	
	@Override
	public Vector3D multiply(double multiplicand) {
		
		return Vector3D.from(super.multiply(multiplicand));
	}
	
	@Override
	public Vector3D divide(Triplet divisor) {
		
		return Vector3D.from(super.divide(divisor));
	}
	
	@Override
	public Vector3D divide(double divisor) {
		
		return Vector3D.from(super.divide(divisor));
	}
	
	public static class Loader implements IsLoadable<Vector3D> {
		
		@Override
		public Vector3D deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			
			if (!json.isJsonObject())
				throw new JsonParseException("Cannot deserialize Vector3D from JSON -- expecting a JSON object!");
			
			final var obj = json.getAsJsonObject();
			
			final var x = obj.get("x").getAsDouble();
			final var y = obj.get("y").getAsDouble();
			final var z = obj.get("z").getAsDouble();
			
			return new Vector3D(x, y, z);
		}
		
		@Override
		public JsonElement serialize(Vector3D src, Type typeOfSrc, JsonSerializationContext context) {
			
			final var obj = new JsonObject();
			
			obj.addProperty("x", src.getX());
			obj.addProperty("y", src.getY());
			obj.addProperty("z", src.getZ());
			
			return obj;
		}
		
	}
}
