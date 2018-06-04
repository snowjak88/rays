package org.snowjak.rays;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;

import com.google.common.math.DoubleMath;

/**
 * Represents application-wide defaults. Typically these will be given by
 * properties specified in "<code>core-settings.properties</code>".
 * 
 * @author snowjak88
 *
 */
public class Settings {
	
	/**
	 * @see #getDoubleEqualityEpsilon()
	 */
	private static double DOUBLE_EQUALITY_EPSILON = 1e-8;
	
	/**
	 * A shared {@link Random} instance.
	 */
	public static final Random RND = new Random(System.currentTimeMillis());
	
	//
	//
	// Try to load the core-settings properties file.
	{
		try (var settingsStream = this.getClass().getClassLoader().getResourceAsStream("./core-settings.properties")) {
			
			var coreSettings = new Properties();
			coreSettings.load(settingsStream);
			
			//
			//
			// Now that we've loaded the core-settings into a Properties object,
			// we can start initializing those fields we care about.
			DOUBLE_EQUALITY_EPSILON = Double.parseDouble(coreSettings.getProperty(
					"org.snowjak.rays.math.double-equality-epsilon", Double.toString(getDoubleEqualityEpsilon())));
			
		} catch (IOException e) {
			throw new CannotLoadSettingsException("Cannot load core settings!", e);
		}
	}
	
	/**
	 * Two <code>double</code> values are considered to be "basically equal" if they
	 * are closer than this value.
	 * <p>
	 * Mapped to <code>org.snowjak.rays.math.double-equality-epsilon</code>
	 * </p>
	 * 
	 * @see #nearlyEqual(double, double)
	 */
	public static double getDoubleEqualityEpsilon() {
		
		return DOUBLE_EQUALITY_EPSILON;
	}
	
	/**
	 * Determines whether two <code>double</code>s are "nearly equal".
	 * 
	 * @param d1
	 *            a <code>double</code> value
	 * @param d2
	 *            another <code>double</code> value
	 * @return <code>true</code> if these two values are within
	 *         {@link #DOUBLE_EQUALITY_EPSILON} of each other
	 * @see #DOUBLE_EQUALITY_EPSILON
	 */
	public static boolean nearlyEqual(double d1, double d2) {
		
		return DoubleMath.fuzzyEquals(d1, d2, getDoubleEqualityEpsilon());
	}
	
	public static class CannotLoadSettingsException extends RuntimeException {
		
		private static final long serialVersionUID = 1636232835175064483L;
		
		public CannotLoadSettingsException() {
			
			super();
		}
		
		public CannotLoadSettingsException(String message, Throwable cause) {
			
			super(message, cause);
		}
		
		public CannotLoadSettingsException(String message) {
			
			super(message);
		}
		
		public CannotLoadSettingsException(Throwable cause) {
			
			super(cause);
		}
		
	}
}
