package org.snowjak.rays.spectrum.colorspace;

import static org.apache.commons.math3.util.FastMath.pow;

import org.snowjak.rays.Settings;
import org.snowjak.rays.Util;
import org.snowjak.rays.geometry.util.Matrix;
import org.snowjak.rays.geometry.util.Triplet;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;

/**
 * Represents the CIE 1931 XYZ tristimulus colorspace.
 * 
 * @author snowjak88
 *
 */
public class XYZ extends Colorspace<XYZ, Triplet> {
	
	//@formatter:off
	private static final Matrix __CONVERSION_TO_RGB =
			new Matrix(new double[][] {
				{ 3.2406d,-1.5372d,-0.4986d, 0d },
				{-0.9689d, 1.8758d, 0.0415d, 0d },
				{ 0.0557d,-0.2040d, 1.0570d, 0d },
				{ 0d,      0d,      0d,      0d }
			});
	//@formatter:on
	
	/**
	 * Calculate the CIE XYZ tristimulus triplet derived from the given spectrum
	 * power-distribution (given as a map of wavelengths (nm) to spectral radiance
	 * (W/sr*m^2)).
	 * 
	 * @param spectrum
	 * @return
	 */
	public static XYZ fromSpectrum(SpectralPowerDistribution spectrum) {
		
		final var cmf = Settings.getInstance().getColorMappingFunctionDistribution();
		
		final double cmfLowLambda = cmf.getLowestWavelength();
		final double cmfHighLambda = cmf.getHighestWavelength();
		
		final var numerator = Util.integrateTriplet(cmfLowLambda, cmfHighLambda,
				Settings.getInstance().getCieXyzIntegrationStepCount(),
				(lambda) -> cmf.getCMF(lambda).multiply(spectrum.get(lambda)));
		final var denominator = Util.integrate(cmfLowLambda, cmfHighLambda,
				Settings.getInstance().getCieXyzIntegrationStepCount(), (lambda) -> cmf.getCMF(lambda).get(1));
		final var result = new XYZ(numerator.divide(denominator));
		
		return new XYZ(result.get().apply(c -> c / result.getY()));
		
	}
	
	/**
	 * Calculate the CIE XYZ tristimulus triplet associated with the given
	 * wavelength (assumed to be expressed in nanometers).
	 * 
	 * @param wavelength
	 *            a given wavelength (given in nanometers)
	 * @return a CIE-XYZ tristimulus triplet representing the color-mapping
	 *         functions for the given wavelength
	 */
	public static XYZ fromWavelength(double wavelength) {
		
		return new XYZ(Settings.getInstance().getColorMappingFunctionDistribution().getCMF(wavelength));
		
	}
	
	public XYZ(double x, double y, double z) {
		
		this(new Triplet(x, y, z));
	}
	
	public XYZ(Triplet representation) {
		
		super(representation);
	}
	
	public double getX() {
		
		return get().get(0);
	}
	
	public double getY() {
		
		return get().get(1);
	}
	
	public double getZ() {
		
		return get().get(2);
	}
	
	@Override
	protected void registerConverters(ColorspaceConverterRegistry<XYZ> registry) {
		
		registry.register(XYZ.class, (xyz) -> xyz);
		
		registry.register(RGB.class, (xyz) -> new RGB(__CONVERSION_TO_RGB.multiply(xyz.get(), 0d)
				.apply(c -> (c <= 0.0031308d) ? (12.92d * c) : (1.055d * pow(c, 1d / 2.4d)) - 0.055d)));
	}
	
	@Override
	public String toString() {
		
		return "XYZ [X=" + getX() + ", Y=" + getY() + ", Z=" + getZ() + "]";
	}
	
}
