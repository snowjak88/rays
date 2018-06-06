package org.snowjak.rays.spectrum;

import static org.apache.commons.math3.util.FastMath.min;
import static org.apache.commons.math3.util.FastMath.pow;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.FastMath;
import org.snowjak.rays.Settings;
import org.snowjak.rays.geometry.util.Matrix;
import org.snowjak.rays.geometry.util.Triplet;

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
	
	public static final NavigableMap<Double, Triplet> COLOR_MAPPING_FUNCTIONS = Collections.unmodifiableNavigableMap(
			new XyzCsvFileLoader().load_lxyz(Settings.getInstance().getCieCsvColorMappingFunctionsPath()));
	
	public static final NavigableMap<Double, Double> D65_STANDARD_ILLUMINATOR_SPECTRUM = Collections
			.unmodifiableNavigableMap(
					new XyzCsvFileLoader().load_lx(Settings.getInstance().getCieCsvD65IlluminatorPath(), true));
	
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
	public static CIEXYZ fromSpectrum(NavigableMap<Double, Double> spectrum) {
		
		final double cmfHighLambda = COLOR_MAPPING_FUNCTIONS.lastKey();
		final double cmfLowLambda = COLOR_MAPPING_FUNCTIONS.firstKey();
		final double cmfStepSize = (cmfHighLambda - cmfLowLambda) / (double) (COLOR_MAPPING_FUNCTIONS.size() - 1);
		
		final double specHighLambda = spectrum.lastKey();
		final double specLowLambda = spectrum.firstKey();
		final double specStepSize = (specHighLambda - specLowLambda) / (double) (spectrum.size() - 1);
		
		final double stepSize = min(cmfStepSize, specStepSize);
		
		final var result = COLOR_MAPPING_FUNCTIONS.keySet().stream().map(lambda -> {
			
			var lowCmfEntry = COLOR_MAPPING_FUNCTIONS.floorEntry(lambda);
			var highCmfEntry = COLOR_MAPPING_FUNCTIONS.ceilingEntry(lambda);
			var cmfFraction = (lambda - lowCmfEntry.getKey())
					/ (highCmfEntry.getKey() - lowCmfEntry.getKey() + Double.MIN_VALUE);
			final var cmf = lowCmfEntry.getValue().linearInterpolateTo(highCmfEntry.getValue(), cmfFraction);
			
			var lowSpecEntry = spectrum.floorEntry(lambda);
			var highSpecEntry = spectrum.ceilingEntry(lambda);
			
			double spec = (highSpecEntry == null) ? lowSpecEntry.getValue() : highSpecEntry.getValue();
			if (lowSpecEntry != null && highSpecEntry != null) {
				var specFraction = (lambda - lowSpecEntry.getKey())
						/ (highSpecEntry.getKey() - lowSpecEntry.getKey() + Double.MIN_VALUE);
				spec = linearInterpolate(specFraction, lowSpecEntry.getValue(), highSpecEntry.getValue());
			}
			
			return cmf.multiply(spec);
		}).reduce(new Triplet(), Triplet::add).multiply(stepSize);
		
		return new CIEXYZ(result);
		
	}
	
	/**
	 * Calculate the CIE XYZ tristimulus triplet associated with the given
	 * wavelength (assumed to be expressed in nanometers). This method will
	 * linearly-interpolate between neighboring nodes in the loaded color-mapping
	 * function (given, e.g., at 1-nm increments)
	 * 
	 * @param wavelength
	 *            a given wavelength (given in nanometers)
	 * @return a CIE-XYZ tristimulus triplet representing the color-mapping
	 *         functions for the given wavelength
	 */
	public static CIEXYZ fromWavelength(double wavelength) {
		
		if (wavelength < COLOR_MAPPING_FUNCTIONS.firstKey())
			return new CIEXYZ(COLOR_MAPPING_FUNCTIONS.firstEntry().getValue());
		
		if (wavelength > COLOR_MAPPING_FUNCTIONS.lastKey())
			return new CIEXYZ(COLOR_MAPPING_FUNCTIONS.lastEntry().getValue());
		
		final var lower = COLOR_MAPPING_FUNCTIONS.floorEntry(wavelength);
		final var upper = COLOR_MAPPING_FUNCTIONS.ceilingEntry(wavelength);
		
		final double fraction = (wavelength - lower.getKey()) / (upper.getKey() - lower.getKey());
		
		return new CIEXYZ(lower.getValue().linearInterpolateTo(upper.getValue(), fraction));
		
	}
	
	/**
	 * Linearly interpolate (by <code>fraction</code>) between <code>p1</code> and
	 * <code>p2</code>.
	 * 
	 * @param fraction
	 *            a fraction specifying the distance from <code>p1</code> to
	 *            <code>p2</code>
	 * @param p1
	 *            the value to interpolate from
	 * @param p2
	 *            the value to interpolate to
	 */
	protected static double linearInterpolate(double fraction, double p1, double p2) {
		
		return ((p2 - p1) * fraction) + p1;
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
	
	/**
	 * Loader helper-class
	 * 
	 * @author snowjak88
	 *
	 */
	public static class XyzCsvFileLoader {
		
		public NavigableMap<Double, Triplet> load_lxyz(String filePath) {
			
			return load(filePath, 3, (xyz) -> new Triplet(xyz.get(0), xyz.get(1), xyz.get(2)));
		}
		
		public NavigableMap<Double, Double> load_lx(String filePath, boolean normalize) {
			
			final var result = load(filePath, 1, (x) -> x.get(0));
			
			if (normalize) {
				final var maxValue = result.values().stream().reduce(FastMath::max).orElse(1d);
				result.keySet().forEach(key -> result.compute(key, (k, v) -> v / maxValue));
			}
			
			return result;
		}
		
		private <T> NavigableMap<Double, T> load(String filePath, int dimensionsExpected,
				Function<List<Double>, T> supplier) {
			
			final var cmf = new TreeMap<Double, T>();
			
			try (var reader = new BufferedReader(
					new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream(filePath)))) {
				
				while (reader.ready()) {
					//
					// While reading in lines -- break each line into pieces, strip surrounding
					// double-quotes off each piece, and convert it to a double value.
					//
					final var quotesMatcher = Pattern.compile("\"(.*)\"");
					final var pieces = Arrays.asList(reader.readLine().split(",")).stream().map(s -> {
						var m = quotesMatcher.matcher(s);
						if (m.matches())
							return m.group(1);
						return s;
					}).map(s -> Double.parseDouble(s)).collect(Collectors.toList());
					
					cmf.put(pieces.get(0), supplier.apply(pieces.subList(1, pieces.size())));
				}
				
			} catch (Throwable t) {
				throw new RuntimeException("Could not load data from \"" + filePath + "\"!", t);
			}
			
			return cmf;
		}
	}
	
	@Override
	public String toString() {
		
		return "CIEXYZ [X=" + getX() + ", Y=" + getY() + ", Z=" + getZ() + "]";
	}
	
}
