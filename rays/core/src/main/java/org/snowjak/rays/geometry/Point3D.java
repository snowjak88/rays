package org.snowjak.rays.geometry;

import java.io.Serializable;
import java.lang.reflect.Type;

import org.snowjak.rays.annotations.UIType;
import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.geometry.util.Triplet;
import org.snowjak.rays.serialization.IsLoadable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

/**
 * Semantic repackaging of {@link Triplet}.
 * 
 * @author snowjak88
 */
@UIType(fields = { @UIField(name = "x", type = Double.class, defaultValue = "0"),
		@UIField(name = "y", type = Double.class, defaultValue = "0"),
		@UIField(name = "z", type = Double.class, defaultValue = "0") })
public class Point3D extends Triplet implements Serializable {
	
	private static final long serialVersionUID = -1065315353543801672L;
	
	public final static Point3D ZERO = new Point3D(0, 0, 0);
	
	/**
	 * Convert the given {@link Triplet} into a Point3D.
	 */
	public static Point3D from(Triplet t) {
		
		if (Point3D.class.isAssignableFrom(t.getClass()))
			return (Point3D) t;
		
		return new Point3D(t.get(0), t.get(1), t.get(2));
	}
	
	public Point3D(double x, double y, double z) {
		
		super(x, y, z);
	}
	
	/**
	 * Create a new Point3D whose coordinates consist of the first 3 values from the
	 * given <code>double[]</code> array. If the given array contains fewer than 3
	 * values, the array is 0-padded to make up a length-3 array.
	 * 
	 * @param values
	 */
	public Point3D(double... coordinates) {
		
		super(coordinates);
	}
	
	/**
	 * Initialize an empty Point3D
	 */
	protected Point3D() {
		
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
	public Point3D negate() {
		
		return Point3D.from(super.negate());
	}
	
	@Override
	public Point3D reciprocal() {
		
		return Point3D.from(super.reciprocal());
	}
	
	@Override
	public Point3D add(Triplet addend) {
		
		return Point3D.from(super.add(addend));
	}
	
	@Override
	public Point3D add(double addend) {
		
		return Point3D.from(super.add(addend));
	}
	
	@Override
	public Point3D subtract(Triplet subtrahend) {
		
		return Point3D.from(super.subtract(subtrahend));
	}
	
	@Override
	public Point3D subtract(double subtrahend) {
		
		return Point3D.from(super.subtract(subtrahend));
	}
	
	@Override
	public Point3D multiply(Triplet multiplicand) {
		
		return Point3D.from(super.multiply(multiplicand));
	}
	
	@Override
	public Point3D multiply(double multiplicand) {
		
		return Point3D.from(super.multiply(multiplicand));
	}
	
	@Override
	public Point3D divide(Triplet divisor) {
		
		return Point3D.from(super.divide(divisor));
	}
	
	@Override
	public Point3D divide(double divisor) {
		
		return Point3D.from(super.divide(divisor));
	}
	
	public static class Loader implements IsLoadable<Point3D> {
		
		@Override
		public Point3D deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			
			if (!json.isJsonObject())
				throw new JsonParseException("Cannot deserialize Point3D from JSON -- expecting a JSON object!");
			
			final var obj = json.getAsJsonObject();
			
			final var x = obj.get("x").getAsDouble();
			final var y = obj.get("y").getAsDouble();
			final var z = obj.get("z").getAsDouble();
			
			return new Point3D(x, y, z);
		}
		
		@Override
		public JsonElement serialize(Point3D src, Type typeOfSrc, JsonSerializationContext context) {
			
			final var obj = new JsonObject();
			
			obj.addProperty("x", src.getX());
			obj.addProperty("y", src.getY());
			obj.addProperty("z", src.getZ());
			
			return obj;
		}
		
	}
}
