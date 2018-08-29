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

@Component
public class ClassPresentation {
	
	private static final Logger LOG = LoggerFactory.getLogger(ClassPresentation.class);
	
	private ClassFieldAnalyzer analyzer = null;
	private Map<String, Object> values = new HashMap<>();
	
	public void setClass(Class<?> clazz) {
		
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
			else {
				value = new ClassPresentation();
				((ClassPresentation) value).setClass(of.getType());
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
