package org.snowjak.rays.annotations;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Collection;

@Documented
@Retention(RUNTIME)
@Target(ANNOTATION_TYPE)
public @interface UIField {
	
	/**
	 * What is the field named?
	 */
	public String name();
	
	/**
	 * What is the field's type?
	 */
	public Class<?> type();
	
	/**
	 * If {@link #type()} is a subtype of {@link Collection}, this indicates which
	 * type is stored in that Collection.
	 */
	public Class<?> collectedType() default Void.class;
	
	/**
	 * What is the field's default-value?
	 */
	public String defaultValue() default "";
}
