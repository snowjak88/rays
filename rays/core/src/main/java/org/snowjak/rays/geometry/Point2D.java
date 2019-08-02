package org.snowjak.rays.geometry;

import java.io.Serializable;
import java.lang.reflect.Type;

import org.snowjak.rays.annotations.UIType;
import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.geometry.util.Pair;
import org.snowjak.rays.serialization.IsLoadable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

/**
 * Semantic repackaging of {@link Pair}.
 * 
 * <h3>JSON</h3>
 * <p>A Point2D may be serialized to JSON in the following format.</p>
 * <pre>
 * ...
 * {
 *     "x": <em>x</em>,
 *     "y": <em>y</em>
 * }
 * ...
 * </pre>
 * 
 * @author snowjak88
 */
@UIType(fields = { @UIField(name = "x", type = Double.class, defaultValue = "0"),
		@UIField(name = "y", type = Double.class, defaultValue = "0") })
public class Point2D extends Pair implements Serializable {
	
	private static final long serialVersionUID = -7421036355210501663L;
	
	/**
	 * <code>{ 0, 0 }</code>
	 */
	public static final Point2D ZERO = new Point2D(0, 0);
	/**
	 * <code>{ .5, .5 }</code>
	 */
	public static final Point2D HALF = new Point2D(0.5, 0.5);
	/**
	 * <code>{ 1, 1 }</code>
	 */
	public static final Point2D ONE = new Point2D(1, 1);
	
	public static Point2D from(Pair p) {
		
		if (Point2D.class.isAssignableFrom(p.getClass()))
			return (Point2D) p;
		
		return new Point2D(p.get(0), p.get(1));
	}
	
	public Point2D(double x, double y) {
		
		super(x, y);
	}
	
	/**
	 * Create a new Point2D whose coordinates consist of the first 2 values from the
	 * given <code>double[]</code> array. If the given array contains fewer than 2
	 * values, the array is 0-padded to make up a length-2 array.
	 * 
	 * @param values
	 */
	public Point2D(double... coordinates) {
		
		super(coordinates);
	}
	
	/**
	 * Initialize an empty Point2D.
	 */
	protected Point2D() {
		
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
	
	@Override
	public Point2D negate() {
		
		return Point2D.from(super.negate());
	}
	
	@Override
	public Point2D reciprocal() {
		
		return Point2D.from(super.reciprocal());
	}
	
	@Override
	public Point2D add(Pair addend) {
		
		return Point2D.from(super.add(addend));
	}
	
	@Override
	public Point2D add(double addend) {
		
		return Point2D.from(super.add(addend));
	}
	
	@Override
	public Point2D subtract(Pair subtrahend) {
		
		return Point2D.from(super.subtract(subtrahend));
	}
	
	@Override
	public Point2D subtract(double subtrahend) {
		
		return Point2D.from(super.subtract(subtrahend));
	}
	
	@Override
	public Point2D multiply(Pair multiplicand) {
		
		return Point2D.from(super.multiply(multiplicand));
	}
	
	@Override
	public Point2D multiply(double multiplicand) {
		
		return Point2D.from(super.multiply(multiplicand));
	}
	
	@Override
	public Point2D divide(Pair divisor) {
		
		return Point2D.from(super.divide(divisor));
	}
	
	@Override
	public Point2D divide(double divisor) {
		
		return Point2D.from(super.divide(divisor));
	}
	
	public static class Loader implements IsLoadable<Point2D> {
		
		@Override
		public Point2D deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			
			if (!json.isJsonObject())
				throw new JsonParseException("Cannot deserialize Point2D from JSON -- expecting a JSON object!");
			
			final var obj = json.getAsJsonObject();
			
			final var x = obj.get("x").getAsDouble();
			final var y = obj.get("y").getAsDouble();
			
			return new Point2D(x, y);
		}
		
		@Override
		public JsonElement serialize(Point2D src, Type typeOfSrc, JsonSerializationContext context) {
			
			final var obj = new JsonObject();
			
			obj.addProperty("x", src.getX());
			obj.addProperty("y", src.getY());
			
			return obj;
		}
		
	}
}
