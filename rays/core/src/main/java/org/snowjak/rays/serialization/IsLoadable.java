package org.snowjak.rays.serialization;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

/**
 * Indicates that a type can be deserialized from JSON (using the
 * <a href="https://github.com/google/gson">GSON</a> library).
 * 
 * @author snowjak88
 *
 * @param <T>
 */
public interface IsLoadable<T> extends JsonDeserializer<T>, JsonSerializer<T> {
	
}
