package org.snowjak.rays;

import java.util.Properties;
import java.util.Random;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.spectrum.CIEXYZ;
import org.snowjak.rays.spectrum.distribution.AnalyticColorMappingFunctionDistribution;
import org.snowjak.rays.spectrum.distribution.ColorMappingFunctionDistribution;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;
import org.snowjak.rays.spectrum.distribution.TabulatedColorMappingFunctionDistribution;
import org.snowjak.rays.spectrum.distribution.TabulatedSpectralPowerDistribution;

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
	private double doubleEqualityEpsilon = 1e-8;
	
	/**
	 * @see #getColorMappingFunctionDistribution()
	 */
	private ColorMappingFunctionDistribution colorMappingFunctionDistribution = new AnalyticColorMappingFunctionDistribution();
	
	/**
	 * @see #getIlluminatorSpectralPowerDistribution()
	 */
	private SpectralPowerDistribution illuminatorSpectralPowerDistribution = null;
	
	/**
	 * @see #getCieXyzIntegrationStepSize()
	 */
	private double cieXyzIntegrationStepSize = 5.0;
	
	/**
	 * A shared {@link Random} instance.
	 */
	public static final Random RND = new Random(System.currentTimeMillis());
	
	private static Settings __INSTANCE = null;
	
	public static Settings getInstance() {
		
		if (__INSTANCE == null)
			__INSTANCE = new Settings();
		
		return __INSTANCE;
	}
	
	private Settings() {
		//
		//
		// Try to load the core-settings properties file.
		
		try (var settingsStream = this.getClass().getClassLoader().getResourceAsStream("./core-settings.properties")) {
			
			var coreSettings = new Properties();
			coreSettings.load(settingsStream);
			System.getProperties().putAll(coreSettings);
			
			//
			//
			// Now that we've loaded the core-settings into a Properties object,
			// we can start initializing those fields we care about.
			doubleEqualityEpsilon = Double.parseDouble(coreSettings.getProperty(
					"org.snowjak.rays.math.double-equality-epsilon", Double.toString(getDoubleEqualityEpsilon())));
			
			colorMappingFunctionDistribution = (coreSettings
					.containsKey("org.snowjak.rays.cie-csv-xyz-color-mapping-path"))
							? TabulatedColorMappingFunctionDistribution.loadFromCSV(
									TabulatedColorMappingFunctionDistribution::new,
									Settings.class.getClassLoader()
											.getResourceAsStream(coreSettings
													.getProperty("org.snowjak.rays.cie-csv-xyz-color-mapping-path")))
							: getColorMappingFunctionDistribution();
			
			illuminatorSpectralPowerDistribution = (coreSettings
					.containsKey("org.snowjak.rays.cie-csv-xyz-d65-standard-illuminator-path"))
							? TabulatedSpectralPowerDistribution.loadFromCSV(TabulatedSpectralPowerDistribution::new,
									Settings.class.getClassLoader()
											.getResourceAsStream(coreSettings.getProperty(
													"org.snowjak.rays.cie-csv-xyz-d65-standard-illuminator-path")))
							: getIlluminatorSpectralPowerDistribution();
			
			cieXyzIntegrationStepSize = Double.parseDouble(coreSettings.getProperty(
					"org.snowjak.rays.cie-xyz-integration-step-size", Double.toString(getCieXyzIntegrationStepSize())));
			
		} catch (Throwable t) {
			throw new CannotLoadSettingsException("Cannot load core settings!", t);
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
	public double getDoubleEqualityEpsilon() {
		
		return doubleEqualityEpsilon;
	}
	
	/**
	 * The distribution of color-mapping-functions (for calculating {@link CIEXYZ}
	 * triplets from spectra).
	 */
	public ColorMappingFunctionDistribution getColorMappingFunctionDistribution() {
		
		return colorMappingFunctionDistribution;
	}
	
	/**
	 * The spectral power-distribution associated with the standard illuminator.
	 * (Used to calculate {@link CIEXYZ} triplets from spectra.)
	 */
	public SpectralPowerDistribution getIlluminatorSpectralPowerDistribution() {
		
		return illuminatorSpectralPowerDistribution;
	}
	
	/**
	 * When calculating a CIE XYZ triplet from a {@link SpectralPowerDistribution},
	 * we need to compute a definite integral across a range of wavelengths. What
	 * step-size (nm) should we use when calculating that definite integral?
	 */
	public double getCieXyzIntegrationStepSize() {
		
		return cieXyzIntegrationStepSize;
	}
	
	/**
	 * Given {@link #getCieXyzIntegrationStepSize()}, how many total steps will each
	 * integration consist of?
	 */
	public int getCieXyzIntegrationStepCount() {
		
		return (int) FastMath.ceil((830.0 - 360.0) / getCieXyzIntegrationStepSize());
		
	}
	
	/**
	 * Determines whether two <code>double</code>s are "nearly equal".
	 * 
	 * @param d1
	 *            a <code>double</code> value
	 * @param d2
	 *            another <code>double</code> value
	 * @return <code>true</code> if these two values are within
	 *         {@link #doubleEqualityEpsilon} of each other
	 * @see #doubleEqualityEpsilon
	 */
	public boolean nearlyEqual(double d1, double d2) {
		
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
