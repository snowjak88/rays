package org.snowjak.rays.spectrum;

import static org.apache.commons.math3.util.FastMath.pow;

import java.io.Serializable;

import org.snowjak.rays.Settings;
import org.snowjak.rays.Util;
import org.snowjak.rays.geometry.util.Matrix;
import org.snowjak.rays.geometry.util.Triplet;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;

/**
 * Represents a CIE XYZ tristimulus triplet (i.e., see
 * <a href="https://en.wikipedia.org/wiki/CIE_1931_color_space">the CIE 1931
 * color-space</a>).
 * 
 * <p>
 * The color-matching function data used here is the "2-deg XYZ CMFs transformed
 * from the CIE (2006) 2-deg LMS cone fundamentals" data, obtained from the
 * <a href="http://www.cvrl.org/">CVRL.org</a> database.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class CIEXYZ implements Serializable {
	
	private static final long serialVersionUID = 6070836284173038489L;
	
	//@formatter:off
	private static final Matrix __CONVERSION_TO_RGB =
			new Matrix(new double[][] {
				{ 3.2406d,-1.5372d,-0.4986d, 0d },
				{-0.9689d, 1.8758d, 0.0415d, 0d },
				{ 0.0557d,-0.2040d, 1.0570d, 0d },
				{ 0d,      0d,      0d,      0d }
			});
	//@formatter:on
	
	//@formatter:off
	private static final Matrix __CONVERSION_FROM_RGB =
			new Matrix(new double[][] {
				{ 0.4124d, 0.3576d, 0.1805d, 0d },
				{ 0.2126d, 0.7152d, 0.0722d, 0d },
				{ 0.0193d, 0.1192d, 0.9505d, 0d },
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
	public static CIEXYZ fromSpectrum(SpectralPowerDistribution spectrum) {
		
		final var cmf = Settings.getInstance().getColorMappingFunctionDistribution();
		
		final double cmfLowLambda = cmf.getLowestWavelength();
		final double cmfHighLambda = cmf.getHighestWavelength();
		
		final var result = Util
				.integrateTriplet(cmfLowLambda, cmfHighLambda, Settings.getInstance().getCieXyzIntegrationStepCount(),
						(lambda) -> cmf.getCMF(lambda).multiply(spectrum.get(lambda)))
				.divide(Util.integrate(cmfLowLambda, cmfHighLambda,
						Settings.getInstance().getCieXyzIntegrationStepCount(), (lambda) -> cmf.getCMF(lambda).get(1)));
		
		return new CIEXYZ(result);
		
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
	public static CIEXYZ fromWavelength(double wavelength) {
		
		return new CIEXYZ(Settings.getInstance().getColorMappingFunctionDistribution().getCMF(wavelength));
		
	}
	
	/**
	 * Construct a new CIE XYZ triplet from a {@link RGB} triplet (where the RGB
	 * triplet is assumed to be in the
	 * <a href="https://en.wikipedia.org/wiki/SRGB">sRGB color-space</a>).
	 * <p>
	 * <strong>Note</strong> that each component of the RGB triplet is clamped to
	 * <code>[0,1]</code> as part of conversion.
	 * </p>
	 * 
	 * @param rgb
	 *            an RGB triplet, assumed to lie in the sRGB color-space
	 * @return a CIE XYZ triplet representing the equivalent color in the CIE 1931
	 *         color-space
	 */
	public static CIEXYZ fromRGB(RGB rgb) {
		
		Triplet linear = new Triplet(rgb.getComponents()).clamp(0d, 1d)
				.apply(c -> (c <= 0.04045d) ? (c / 12.92d) : (pow((c + 0.055d) / 1.055d, 2.4d)));
		
		return new CIEXYZ(__CONVERSION_FROM_RGB.multiply(linear, 0d));
	}
	
	private Triplet xyz;
	
	public CIEXYZ() {
		
		this(0d, 0d, 0d);
	}
	
	public CIEXYZ(double x, double y, double z) {
		
		this(new Triplet(x, y, z));
	}
	
	public CIEXYZ(Triplet xyz) {
		
		this.xyz = xyz;
	}
	
	/**
	 * Convert this CIE XYZ triplet to an {@link RGB} triplet (assumed to be in the
	 * sRGB color-space).
	 * 
	 * @return
	 */
	public RGB toRGB() {
		
		return new RGB(__CONVERSION_TO_RGB.multiply(xyz, 0d)
				.apply(c -> (c <= 0.0031308d) ? (12.92d * c) : (1.055d * pow(c, 1d / 2.4d) - 0.055d)));
	}
	
	public double getX() {
		
		return xyz.get(0);
	}
	
	protected void setX(double x) {
		
		xyz = new Triplet(x, getY(), getZ());
	}
	
	public double getY() {
		
		return xyz.get(1);
	}
	
	protected void setY(double y) {
		
		xyz = new Triplet(getX(), y, getZ());
	}
	
	public double getZ() {
		
		return xyz.get(2);
	}
	
	protected void setZ(double z) {
		
		xyz = new Triplet(getX(), getY(), z);
	}
	
	public Triplet getTriplet() {
		
		return xyz;
	}
	
	@Override
	public String toString() {
		
		return "CIEXYZ [X=" + getX() + ", Y=" + getY() + ", Z=" + getZ() + "]";
	}
	
}
