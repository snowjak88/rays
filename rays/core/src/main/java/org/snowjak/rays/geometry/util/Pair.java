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
 * Represents a vector of 2 values.
 * 
 * @author snowjak88
 */
public class Pair extends AbstractVector<Pair> implements Serializable {
	
	private static final long serialVersionUID = 8826976880964290469L;
	
	/**
	 * <code>{ 0, 0 }</code>
	 */
	public static final Pair ZERO = new Pair(0, 0);
	/**
	 * <code>{ .5, .5 }</code>
	 */
	public static final Pair HALF = new Pair(0.5, 0.5);
	/**
	 * <code>{ 1, 1 }</code>
	 */
	public static final Pair ONE = new Pair(1, 1);
	
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
	
	public static class Loader implements IsLoadable<Pair> {
		
		@Override
		public Pair deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			
			if (!json.isJsonArray())
				throw new JsonParseException("Cannot deserialize a vector from JSON that is not given as an array!");
			
			final var array = json.getAsJsonArray();
			
			final var values = new double[array.size()];
			for (int i = 0; i < values.length; i++)
				values[i] = array.get(i).getAsDouble();
			
			return new Pair(values);
		}
		
		@Override
		public JsonElement serialize(Pair src, Type typeOfSrc, JsonSerializationContext context) {
			
			final var array = new JsonArray(2);
			array.add(new JsonPrimitive(src.get(0)));
			array.add(new JsonPrimitive(src.get(1)));
			
			return array;
		}
		
	}
}
