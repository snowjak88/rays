package org.snowjak.rays.geometry.util;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import org.snowjak.rays.serialization.IsLoadable;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

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
	
	public static class Loader implements IsLoadable<Point> {
		
		@Override
		public Point deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			
			if (!json.isJsonArray())
				throw new JsonParseException("Cannot deserialize a vector from JSON that is not given as an array!");
			
			final var array = json.getAsJsonArray();
			
			final var values = new double[array.size()];
			for (int i = 0; i < values.length; i++)
				values[i] = array.get(i).getAsDouble();
			
			return new Point(values);
		}
		
		@Override
		public JsonElement serialize(Point src, Type typeOfSrc, JsonSerializationContext context) {
			
			final var array = new JsonArray(1);
			array.add(new JsonPrimitive(src.get(0)));
			
			return array;
		}
		
	}
	
}
