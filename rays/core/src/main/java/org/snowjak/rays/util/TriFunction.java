package org.snowjak.rays.util;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Extension of {@link Function} (1 argument) and {@link BiFunction} (2
 * arguments) to 3 arguments.
 * 
 * @author snowjak88
 *
 * @param <U>
 * @param <V>
 * @param <W>
 * @param <R>
 */
@FunctionalInterface
public interface TriFunction<U, V, W, R> {
	
	public R apply(U u, V v, W w);
	
}
