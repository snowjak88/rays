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
	 * <code>double[]</code> array. If the given array contains fewer than 2 values,
	 * the array is 0-padded to make up a length-2 array.
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
	
	public static class Loader implements IsLoadable<Triplet> {
		
		@Override
		public Triplet deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			
			if (!json.isJsonArray())
				throw new JsonParseException("Cannot deserialize a vector from JSON that is not given as an array!");
			
			final var array = json.getAsJsonArray();
			
			final var values = new double[array.size()];
			for (int i = 0; i < values.length; i++)
				values[i] = array.get(i).getAsDouble();
			
			return new Triplet(values);
		}
		
		@Override
		public JsonElement serialize(Triplet src, Type typeOfSrc, JsonSerializationContext context) {
			
			final var array = new JsonArray(3);
			array.add(new JsonPrimitive(src.get(0)));
			array.add(new JsonPrimitive(src.get(1)));
			array.add(new JsonPrimitive(src.get(2)));
			
			return array;
		}
		
	}
}
