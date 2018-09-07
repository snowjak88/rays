package org.snowjak.rays.frontend.ui.components;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.Settings;
import org.snowjak.rays.annotations.bean.BeanNode;
import org.snowjak.rays.annotations.bean.CollectionNode;
import org.snowjak.rays.annotations.bean.LiteralNode;
import org.snowjak.rays.annotations.bean.Node;
import org.snowjak.rays.annotations.bean.Nodes;
import org.snowjak.rays.frontend.messages.backend.commands.AbstractChainableCommand;
import org.snowjak.rays.frontend.messages.backend.commands.RequestMultipleRenderTaskSubmission;
import org.snowjak.rays.frontend.messages.backend.commands.RequestRenderCreationFromSingleJson;
import org.snowjak.rays.frontend.messages.backend.commands.RequestRenderDecomposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;

import com.google.common.eventbus.EventBus;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@SpringComponent
@UIScope
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
	
	@Autowired
	private EventBus bus;
	
	private BeanNode bean;
	
	private String json = "";
	private TextArea jsonOutput;
	
	public ObjectCreator() {
		
		super();
	}
	
	public void setClass(Class<?> clazz) {
		
		bean = new BeanNode(clazz);
		
		refreshForm();
	}
	
	private void refreshForm() {
		
		removeAllComponents();
		
		final var rootComponent = wrap(bean);
		
		addListener((e) -> {
			if (e instanceof ValueChangedEvent) {
				
				LOG.info("Received ValueChangedEvent in root handler. Refreshing JSON.");
				refreshJson();
				
			}
		});
		addComponent(rootComponent);
		
		json = Settings.getInstance().getGson().toJson(bean);
		
		jsonOutput = new TextArea();
		jsonOutput.setWidth(100, Unit.PERCENTAGE);
		jsonOutput.setHeightUndefined();
		jsonOutput.setCaption("JSON");
		jsonOutput.setValue(json);
		addComponent(jsonOutput);
		
		final var submitButton = new Button("Submit");
		submitButton.addClickListener((ce) -> {
			
			bus.post(AbstractChainableCommand.chain(new RequestRenderCreationFromSingleJson(jsonOutput.getValue()),
					new RequestRenderDecomposition(128), new RequestMultipleRenderTaskSubmission()));
			
		});
		addComponent(submitButton);
	}
	
	private void refreshJson() {
		
		json = Settings.getInstance().getGson().toJson(bean);
		jsonOutput.setValue(json);
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
			fireEvent(new ValueChangedEvent(layout));
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
	
	public static class ValueChangedEvent extends Event {
		
		private static final long serialVersionUID = -8497980777373170645L;
		
		public ValueChangedEvent(Component source) {
			
			super(source);
		}
		
	}
	
}
