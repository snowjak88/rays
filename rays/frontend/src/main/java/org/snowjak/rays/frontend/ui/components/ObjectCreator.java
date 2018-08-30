package org.snowjak.rays.frontend.ui.components;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.Settings;
import org.snowjak.rays.annotations.UIBean;
import org.snowjak.rays.geometry.Vector3D;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@org.springframework.stereotype.Component
public class ObjectCreator extends FormLayout {
	
	private static final Logger LOG = LoggerFactory.getLogger(ObjectCreator.class);
	
	private static final long serialVersionUID = -2320590174526944151L;
	
	private static final Map<Class<?>, Function<String, Component>> CLASS_COMPONENTS = new HashMap<>();
	static {
		CLASS_COMPONENTS.put(Boolean.class,
				(caption) -> new NativeSelect<String>(caption, Arrays.asList("true", "false")));
		CLASS_COMPONENTS.put(Boolean.TYPE,
				(caption) -> new NativeSelect<String>(caption, Arrays.asList("true", "false")));
	}
	
	@Autowired
	private ConversionService conversion;
	
	private UIBean<?> bean;
	
	private TextArea jsonOutput;
	
	public ObjectCreator() {
		
		super();
		
		addListener((e) -> {
			if (e instanceof ValueChangedEvent) {
				
				LOG.info("Received ValueChangedEvent in root handler. Refreshing JSON.");
				
				final var json = Settings.getInstance().getGson().toJson(bean);
				
				jsonOutput.setValue(json);
				
			}
		});
	}
	
	public void setClass(Class<?> clazz) {
		
		bean = new UIBean<>(Vector3D.class);
		
		refreshForm();
	}
	
	private void refreshForm() {
		
		removeAllComponents();
		
		for (String fieldName : bean.getFieldNames()) {
			LOG.trace("Wrapping root bean field {} ...", fieldName);
			
			final var wrappedValue = wrapValue(bean, fieldName, bean.getFieldValue(fieldName));
			wrappedValue.addListener((e) -> {
				if (e instanceof ValueChangedEvent) {
					fireEvent(new ValueChangedEvent(wrappedValue));
				}
			});
			addComponent(wrappedValue);
		}
		
		jsonOutput = new TextArea();
		jsonOutput.setCaption("JSON");
		jsonOutput.setReadOnly(true);
		addComponent(jsonOutput);
	}
	
	private Component wrapValue(UIBean<?> parentBean, String fieldName, Object value) {
		
		LOG.trace("Wrapping value: {}.{} ...", parentBean.getType().getName(), fieldName);
		
		if (Collection.class.isAssignableFrom(value.getClass())) {
			
			LOG.trace("Value is a Collection<{}> ...", parentBean.getFieldCollectedType(fieldName).getName());
			
			final var component = new VerticalLayout();
			component.setCaption(fieldName);
			
			int i = 1;
			for (var item : (Collection<?>) value) {
				
				LOG.trace("Value {}[{}] ...", fieldName, i);
				
				final var itemLayout = new HorizontalLayout();
				itemLayout.addComponent(new Button(VaadinIcons.MINUS_CIRCLE, (ce) -> {
					((Collection<?>) value).remove(item);
					component.removeComponent(itemLayout);
				}));
				
				final var wrappedValue = wrapValue(parentBean, fieldName + "[" + i + "]", value);
				wrappedValue.addListener((e) -> {
					if (e instanceof ValueChangedEvent) {
						fireEvent(new ValueChangedEvent(component));
					}
				});
				itemLayout.addComponent(wrappedValue);
				
				component.addComponent(itemLayout);
				i++;
			}
			
			LOG.trace("Done wrapping {}.{} ...", parentBean.getType().getName(), fieldName);
			
			return component;
			
		} else if (UIBean.class.isAssignableFrom(value.getClass())) {
			
			LOG.trace("Value is a child UIBean<{}> ...", ((UIBean<?>) value).getType().getName());
			
			final var layout = new HorizontalLayout();
			layout.setCaption(fieldName);
			
			LOG.trace("Adding dropdown for all available types: {}", parentBean.getFieldAvailableTypes(fieldName));
			
			final var typeDropdown = new NativeSelect<>("type", parentBean.getFieldAvailableTypes(fieldName));
			typeDropdown.setItemCaptionGenerator((c) -> c.getSimpleName());
			typeDropdown.setEmptySelectionAllowed(false);
			typeDropdown.addSelectionListener((se) -> {
				if (se.getSelectedItem().get() != se.getOldValue()) {
					parentBean.setFieldValue(fieldName, new UIBean<>(se.getSelectedItem().get()));
					fireEvent(new ValueChangedEvent(typeDropdown));
				}
			});
			
			layout.addComponent(typeDropdown);
			
			final var childBeanLayout = new FormLayout();
			
			for (String childFieldName : ((UIBean<?>) value).getFieldNames()) {
				LOG.trace("Wrapping UIBean<{}>.{} ...", ((UIBean<?>) value).getType().getName(), childFieldName);
				
				final var wrappedField = wrapValue((UIBean<?>) value, childFieldName,
						((UIBean<?>) value).getFieldValue(childFieldName));
				wrappedField.addListener((e) -> {
					if (e instanceof ValueChangedEvent)
						fireEvent(new ValueChangedEvent(wrappedField));
				});
				childBeanLayout.addComponent(wrappedField);
			}
			
			layout.addComponent(childBeanLayout);
			
			LOG.trace("Done wrapping {}.{} ...", parentBean.getType().getName(), fieldName);
			
			return layout;
			
		} else {
			LOG.trace("Value is wrappable by a TextField ...");
			
			final var component = new TextField(fieldName);
			component.setValue(conversion.convert(value, String.class));
			component.addValueChangeListener((e) -> {
				parentBean.setFieldValue(fieldName,
						conversion.convert(e.getValue(), parentBean.getFieldType(fieldName)));
				fireEvent(new ValueChangedEvent(component));
			});
			
			LOG.trace("Done wrapping {}.{} ...", parentBean.getType().getName(), fieldName);
			
			return component;
		}
		
	}
	
	public static class ValueChangedEvent extends Event {
		
		private static final long serialVersionUID = -8497980777373170645L;
		
		public ValueChangedEvent(Component source) {
			
			super(source);
		}
		
	}
	
}
