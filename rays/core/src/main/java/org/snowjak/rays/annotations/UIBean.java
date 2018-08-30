package org.snowjak.rays.annotations;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import io.github.classgraph.ClassGraph;

/**
 * Bean implementor for classes annotated with {@link UIType}/{@link UIField}.
 * 
 * @author snowjak88
 *
 */
public class UIBean<T> {
	
	private final Class<T> type;
	
	private final String jsonType;
	private final Map<String, Class<?>> fieldTypes;
	private final Map<String, Class<?>> fieldCollectedTypes;
	private final Map<String, Object> fieldValues;
	private final Map<String, Supplier<Object>> fieldDefaultValues;
	private final Map<String, Supplier<Object>> fieldDefaultCollectedValues;
	private final Map<String, Collection<Class<?>>> fieldAvailableTypes;
	
	private static final Map<Class<? extends Number>, Function<String, ? extends Number>> NUMBER_CONVERTER = new HashMap<>();
	static {
		NUMBER_CONVERTER.put(Byte.class, (s) -> Byte.parseByte(s));
		NUMBER_CONVERTER.put(Double.class, (s) -> Double.parseDouble(s));
		NUMBER_CONVERTER.put(Float.class, (s) -> Float.parseFloat(s));
		NUMBER_CONVERTER.put(Integer.class, (s) -> Integer.parseInt(s));
		NUMBER_CONVERTER.put(Long.class, (s) -> Long.parseLong(s));
		NUMBER_CONVERTER.put(Short.class, (s) -> Short.parseShort(s));
	}
	
	public UIBean(Class<T> type) throws UIAnnotationMissing {
		
		this.type = type;
		fieldTypes = new HashMap<>();
		fieldCollectedTypes = new HashMap<>();
		fieldValues = new HashMap<>();
		fieldDefaultValues = new HashMap<>();
		fieldDefaultCollectedValues = new HashMap<>();
		fieldAvailableTypes = new HashMap<>();
		
		//
		//
		// Does this type have the proper annotations?
		final var typeDef = type.getAnnotation(UIType.class);
		if (typeDef == null)
			throw new UIAnnotationMissing(
					"Cannot construct UIBean for [" + type.getName() + "] -- missing UIType annotation!");
		
		jsonType = typeDef.type();
		//
		//
		// Start reading that annotation.
		for (UIField field : typeDef.fields()) {
			
			fieldTypes.put(field.name(), field.type());
			
			if (Collection.class.isAssignableFrom(field.type()))
				fieldCollectedTypes.put(field.name(), field.collectedType());
				
			//
			// Does this field have a default-value we can use?
			final var newValue = getNewDefaultValueForField(field.type(), field.defaultValue());
			if (newValue != null) {
				
				fieldValues.put(field.name(), newValue);
				fieldDefaultValues.put(field.name(), () -> newValue);
				fieldAvailableTypes.put(field.name(), Arrays.asList(field.type()));
				
				if (Collection.class.isAssignableFrom(field.type())) {
					fieldAvailableTypes.put(field.name(), getAnnotatedTypes(field.collectedType()));
					
					final var defaultCollectedValue = getNewDefaultValueForField(field.collectedType(),
							field.defaultValue());
					if (defaultCollectedValue != null)
						fieldDefaultCollectedValues.put(field.name(), () -> defaultCollectedValue);
					else {
						
						final var possibleValueCollectedTypes = getAnnotatedTypes(field.collectedType());
						
						if (possibleValueCollectedTypes.size() == 0)
							throw new UIAnnotationMissing("Class [" + type + "] defines UI-field \"" + field.name()
									+ "\", expecting collected-type [" + field.collectedType()
									+ "] -- but that type isn't annotated with UIType!");
							
						//
						// Possibly more than one allowable type in this field.
						// Pick the first one.
						final var firstPossibleType = possibleValueCollectedTypes.iterator().next();
						fieldDefaultCollectedValues.put(field.name(), () -> new UIBean<>(firstPossibleType));
					}
				}
				
			} else {
				
				final var possibleValueTypes = getAnnotatedTypes(field.type());
				
				if (possibleValueTypes.size() == 0)
					throw new UIAnnotationMissing(
							"Class [" + type + "] defines UI-field \"" + field.name() + "\", expecting type ["
									+ field.type() + "] -- but that type isn't annotated with UIType!");
					
				//
				// Possibly more than one allowable type in this field.
				// Pick the first one.
				final var firstPossibleType = possibleValueTypes.iterator().next();
				fieldTypes.put(field.name(), firstPossibleType);
				fieldValues.put(field.name(), new UIBean<>(firstPossibleType));
				fieldDefaultValues.put(field.name(), () -> new UIBean<>(firstPossibleType));
				fieldAvailableTypes.put(field.name(), possibleValueTypes);
				
			}
			
		}
	}
	
	private Object getNewDefaultValueForField(Class<?> fieldType, String defaultValue) {
		
		if (String.class.isAssignableFrom(fieldType))
			return defaultValue;
		
		if (Boolean.class.isAssignableFrom(fieldType))
			return Boolean.parseBoolean(defaultValue);
		
		if (Number.class.isAssignableFrom(fieldType))
			return NUMBER_CONVERTER.get(fieldType).apply(defaultValue);
		
		if (Collection.class.isAssignableFrom(fieldType))
			return new LinkedList<>();
		
		return null;
	}
	
	private Collection<Class<?>> getAnnotatedTypes(Class<?> supertype) {
		
		try (var scan = new ClassGraph().enableAllInfo()
				.whitelistPackages("org.snowjak.rays", supertype.getPackageName()).scan()) {
			
			final List<Class<?>> list;
			
			if (supertype.isInterface())
				list = scan.getClassesImplementing(supertype.getName())
						.filter((ci) -> ci.hasAnnotation(UIType.class.getName())).loadClasses();
			else
				list = scan.getSubclasses(supertype.getName()).filter((ci) -> ci.hasAnnotation(UIType.class.getName()))
						.loadClasses();
			
			if (supertype.getAnnotation(UIType.class) != null) {
				final var listWithSupertype = new LinkedList<Class<?>>();
				listWithSupertype.add(supertype);
				listWithSupertype.addAll(list);
				return listWithSupertype;
			}
			
			return list;
		}
	}
	
	/**
	 * @return the type for which this UIBean has been initialized
	 */
	public Class<T> getType() {
		
		return type;
	}
	
	/**
	 * Get the list of all field-names detected by this UIBean.
	 * 
	 * @return
	 */
	public Collection<String> getFieldNames() {
		
		return fieldTypes.keySet();
	}
	
	/**
	 * Get the type assigned to this field-name.
	 * 
	 * @param fieldName
	 * @return <code>null</code> if the given field-name is not recognized
	 */
	public Class<?> getFieldType(String fieldName) {
		
		return fieldTypes.get(fieldName);
	}
	
	/**
	 * Get the "collected-type" assigned to this field-name, if available
	 * 
	 * @param fieldName
	 * @return <code>null</code> if the given field-name is not recognized or does
	 *         not have a collected-type assigned
	 */
	public Class<?> getFieldCollectedType(String fieldName) {
		
		return fieldCollectedTypes.get(fieldName);
	}
	
	/**
	 * Get the current value assigned to this particular field-name.
	 * <p>
	 * This will be one of several possible types:
	 * <ul>
	 * <li>{@link String}</li>
	 * <li>a {@link Number}-subtype</li>
	 * <li>{@link Boolean}</li>
	 * <li>a {@link Collection}-subtype</li>
	 * <li>a child {@link UIBean}</li>
	 * <li>{@link Void} (if the target-class of the UIBean hasn't been selected
	 * yet)</li>
	 * </ul>
	 * </p>
	 * 
	 * @param fieldName
	 * @return <code>null</code> if the given field-name is not recognized
	 */
	public Object getFieldValue(String fieldName) {
		
		return fieldValues.get(fieldName);
	}
	
	/**
	 * Get a list of all available types that can be assigned to the given
	 * field-name.
	 * 
	 * @param fieldName
	 * @return <code>null</code> if the given field-name is not recognized
	 */
	public Collection<Class<?>> getFieldAvailableTypes(String fieldName) {
		
		if (!fieldAvailableTypes.containsKey(fieldName))
			return null;
		
		if (fieldAvailableTypes.get(fieldName) == null)
			fieldAvailableTypes.put(fieldName, Arrays.asList(fieldTypes.get(fieldName)));
		
		return fieldAvailableTypes.get(fieldName);
	}
	
	/**
	 * Set the current value for this particular field-name.
	 * 
	 * @param fieldName
	 * @param value
	 * @throws InvalidTypeReferenceException
	 *             if the given value's type is not assignable to the expected
	 *             field-type
	 */
	public void setFieldValue(String fieldName, Object value) throws InvalidTypeReferenceException {
		
		final var isDirectlyAssignable = getFieldType(fieldName).isAssignableFrom(value.getClass());
		final var isUiBean = value instanceof UIBean;
		
		if (!isDirectlyAssignable && !isUiBean)
			throw new InvalidTypeReferenceException("Cannot assigned a [" + value.getClass().getName() + "] to a ["
					+ getFieldType(fieldName).getName() + "]");
		
		fieldValues.put(fieldName, value);
	}
	
	/**
	 * Given a field-name which denotes a Collection-type field, add a new object of
	 * an acceptable type to that field's value.
	 * 
	 * @param fieldName
	 * @return the new value, also just added to the Collection
	 */
	@SuppressWarnings("unchecked")
	public Object addToCollection(String fieldName) {
		
		if (getFieldCollectedType(fieldName) == null)
			return null;
		
		final Object newItem = fieldDefaultCollectedValues.get(fieldName).get();
		
		((Collection<Object>) getFieldValue(fieldName)).add(newItem);
		return newItem;
	}
	
	public static class Serializer implements JsonSerializer<UIBean<?>> {
		
		@Override
		public JsonElement serialize(UIBean<?> src, Type typeOfSrc, JsonSerializationContext context) {
			
			final var obj = new JsonObject();
			
			if (src.jsonType != null && !src.jsonType.trim().isEmpty())
				obj.addProperty("type", src.jsonType);
			
			for (String fieldName : src.getFieldNames()) {
				
				final var val = src.getFieldValue(fieldName);
				
				if (val instanceof UIBean)
					obj.add(fieldName, context.serialize(val));
				else if (val instanceof Boolean)
					obj.addProperty(fieldName, (Boolean) val);
				else if (val instanceof Number)
					obj.addProperty(fieldName, (Number) val);
				else if (val instanceof String)
					obj.addProperty(fieldName, (String) val);
				else if (val instanceof Collection) {
					
					final var array = new JsonArray();
					for (var v : (Collection<?>) val)
						array.add(context.serialize(v, src.getFieldCollectedType(fieldName)));
					
					obj.add(fieldName, array);
					
				}
			}
			
			return obj;
		}
		
	}
	
	/**
	 * Indicates that the given type does not have the expected
	 * {@link UIType}/{@link UIField} annotations.
	 * 
	 * @author snowjak88
	 *
	 */
	public static class UIAnnotationMissing extends RuntimeException {
		
		private static final long serialVersionUID = -6498178640533984053L;
		
		public UIAnnotationMissing(String message) {
			
			super(message);
		}
		
	}
	
	/**
	 * Indicates that the given value is not of a suitable type -- i.e., the given
	 * type is not assignable to the expected type.
	 * 
	 * @author snowjak88
	 *
	 */
	public static class InvalidTypeReferenceException extends RuntimeException {
		
		public InvalidTypeReferenceException(String message) {
			
			super(message);
		}
		
		private static final long serialVersionUID = -1906859776084785480L;
		
	}
}