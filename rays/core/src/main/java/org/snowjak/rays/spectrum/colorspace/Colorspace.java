package org.snowjak.rays.spectrum.colorspace;

import java.util.HashMap;
import java.util.Map;

import org.snowjak.rays.geometry.util.AbstractVector;

/**
 * Base-class for all color-space implementations.
 * 
 * @author snowjak88
 *
 * @param <T>
 */
public abstract class Colorspace<C extends Colorspace<C, T>, T extends AbstractVector<T>> {
	
	private T representation;
	private transient Map<Class<? extends Colorspace<?, ?>>, Converter<C, ?>> converters;
	
	/**
	 * Create a new Colorspace with the given representation.
	 * 
	 * @param representation
	 */
	public Colorspace(T representation) {
		
		this.representation = representation;
		this.converters = new HashMap<>();
		registerConverters(converters::put);
	}
	
	/**
	 * This method is called by the base constructor, and allows a Colorspace
	 * implementation to register its converters.
	 * 
	 * @param registry
	 */
	protected abstract void registerConverters(ColorspaceConverterRegistry<C> registry);
	
	/**
	 * Return the representation backing this Colorspace object.
	 * 
	 * @return
	 */
	public T get() {
		
		return representation;
	}
	
	/**
	 * Clamp each of this Colorspace's components to lie in <code>[0,1]</code>.
	 */
	public abstract C clamp();
	
	/**
	 * Convert this colorspace implementation to another colorspace.
	 * 
	 * @param colorspaceClass
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <D extends Colorspace<D, ?>> D to(Class<D> colorspaceClass) throws NoSuchConverterException {
		
		return (D) converters.get(converters.keySet().stream().filter(clazz -> clazz.isAssignableFrom(colorspaceClass))
				.findFirst().orElseThrow(() -> new NoSuchConverterException("Cannot find suitable converter from "
						+ this.getClass().getName() + " to " + colorspaceClass.getName())))
				.convert((C) this);
	}
	
	/**
	 * Base class for all colorspace converters.
	 * 
	 * @author snowjak88
	 *
	 * @param <S>
	 *            source-type this Converter can convert <em>from</em>
	 * @param <D>
	 *            destination-type this Converter can convert <em>to</em>
	 */
	@FunctionalInterface
	public interface Converter<S extends Colorspace<S, ?>, D extends Colorspace<D, ?>> {
		
		/**
		 * Convert one colorspace to another.
		 * 
		 * @param colorspace
		 * @return
		 */
		public D convert(S colorspace);
	}
	
	@FunctionalInterface
	public interface ColorspaceConverterRegistry<C extends Colorspace<C, ?>> {
		
		public <D extends Colorspace<D, ?>> void register(Class<D> implementationClass, Converter<C, D> converter);
	}
	
	/**
	 * Exception denoting that a colorspace conversion is requested that is not
	 * supported.
	 * 
	 * @author snowjak88
	 *
	 */
	public static class NoSuchConverterException extends RuntimeException {
		
		private static final long serialVersionUID = 1279760251357640546L;
		
		public NoSuchConverterException(String message, Throwable cause) {
			
			super(message, cause);
		}
		
		public NoSuchConverterException(String message) {
			
			super(message);
		}
	}
}
