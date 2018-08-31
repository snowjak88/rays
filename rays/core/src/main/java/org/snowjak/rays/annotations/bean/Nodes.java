package org.snowjak.rays.annotations.bean;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.snowjak.rays.annotations.UIType;
import org.snowjak.rays.annotations.bean.Node.UnsupportedClassException;

import io.github.classgraph.ClassGraph;

public class Nodes {
	
	/**
	 * Determines if a type should be represented by a {@link LiteralNode}.
	 * 
	 * @param type
	 * @return
	 */
	public static boolean isLiteralType(Class<?> type) {
		
		return LiteralNode.CONVERTERS.keySet().stream().anyMatch(c -> c.isAssignableFrom(type));
	}
	
	/**
	 * Determines if a type should be represented by a {@link CollectionNode}.
	 * 
	 * @param type
	 * @return
	 */
	public static boolean isCollectionType(Class<?> type) {
		
		return Collection.class.isAssignableFrom(type);
	}
	
	/**
	 * Determines if a type should be represented by a {@code BeanNode}.
	 * 
	 * @param type
	 * @return
	 */
	public static boolean isBeanType(Class<?> type) {
		
		return getAnnotatedTypes(type).contains(type);
	}
	
	/**
	 * Given a type, find all types (including the given type and its subtypes)
	 * which are "acceptable" -- whether as literals or beans.
	 * 
	 * @param type
	 * @return
	 */
	public static Collection<Class<?>> getAcceptableTypes(Class<?> type) {
		
		if (isLiteralType(type))
			return LiteralNode.getAcceptableTypes(type);
		
		if (isBeanType(type))
			return getAnnotatedTypes(type);
		
		throw new UnsupportedClassException("Cannot derive a list of acceptable types from [" + type.getName() + "].");
	}
	
	/**
	 * Given a supertype, find all types (potentially including the given
	 * {@code supertype}) that are annotated with {@link UIType}.
	 * 
	 * @param supertype
	 * @return
	 */
	public static Collection<Class<?>> getAnnotatedTypes(Class<?> supertype) {
		
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
	
}
