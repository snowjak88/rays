package org.snowjak.rays.frontend.ui.presentation;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.frontend.ui.presentation.ClassFieldAnalyzer.ObjectField;
import org.springframework.stereotype.Component;

import com.vaadin.data.TreeData;

/**
 * Encapsulates a general object-configuration mechanism.
 * <p>
 * To use, call {@link #setClass(Class)}:
 * <ul>
 * <li>This instance will prepare a list of that class's available fields using
 * {@link ClassFieldAnalyzer}</li>
 * <li>This list of fields is available via {@link #getFieldNames()}, and
 * individually via {@link #getField(String)}</li>
 * <li>You may get and set each field's value via {@link #getValue(String)} and
 * {@link #setValue(String, Object)}</li>
 * <li>Finally, call {@link #instantiate(Class)} to instantiate a new instance
 * of this type. This object will use the constructor identified by
 * {@link ClassFieldAnalyzer}, if available.</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Important</strong>: if a field is of a type not considered to be an
 * "immediate" value, then the field's value will be initialized to another
 * {@link ClassPresentation} instance (i.e., recursive). <em>There is no guard
 * against stack-overflow here</em> -- e.g., if you're trying to handle a
 * self-referencing type (e.g., {@link TreeData}), this <em>will</em> cause a
 * stack-overflow unless you explicitly use {@link #setClass(Class, boolean)}.
 * </p>
 * <p>
 * A list of "immediate" field-types is given here:
 * <ul>
 * <li>{@link String}</li>
 * <li>{@link Boolean}</li>
 * <li>{@code boolean}</li>
 * <li>{@link Byte}</li>
 * <li>{@code byte}</li>
 * <li>{@link Short}</li>
 * <li>{@code short}</li>
 * <li>{@link Integer}</li>
 * <li>{@code int}</li>
 * <li>{@link Long}</li>
 * <li>{@code long}</li>
 * <li>{@link Float}</li>
 * <li>{@code float}</li>
 * <li>{@link Double}</li>
 * <li>{@code double}</li>
 * </ul>
 * </p>
 * 
 * @author snowjak88
 *
 */
@Component
public class ClassPresentation {
	
	private static final Logger LOG = LoggerFactory.getLogger(ClassPresentation.class);
	
	private ClassFieldAnalyzer analyzer = null;
	private Map<String, Object> values = new HashMap<>();
	
	public void setClass(Class<?> clazz) {
		
		setClass(clazz, true);
	}
	
	public void setClass(Class<?> clazz, boolean doRecursive) {
		
		analyzer = new ClassFieldAnalyzer(clazz);
		values.clear();
		
		for (ObjectField of : analyzer.getFields()) {
			final Object value;
			
			if (String.class.isAssignableFrom(of.getType()))
				value = "";
			else if (Number.class.isAssignableFrom(of.getType()))
				value = "0";
			else if (of.getType().isPrimitive()
					&& (Byte.TYPE.isAssignableFrom(of.getType()) || Short.TYPE.isAssignableFrom(of.getType())
							|| Integer.TYPE.isAssignableFrom(of.getType()) || Long.TYPE.isAssignableFrom(of.getType())
							|| Float.TYPE.isAssignableFrom(of.getType()) || Double.TYPE.isAssignableFrom(of.getType())))
				value = "0";
			else if (Boolean.class.isAssignableFrom(of.getType()) || Boolean.TYPE.isAssignableFrom(of.getType()))
				value = "false";
			else {
				value = new ClassPresentation();
				((ClassPresentation) value).setClass(of.getType(), doRecursive);
			}
			
			values.put(of.getName(), value);
		}
	}
	
	public List<String> getFieldNames() {
		
		if (analyzer == null)
			return Collections.emptyList();
		
		return analyzer.getFields().stream().map(ObjectField::getName).collect(Collectors.toList());
	}
	
	public Object getValue(String fieldName) {
		
		return values.getOrDefault(fieldName, "");
	}
	
	public void setValue(String fieldName, Object value) {
		
		values.put(fieldName, value);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T instantiate(Class<T> type) {
		
		if (!type.isAssignableFrom(analyzer.getAnalyzedClass())) {
			LOG.error("Cannot take the type {} and instantiate it as type {}!", analyzer.getAnalyzedClass().getName(),
					type.getName());
			return null;
		}
		
		T obj;
		try {
			
			if (analyzer.getConstructor() == null)
				obj = (T) analyzer.getAnalyzedClass().getConstructor().newInstance();
			
			else {
				
				final var constructor = analyzer.getConstructor();
				final Object[] args = new Object[constructor.getParameters().length];
				
				for (int i = 0; i < args.length; i++) {
					
					final var param = constructor.getParameters()[i];
					args[i] = analyzer.getFields().stream().filter(of -> of.getName().equals(param.getName())
							&& of.getType().isAssignableFrom(param.getType())).map(of -> {
								
								if (getValue(of.getName()) instanceof ClassPresentation)
									return ((ClassPresentation) getValue(of.getName())).instantiate(of.getType());
								
								if (of.getType().isPrimitive() && (Byte.TYPE.isAssignableFrom(of.getType())
										|| Short.TYPE.isAssignableFrom(of.getType())
										|| Integer.TYPE.isAssignableFrom(of.getType())
										|| Long.TYPE.isAssignableFrom(of.getType())
										|| Float.TYPE.isAssignableFrom(of.getType())
										|| Double.TYPE.isAssignableFrom(of.getType())))
									return Number.class.cast(getValue(of.getName()));
								
								if (Boolean.class.isAssignableFrom(of.getType())
										|| Boolean.TYPE.isAssignableFrom(of.getType()))
									return Boolean.class.cast(getValue(of.getName()));
								
								return of.getType().cast(getValue(of.getName()));
								
							}).findFirst().orElse(null);
					
				}
				
				obj = (T) constructor.newInstance(args);
			}
			
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException |
				
				SecurityException e) {
			LOG.error("Cannot instantiate object of type {}! -- {}: {}", analyzer.getAnalyzedClass().getName(),
					e.getClass().getSimpleName(), e.getMessage());
			
			return null;
		}
		for (ObjectField of : analyzer.getFields()) {
			
			if (of.getSetter() != null)
				if (values.get(of.getName()) instanceof ClassPresentation) {
					of.getSetter().accept(obj,
							((ClassPresentation) values.get(of.getName())).instantiate(of.getType()));
				} else
					of.getSetter().accept(obj, values.get(of.getName()));
		}
		
		return obj;
	}
	
	public Optional<ObjectField> getField(String fieldName) {
		
		if (analyzer == null)
			return Optional.empty();
		
		return analyzer.getFields().stream().filter(of -> of.getName().equals(fieldName)).findFirst();
	}
	
}
