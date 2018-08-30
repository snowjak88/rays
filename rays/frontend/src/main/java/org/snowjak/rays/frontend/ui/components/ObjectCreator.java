package org.snowjak.rays.frontend.ui.components;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.snowjak.rays.Settings;
import org.snowjak.rays.frontend.ui.presentation.ClassPresentation;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

@org.springframework.stereotype.Component
public class ObjectCreator extends FormLayout {
	
	private static final long serialVersionUID = -2320590174526944151L;
	
	private static final Map<Class<?>, Function<String, Component>> CLASS_COMPONENTS = new HashMap<>();
	static {
		CLASS_COMPONENTS.put(Boolean.class,
				(caption) -> new NativeSelect<String>(caption, Arrays.asList("true", "false")));
		CLASS_COMPONENTS.put(Boolean.TYPE,
				(caption) -> new NativeSelect<String>(caption, Arrays.asList("true", "false")));
	}
	
	@Autowired
	private ClassPresentation presentation;
	
	private TextArea jsonOutput;
	
	public ObjectCreator() {
		
		super();
		
		addListener((e) -> {
			if (e instanceof ValueChangedEvent) {
				
				final var json = Settings.getInstance().getGson().toJson(getValue());
				
				jsonOutput.setValue(json);
				
			}
		});
	}
	
	public void setClass(Class<?> clazz) {
		
		presentation.setClass(clazz);
		refreshForm();
	}
	
	private void refreshForm() {
		
		removeAllComponents();
		
		for (String fieldName : presentation.getFieldNames()) {
			
			final var of = presentation.getField(fieldName).get();
			
			final Component component;
			if (CLASS_COMPONENTS.containsKey(of.getType()))
				component = CLASS_COMPONENTS.get(of.getType()).apply(of.getName());
			
			else if (presentation.getValue(fieldName) instanceof ClassPresentation) {
				component = new ObjectCreator();
				((ObjectCreator) component).setClass(of.getType());
				component.setCaption(of.getName());
				
				component.addListener((e) -> {
					
					if (e instanceof ValueChangedEvent) {
						final var p = ((ObjectCreator) e.getComponent()).presentation;
						final Class<?> type = p.getPresentedClass();
						presentation.setValue(e.getComponent().getCaption(), p.instantiate(type));
						
						fireEvent(new ValueChangedEvent(this));
					}
				});
				
			} else {
				
				component = new TextField();
				component.setCaption(of.getName());
				
				((TextField) component).addValueChangeListener((e) -> {
					presentation.setValue(of.getName(), e.getValue());
					
					fireEvent(new ValueChangedEvent(this));
				});
			}
			
			addComponent(component);
			
		}
		
		jsonOutput = new TextArea();
		jsonOutput.setCaption("JSON");
		jsonOutput.setReadOnly(true);
		addComponent(jsonOutput);
	}
	
	public Object getValue() {
		
		return presentation.instantiate(presentation.getPresentedClass());
	}
	
	public static class ValueChangedEvent extends Event {
		
		private static final long serialVersionUID = -8497980777373170645L;
		
		public ValueChangedEvent(Component source) {
			
			super(source);
		}
		
	}
	
}
