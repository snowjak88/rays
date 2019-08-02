package org.snowjak.rays.spectrum.colorspace;

import static org.apache.commons.math3.util.FastMath.pow;

import java.lang.reflect.Type;

import org.snowjak.rays.Settings;
import org.snowjak.rays.Util;
import org.snowjak.rays.geometry.util.Matrix;
import org.snowjak.rays.geometry.util.Triplet;
import org.snowjak.rays.serialization.IsLoadable;
import org.snowjak.rays.spectrum.Spectrum;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

/**
 * Represents the CIE 1931 XYZ tristimulus colorspace.
 * <h3>JSON</h3>
 * <p>
 * An XYZ triplet may be serialized to JSON as an array.
 * </p>
 * 
 * <pre>
 * ...
 * [ <em>x</em>, <em>y</em>, <em>z</em> ]
 * ...
 * </pre>
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
	 * <p>
	 * <strong>Note</strong> that it is assumed that the given SPD represents a
	 * non-relative energy-distribution -- i.e., with physical units of
	 * {@code W * nm}, integrating to {@code W}.
	 * </p>
	 * 
	 * @param spectrum
	 * @return
	 * @see #fromSpectrum(Spectrum, boolean)
	 */
	public static XYZ fromSpectrum(Spectrum spectrum) {
		
		return fromSpectrum(spectrum, false);
	}
	
	/**
	 * Calculate the CIE XYZ tristimulus triplet derived from the given spectral
	 * power-distribution (given as a mapping of wavelengths (nm) to spectral
	 * radiance (W/sr*m^2)).
	 * 
	 * <p>
	 * If {@code isRelative = false}, then it is assumed that this Spectrum's
	 * integral equals a certain power (in Watts), and the returned XYZ triplet
	 * expresses <em>absolute</em> luminance -- i.e., its {@code Y} component will
	 * report luminance in terms of candela per square meter.
	 * </p>
	 * <p>
	 * If {@code isRelative = true}, then the returned XYZ triplet expresses
	 * <em>relative</em> luminance. It is assumed that this spectrum represents an
	 * absorption-spectrum being illuminated by the standard illuminant (see
	 * {@link Settings#getIlluminatorSpectralPowerDistribution()} with a total
	 * luminous intensity of 1 candela.
	 * </p>
	 * 
	 * @param spectrum
	 * @param isRelative
	 * @return
	 */
	public static XYZ fromSpectrum(Spectrum spectrum, boolean isRelative) {
		
		final var cmf = Settings.getInstance().getColorMappingFunctions();
		final var illuminant = Settings.getInstance().getIlluminatorSpectralPowerDistribution();
		
		if (!(spectrum instanceof SpectralPowerDistribution))
			throw new IllegalArgumentException(
					"Cannot create an XYZ triplet from the given Spectrum -- cannot handle Spectrum implementation.");
		
		final var spd = (SpectralPowerDistribution) spectrum;
		
		final double lowLambda = spd.getBounds().get().getFirst();
		final double highLambda = spd.getBounds().get().getSecond();
		
		final var numerator = Util.integrateTriplet(lowLambda, highLambda,
				Settings.getInstance().getCieXyzIntegrationStepCount(),
				(lambda) -> cmf.get(lambda).multiply(spd.get(lambda).get(0)));
		
		if (!isRelative)
			return new XYZ(numerator);
		else {
			
			final var denominator = Util.integrate(lowLambda, highLambda,
					Settings.getInstance().getCieXyzIntegrationStepCount(),
					(lambda) -> cmf.get(lambda).get(1) * illuminant.get(lambda).get(0));
			
			return new XYZ(numerator.divide(denominator));
		}
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
		registry.register(RGB_Gammaless.class, (xyz) -> new RGB_Gammaless(__CONVERSION_TO_RGB.multiply(xyz.get(), 0d)));
		registry.register(RGB.class, (xyz) -> new RGB(xyz.to(RGB_Gammaless.class).get()
				.apply(c -> (c <= 0.0031308d) ? (12.92d * c) : (1.055d * pow(c, 1d / 2.4d)) - 0.055d)));
	}
	
	@Override
	public String toString() {
		
		return "XYZ [X=" + getX() + ", Y=" + getY() + ", Z=" + getZ() + "]";
	}
	
	public static class Loader implements IsLoadable<XYZ> {
		
		@Override
		public XYZ deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			
			if (!json.isJsonArray())
				throw new JsonParseException("Cannot deserialize a XYZ from JSON that is not given as an array!");
			
			final var array = json.getAsJsonArray();
			
			final var values = new double[array.size()];
			for (int i = 0; i < values.length; i++)
				values[i] = array.get(i).getAsDouble();
			
			return new XYZ(new Triplet(values));
		}
		
		@Override
		public JsonElement serialize(XYZ src, Type typeOfSrc, JsonSerializationContext context) {
			
			final var array = new JsonArray(3);
			array.add(new JsonPrimitive(src.getX()));
			array.add(new JsonPrimitive(src.getY()));
			array.add(new JsonPrimitive(src.getZ()));
			
			return array;
		}
		
	}
}
