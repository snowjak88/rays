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
	 * Calculate the CIE XYZ tristimulus triplet derived from the given spectral
	 * power-distribution (given as a mapping of wavelengths (nm) to spectral
	 * radiance (W/sr*m^2)).
	 * 
	 * @param spd
	 * @return
	 */
	public static XYZ fromSpectrum(SpectralPowerDistribution spd) {
		
		final var cmf = Settings.getInstance().getColorMappingFunctions();
		
		final double lowLambda = spd.getBounds().get().getFirst();
		final double highLambda = spd.getBounds().get().getSecond();
		
		final var numerator = Util.integrateTriplet(lowLambda, highLambda,
				Settings.getInstance().getCieXyzIntegrationStepCount(),
				(lambda) -> cmf.get(lambda).multiply(spd.get(lambda).get(0)));
		final var denominator = Util.integrate(lowLambda, highLambda,
				Settings.getInstance().getCieXyzIntegrationStepCount(), (lambda) -> cmf.get(lambda).get(1));
		final var result = new XYZ(numerator.divide(denominator));
		
		return new XYZ(result.get());
		
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
		
		return new XYZ(Settings.getInstance().getColorMappingFunctions().get(wavelength));
		
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
	public XYZ clamp() {
		
		return new XYZ(get().clamp(0d, 1d));
	}
	
	/**
	 * Normalize this XYZ triplet to unity brightness -- i.e., Y == 1.0
	 * 
	 * @return
	 */
	public XYZ normalize() {
		
		return new XYZ(this.get().apply(c -> c / this.getY()));
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
