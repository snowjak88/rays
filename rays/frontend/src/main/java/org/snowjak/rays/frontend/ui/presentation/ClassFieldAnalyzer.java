package org.snowjak.rays.frontend.ui.presentation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Given a Class instance, this analyzer will:
 * 
 * <ol>
 * <li>analyze all its declared fields
 * <li>identify all those non-transient, non-static fields' getters and (if
 * available) setters</li>
 * <li>identify the constructor with the fewest parameters which will cover all
 * fields lacking setters</li>
 * </ol>
 * 
 * <p>
 * A couple of <strong>important caveats</strong>:
 * <ul>
 * <li>This analyzer will only list fields that are not transient and not
 * static</li>
 * <li>This analyzer expects every field to have a getter with a standard name
 * -- e.g., for {@code field}, it looks for a 0-parameter method with the name
 * {@code getField()}</li>
 * <li>This analyzer expects to have parameter-name information available.
 * Therefore, you <strong>must</strong> compile this code with the {@code javac}
 * option {@code -parameters}!</li>
 * </ul>
 * </p>
 * 
 * @author snowjak88
 *
 */
public class ClassFieldAnalyzer {
	
	private static final Logger LOG = LoggerFactory.getLogger(ClassFieldAnalyzer.class);
	
	private Class<?> clazz;
	private List<ObjectField> fields = new LinkedList<>();
	private Constructor<?> constructor;
	
	public ClassFieldAnalyzer(Class<?> clazz) {
		
		LOG.info("Constructing ClassFieldAnalyzer for {}", clazz.getName());
		
		this.clazz = clazz;
		
		LOG.debug("Scanning declared fields on {}", clazz.getName());
		for (Field field : clazz.getDeclaredFields()) {
			
			final var fieldName = field.getName();
			LOG.trace("Declared field: {}", fieldName);
			
			if (Modifier.isTransient(field.getModifiers())) {
				LOG.trace("Ignoring because transient.");
				continue;
			}
			if (Modifier.isStatic(field.getModifiers())) {
				LOG.trace("Ignoring because static.");
				continue;
			}
			
			var getterName = "get" + fieldName.substring(0, 1).toUpperCase();
			if (fieldName.length() > 1)
				getterName += fieldName.substring(1);
			
			LOG.trace("Looking for getter method: {}", getterName);
			
			final Method getterMethod;
			try {
				getterMethod = clazz.getDeclaredMethod(getterName);
			} catch (SecurityException | NoSuchMethodException e) {
				LOG.warn("{} while accessing getter-method {} for {}.{} -- skipping this field.",
						e.getClass().getSimpleName(), getterName, clazz.getName(), fieldName);
				continue;
			}
			
			if (!Modifier.isPublic(getterMethod.getModifiers())) {
				LOG.info("{}.{} is not public -- skipping this field.", clazz.getName(), getterName);
				continue;
			}
			
			var setterName = "set" + fieldName.substring(0, 1).toUpperCase();
			if (fieldName.length() > 1)
				setterName += fieldName.substring(1);
			
			LOG.trace("Looking for setter method: {}", setterName);
			
			Method setterMethod;
			try {
				setterMethod = clazz.getDeclaredMethod(setterName, field.getType());
				
				if (!Modifier.isPublic(setterMethod.getModifiers())) {
					LOG.info("{}.{} is not public -- skipping this field.", clazz.getName(), setterName);
					setterMethod = null;
				}
				
			} catch (SecurityException | NoSuchMethodException e) {
				
				LOG.warn("{} while accessing setter-method {} for {}.{} -- skipping this method.",
						e.getClass().getSimpleName(), setterName, clazz.getName(), fieldName);
				setterMethod = null;
				
			}
			
			final Function<Object, Object> getter = (obj) -> {
				try {
					return getterMethod.invoke(obj);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					LOG.error("Cannot invoke getter-method!", e);
				}
				return null;
			};
			
			final var finalSetterMethod = setterMethod;
			final BiConsumer<Object, Object> setter;
			if (setterMethod == null)
				setter = null;
			else
				setter = (obj, val) -> {
					try {
						finalSetterMethod.invoke(obj, val);
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						LOG.error("Cannot invoke setter-method!", e);
					}
				};
			
			fields.add(new ObjectField(fieldName, field.getType(), getter, setter));
		}
		
		LOG.debug("Identifying suitable constructor for type {}.", clazz);
		
		LOG.trace("Identifying all fields not covered by setters (and so needing a constructor).");
		final var fieldsWithoutSetters = fields.stream().filter(of -> of.getSetter() == null)
				.collect(Collectors.toList());
		
		LOG.trace("Identifying constructor with least number of fields, that covers all these fields.");
		
		Constructor<?> bestConstructor = null;
		int bestParameterCount = 32767;
		for (Constructor<?> c : clazz.getConstructors()) {
			
			if (fieldsWithoutSetters.stream().allMatch(of -> Arrays.stream(c.getParameters())
					.anyMatch(p -> p.getName().equals(of.getName()) && of.getType().isAssignableFrom(p.getType())))) {
				
				LOG.trace("Found constructor which covers all these fields: {}", c.toString());
				
				if (c.getParameters().length < bestParameterCount) {
					LOG.trace("Constructor has fewer parameters ({}) than current best ({}). Best constructor so far.",
							c.getParameters().length, bestParameterCount);
					bestConstructor = c;
					bestParameterCount = c.getParameters().length;
				}
			}
			
		}
		
		constructor = bestConstructor;
	}
	
	public Class<?> getAnalyzedClass() {
		
		return clazz;
	}
	
	public List<ObjectField> getFields() {
		
		return fields;
	}
	
	public Constructor<?> getConstructor() {
		
		return constructor;
	}
	
	public static class ObjectField {
		
		private String name;
		private Class<?> type;
		private Function<Object, Object> getter;
		private BiConsumer<Object, Object> setter;
		
		public ObjectField(String name, Class<?> type, Function<Object, Object> getter,
				BiConsumer<Object, Object> setter) {
			
			this.name = name;
			this.type = type;
			this.getter = getter;
			this.setter = setter;
		}
		
		public String getName() {
			
			return name;
		}
		
		public Class<?> getType() {
			
			return type;
		}
		
		public Function<Object, Object> getGetter() {
			
			return getter;
		}
		
		public BiConsumer<Object, Object> getSetter() {
			
			return setter;
		}
		
	}
}
