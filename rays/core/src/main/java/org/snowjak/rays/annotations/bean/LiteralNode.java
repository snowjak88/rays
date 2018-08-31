package org.snowjak.rays.annotations.bean;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

public class LiteralNode implements Node {
	
	static final Map<Class<?>, Function<String, Object>> CONVERTERS = new HashMap<>();
	static final Map<Class<?>, Function<Object, JsonElement>> SERIALIZERS = new HashMap<>();
	
	static {
		CONVERTERS.put(Byte.class, Byte::parseByte);
		CONVERTERS.put(Double.class, Double::parseDouble);
		CONVERTERS.put(Float.class, Float::parseFloat);
		CONVERTERS.put(Integer.class, Integer::parseInt);
		CONVERTERS.put(Long.class, Long::parseLong);
		CONVERTERS.put(Short.class, Short::parseShort);
		CONVERTERS.put(String.class, (s) -> s);
		CONVERTERS.put(Boolean.class, Boolean::parseBoolean);
		
		SERIALIZERS.put(Number.class, (v) -> new JsonPrimitive((Number) v));
		SERIALIZERS.put(String.class, (v) -> new JsonPrimitive((String) v));
		SERIALIZERS.put(Boolean.class, (v) -> new JsonPrimitive((Boolean) v));
	}
	
	private final Class<?> type;
	private final String stringDefaultValue;
	
	private Object value;
	
	public static Collection<Class<?>> getAcceptableTypes(Class<?> type) {
		
		return CONVERTERS.keySet().stream().filter(c -> type.isAssignableFrom(c)).collect(Collectors.toList());
	}
	
	public LiteralNode(Class<?> type, String stringDefaultValue) {
		
		if (CONVERTERS.keySet().stream().allMatch(c -> !c.isAssignableFrom(type)))
			throw new UnsupportedClassException("Cannot create LiteralNode of type [" + type.getName() + "].");
		
		this.type = type;
		this.stringDefaultValue = stringDefaultValue;
		
		this.value = getDefaultValue();
	}
	
	@Override
	public Class<?> getType() {
		
		return type;
	}
	
	/**
	 * @return this node's current value
	 */
	public Object getValue() {
		
		return value;
	}
	
	/**
	 * Set this node's current value
	 * 
	 * @param value
	 * @throws UnsupportedOperationException
	 *             if the given value cannot be assigned to this node (because the
	 *             given value is not a subtype of this node's type)
	 */
	public void setValue(Object value) {
		
		if (!type.isAssignableFrom(value.getClass()))
			throw new UnsupportedOperationException(
					"Cannot assign a [" + value.getClass().getSimpleName() + "] to a [" + type.getSimpleName() + "].");
		this.value = value;
	}
	
	protected String getStringDefaultValue() {
		
		return stringDefaultValue;
	}
	
	@Override
	public Object getDefaultValue() {
		
		return CONVERTERS.entrySet().stream().filter(e -> e.getKey().isAssignableFrom(getType())).findFirst()
				.map(e -> e.getValue()).get().apply(getStringDefaultValue());
	}
	
	@Override
	public JsonElement serialize(JsonSerializationContext context) {
		
		return SERIALIZERS.entrySet().stream().filter(e -> e.getKey().isAssignableFrom(getType())).findFirst()
				.map(e -> e.getValue()).get().apply(getValue());
	}
	
}
