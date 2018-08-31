package org.snowjak.rays.annotations.bean;

import java.util.Collection;
import java.util.LinkedList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;

public class CollectionNode implements Node {
	
	private final Class<?> collectedType;
	private final Collection<Node> values = new LinkedList<>();
	private final String stringDefaultValue;
	
	public CollectionNode(Class<?> collectedType, String defaultValue) {
		
		this.collectedType = collectedType;
		this.stringDefaultValue = defaultValue;
	}
	
	@Override
	public Class<?> getType() {
		
		return collectedType;
	}
	
	@Override
	public Node getDefaultValue() {
		
		if (Nodes.isLiteralType(collectedType))
			return new LiteralNode(LiteralNode.getAcceptableTypes(collectedType).iterator().next(), stringDefaultValue);
		
		if (Nodes.isBeanType(collectedType))
			return new BeanNode(Nodes.getAnnotatedTypes(collectedType).iterator().next());
		
		throw new UnsupportedClassException(
				"Cannot get default-value for collected-type [" + collectedType.getSimpleName() + "].");
	}
	
	public Collection<Node> getValues() {
		
		return values;
	}
	
	public void addValue(Node node) {
		
		if (!collectedType.isAssignableFrom(node.getType()))
			throw new UnsupportedClassException("Cannot insert a [" + node.getType().getSimpleName()
					+ "] into a Collection<" + getType().getSimpleName() + ">.");
		
		values.add(node);
	}
	
	public void removeValue(Node node) {
		
		values.remove(node);
	}
	
	@Override
	public JsonElement serialize(JsonSerializationContext context) {
		
		final var array = new JsonArray();
		
		values.stream().map(v -> v.serialize(context)).forEach(e -> array.add(e));
		
		return array;
	}
	
}
