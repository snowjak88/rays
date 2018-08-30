package org.snowjak.rays.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Defines how this type will be represented in the UI's auto-generated forms.
 * <p>
 * This UIType is then used to generate a JSON object, which is in turn used to
 * initialize an instance of this type. Therefore, you must ensure that the
 * UIType definition follows this type's expected JSON structure, which is not
 * necessarily the same as this type's internal structure.
 * </p>
 * 
 * @author snowjak88
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
@Documented
public @interface UIType {
	
	/**
	 * If the JSON deserializer requires a "type" value, supply it here.
	 */
	public String type() default "";
	
	public UIField[] fields();
}
