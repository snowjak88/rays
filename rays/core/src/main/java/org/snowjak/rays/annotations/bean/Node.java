package org.snowjak.rays.annotations.bean;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public interface Node {
	
	/**
	 * @return this Node's assigned type
	 */
	public Class<?> getType();
	
	/**
	 * Reassign this Node's type.
	 * 
	 * @param type
	 * @throws UnsupportedClassException
	 *             if this Node cannot take on the assigned type for some reason
	 */
	public void setType(Class<?> type) throws UnsupportedClassException;
	
	/**
	 * @return this Node's declared type (may be a supertype of {@link #getType()})
	 */
	public Class<?> getDeclaredType();
	
	/**
	 * @return a new instance of an acceptable default-value for this node
	 */
	public Object getDefaultValue();
	
	public JsonElement serialize(JsonSerializationContext context);
	
	public static class UnsupportedClassException extends RuntimeException {
		
		public UnsupportedClassException(String message) {
			
			super(message);
		}
		
		private static final long serialVersionUID = 3955178124066274005L;
		
	}
	
	public static class Serializer implements JsonSerializer<Node> {
		
		@Override
		public JsonElement serialize(Node src, Type typeOfSrc, JsonSerializationContext context) {
			
			return src.serialize(context);
		}
		
	}
}
