package org.snowjak.rays.annotations.bean;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.annotations.UIType;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

public class BeanNode implements Node {
	
	private final String jsonType;
	private final Class<?> type;
	private final Map<String, Node> fields;
	
	public BeanNode(Class<?> type) {
		
		this.type = type;
		this.fields = new HashMap<>();
		
		final var typeDef = type.getAnnotation(UIType.class);
		jsonType = typeDef.type();
		
		for (UIField fieldDef : typeDef.fields()) {
			
			final Node fieldNode;
			
			if (Nodes.isLiteralType(fieldDef.type()))
				fieldNode = new LiteralNode(fieldDef.type(), fieldDef.defaultValue());
			
			else if (Nodes.isCollectionType(fieldDef.type()))
				fieldNode = new CollectionNode(fieldDef.collectedType(), fieldDef.defaultValue());
			
			else if (Nodes.isBeanType(fieldDef.type()))
				fieldNode = new BeanNode(fieldDef.type());
			
			else
				throw new UnsupportedClassException(
						"Cannot determine how to handle UIField definition " + type.getName() + "." + fieldDef.name()
								+ " -- neither recognized literal, collection, nor UIType-annotated.");
			
			fields.put(fieldDef.name(), fieldNode);
			
		}
	}
	
	@Override
	public Class<?> getType() {
		
		return type;
	}
	
	@Override
	public Object getDefaultValue() {
		
		return new BeanNode(getType());
	}
	
	public Collection<String> getFieldNames() {
		
		return fields.keySet();
	}
	
	public Node getField(String name) {
		
		return fields.get(name);
	}
	
	@Override
	public JsonElement serialize(JsonSerializationContext context) {
		
		final var obj = new JsonObject();
		
		if (jsonType != null && !jsonType.trim().isEmpty())
			obj.addProperty("type", jsonType);
		
		for (String name : getFieldNames())
			obj.add(name, getField(name).serialize(context));
		
		return obj;
	}
	
}
