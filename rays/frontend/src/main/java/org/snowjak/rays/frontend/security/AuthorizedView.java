package org.snowjak.rays.frontend.security;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Documented
@Retention(RUNTIME)
@Target(TYPE)
@Repeatable(AuthorizedViews.class)
public @interface AuthorizedView {
	
	/**
	 * Alias for {@link #authorities()}
	 */
	String[] value() default {};
	
	/**
	 * Which roles a principal must have to access the annotated View class. A
	 * principal must have <em>ALL</em> of the given authorities to be qualified to
	 * access this View.
	 * <p>
	 * Your authority-names can be regular-expressions. (see
	 * {@link SecurityOperations#hasAuthority(String)})
	 * </p>
	 */
	String[] authorities() default {};
}
