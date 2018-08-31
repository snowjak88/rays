package org.snowjak.rays.frontend.ui.components;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.Settings;
import org.snowjak.rays.annotations.UIBean;
import org.snowjak.rays.annotations.bean.BeanNode;
import org.snowjak.rays.annotations.bean.CollectionNode;
import org.snowjak.rays.annotations.bean.LiteralNode;
import org.snowjak.rays.annotations.bean.Node;
import org.snowjak.rays.annotations.bean.Nodes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.ValueChangeMode;
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
	
	private BeanNode bean;
	
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
		
		bean = new BeanNode(clazz);
		
		refreshForm();
	}
	
	private void refreshForm() {
		
		removeAllComponents();
		
		addComponent(wrap(bean));
		
		jsonOutput = new TextArea();
		jsonOutput.setWidth(100, Unit.PERCENTAGE);
		jsonOutput.setHeightUndefined();
		jsonOutput.setCaption("JSON");
		jsonOutput.setReadOnly(true);
		jsonOutput.setValue(Settings.getInstance().getGson().toJson(bean));
		addComponent(jsonOutput);
	}
	
	private Component wrap(Node node) {
		
		if (node instanceof LiteralNode)
			return wrap((LiteralNode) node);
		else if (node instanceof CollectionNode)
			return wrap((CollectionNode) node);
		else
			return wrap((BeanNode) node);
	}
	
	private Component wrap(LiteralNode node) {
		
		LOG.trace("Wrapping LiteralNode <{}> ...", node.getType().getSimpleName());
		
		final var layout = new HorizontalLayout();
		
		final var field = new TextField();
		field.setValueChangeMode(ValueChangeMode.BLUR);
		field.setValue(conversion.convert(node.getValue(), String.class));
		field.addValueChangeListener((vce) -> {
			node.setValue(conversion.convert(vce.getValue(), node.getType()));
			fireEvent(new ValueChangedEvent(field));
		});
		
		final var acceptableTypes = Nodes.getAcceptableTypes(node.getDeclaredType());
		
		if (acceptableTypes.size() > 1) {
			final var typeDropdown = new NativeSelect<Class<?>>();
			typeDropdown.setItems(acceptableTypes);
			typeDropdown.setItemCaptionGenerator(Class::getSimpleName);
			typeDropdown.setSelectedItem(node.getType());
			typeDropdown.setEnabled((acceptableTypes.size() > 1));
			typeDropdown.addValueChangeListener((vce) -> {
				node.setType(vce.getValue());
				field.setValue(conversion.convert(node.getValue(), String.class));
			});
			
			layout.addComponent(typeDropdown);
		}
		
		layout.addComponent(field);
		
		LOG.trace("Done wrapping LiteralNode <{}>.", node.getType().getSimpleName());
		
		return layout;
		
	}
	
	private Component wrap(CollectionNode collectionNode) {
		
		LOG.trace("Wrapping CollectionNode <{}> ...", collectionNode.getType().getSimpleName());
		
		final var layout = new VerticalLayout();
		
		final Consumer<Node> nodeLayoutMaker = (n) -> {
			final var nodeLayout = new HorizontalLayout();
			final var removeButton = new Button(VaadinIcons.MINUS_CIRCLE_O);
			removeButton.addClickListener((ce) -> {
				collectionNode.removeValue(n);
				layout.removeComponent(nodeLayout);
				fireEvent(new ValueChangedEvent(nodeLayout));
			});
			nodeLayout.addComponent(removeButton);
			nodeLayout.addComponent(wrap(n));
			layout.addComponent(nodeLayout);
		};
		
		for (Node node : collectionNode.getValues())
			nodeLayoutMaker.accept(node);
		
		final var addButton = new Button(collectionNode.getType().getSimpleName(), VaadinIcons.PLUS_CIRCLE_O);
		addButton.addClickListener((ce) -> {
			final var newNode = collectionNode.getDefaultValue();
			collectionNode.addValue(newNode);
			layout.removeComponent(addButton);
			nodeLayoutMaker.accept(newNode);
			layout.addComponent(addButton);
		});
		
		layout.addComponent(addButton);
		
		LOG.trace("Done wrapping CollectionNode <{}>.", collectionNode.getType().getSimpleName());
		
		return layout;
	}
	
	private Component wrap(BeanNode node) {
		
		LOG.trace("Wrapping BeanNode <{}> ...", node.getType().getSimpleName());
		final var layout = new FormLayout();
		
		final List<Component> components = new LinkedList<>();
		final Consumer<BeanNode> componentsRefresh = (n) -> n.getFieldNames().stream().map(fn -> {
			final var c = wrap(node.getField(fn));
			c.setCaption(fn);
			return c;
		}).forEach(c -> components.add(c));
		componentsRefresh.accept(node);
		
		final var acceptableTypes = Nodes.getAcceptableTypes(node.getDeclaredType());
		if (acceptableTypes.size() > 1) {
			final var typeDropdown = new NativeSelect<Class<?>>();
			typeDropdown.setItems(acceptableTypes);
			typeDropdown.setItemCaptionGenerator(Class::getSimpleName);
			typeDropdown.setSelectedItem(node.getType());
			typeDropdown.setEnabled((acceptableTypes.size() > 1));
			typeDropdown.addValueChangeListener((vce) -> {
				node.setType(vce.getValue());
				components.forEach(c -> layout.removeComponent(c));
				components.clear();
				componentsRefresh.accept(node);
				components.forEach(c -> layout.addComponent(c));
			});
			
			layout.addComponent(typeDropdown);
		}
		
		components.forEach(c -> layout.addComponent(c));
		
		LOG.trace("Done wrapping BeanNode <{}>.", node.getType().getSimpleName());
		return layout;
	}
	
	private Component wrapBean(UIBean<?> bean) {
		
		LOG.trace("Wrapping UIBean<{}> ...", bean.getType().getSimpleName());
		final var layout = new FormLayout();
		
		for (String fieldName : bean.getFieldNames()) {
			LOG.trace("Wrapping UIBean<{}>.{} ...", bean.getType().getSimpleName(), fieldName);
			
			final var wrappedValue = wrapValue(bean, fieldName, bean.getFieldValue(fieldName));
			wrappedValue.addListener((e) -> {
				if (e instanceof ValueChangedEvent) {
					fireEvent(new ValueChangedEvent(wrappedValue));
				}
			});
			layout.addComponent(wrappedValue);
		}
		
		LOG.trace("Done wrapping UIBean<{}>.", bean.getType().getSimpleName());
		
		return layout;
	}
	
	private Component wrapValue(UIBean<?> parentBean, String fieldName, Object value) {
		
		LOG.trace("Wrapping value: {}.{} ...", parentBean.getType().getName(), fieldName);
		
		if (Collection.class.isAssignableFrom(value.getClass())) {
			
			LOG.trace("Value is a Collection<{}> ...", parentBean.getFieldCollectedType(fieldName).getName());
			
			final var layout = new VerticalLayout();
			layout.setCaption(fieldName);
			
			int i = 1;
			for (var item : (Collection<?>) value) {
				
				LOG.trace("Value {}[{}] ({}) ...", fieldName, i, item.getClass().getName());
				
				final var itemLayout = new HorizontalLayout();
				final var removeButton = new Button(fieldName, VaadinIcons.MINUS_CIRCLE);
				itemLayout.addComponent(removeButton);
				removeButton.addClickListener((ce) -> {
					((Collection<?>) value).remove(item);
					layout.removeComponent(itemLayout);
				});
				
				final var wrappedValue = wrapValue(parentBean, fieldName, item);
				itemLayout.addComponent(wrappedValue);
				
				layout.addComponent(itemLayout);
				i++;
			}
			
			final Button addButton = new Button(fieldName, VaadinIcons.PLUS_CIRCLE);
			addButton.addClickListener((ce) -> {
				final var itemLayout = new HorizontalLayout();
				
				final var newItem = parentBean.addToCollection(fieldName);
				
				final var removeButton = new Button(fieldName, VaadinIcons.MINUS_CIRCLE);
				itemLayout.addComponent(removeButton);
				removeButton.addClickListener((rce) -> {
					((Collection<?>) value).remove(newItem);
					layout.removeComponent(itemLayout);
				});
				
				final var wrappedValue = wrapValue(parentBean, fieldName, newItem);
				
				itemLayout.addComponent(wrappedValue);
				layout.addComponent(itemLayout);
				
				layout.removeComponent(addButton);
				layout.addComponent(addButton);
			});
			layout.addComponent(addButton);
			
			LOG.trace("Done wrapping {}.{} ...", parentBean.getType().getName(), fieldName);
			
			return layout;
			
		} else if (UIBean.class.isAssignableFrom(value.getClass())) {
			
			final var childBean = (UIBean<?>) value;
			LOG.trace("Value is a child UIBean<{}> ...", childBean.getType().getName());
			
			final var layout = new HorizontalLayout();
			
			LOG.trace("Adding dropdown for all available types: {}", parentBean.getFieldAvailableTypes(fieldName));
			
			final var typeDropdown = new NativeSelect<>("type", parentBean.getFieldAvailableTypes(fieldName));
			typeDropdown.setSelectedItem(parentBean.getFieldType(fieldName));
			typeDropdown.setItemCaptionGenerator((c) -> c.getSimpleName());
			typeDropdown.setEmptySelectionAllowed(false);
			typeDropdown.addSelectionListener((se) -> {
				if (se.getSelectedItem().get() != se.getOldValue()) {
					parentBean.setFieldValue(fieldName, new UIBean<>(se.getSelectedItem().get()));
					
					layout.removeAllComponents();
					layout.addComponent(typeDropdown);
					final var wrappedBean = wrapBean((UIBean<?>) parentBean.getFieldValue(fieldName));
					layout.addComponent(wrappedBean);
					
					fireEvent(new ValueChangedEvent(typeDropdown));
				}
			});
			
			layout.addComponent(typeDropdown);
			
			layout.addComponent(wrapBean(childBean));
			
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
