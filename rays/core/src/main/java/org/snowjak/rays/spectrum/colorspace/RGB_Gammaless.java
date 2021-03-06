package org.snowjak.rays.spectrum.colorspace;

import static org.apache.commons.math3.util.FastMath.abs;
import static org.apache.commons.math3.util.FastMath.max;
import static org.apache.commons.math3.util.FastMath.min;
import static org.apache.commons.math3.util.FastMath.pow;

import java.lang.reflect.Type;

import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.annotations.UIType;
import org.snowjak.rays.geometry.util.Matrix;
import org.snowjak.rays.geometry.util.Triplet;
import org.snowjak.rays.serialization.IsLoadable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

/**
 * Represents the sRGB colorspace without gamma-correction.
 * <p>
 * For comparison, {@link RGB} represents the sRGB colorspace <em>with</em>
 * gamma-correction.
 * </p>
 * 
 * @author snowjak88
 *
 */
@UIType(fields = { @UIField(name = "red", type = Double.class, defaultValue = "1"),
		@UIField(name = "green", type = Double.class, defaultValue = "1"),
		@UIField(name = "blue", type = Double.class, defaultValue = "1") })
public class RGB_Gammaless extends Colorspace<RGB_Gammaless, Triplet> {
	
	/**
	 * Represents the RGB triplet { 0, 0, 0 }
	 */
	public static final RGB_Gammaless BLACK = new RGB_Gammaless(0d, 0d, 0d);
	/**
	 * Represents the RGB triplet { 1, 1, 1 }
	 */
	public static final RGB_Gammaless WHITE = new RGB_Gammaless(1d, 1d, 1d);
	/**
	 * Represents the RGB triplet { 1, 0, 0 }
	 */
	public static final RGB_Gammaless RED = new RGB_Gammaless(1d, 0d, 0d);
	/**
	 * Represents the RGB triplet { 0, 1, 0 }
	 */
	public static final RGB_Gammaless GREEN = new RGB_Gammaless(0d, 1d, 0d);
	/**
	 * Represents the RGB triplet { 0, 0, 1 }
	 */
	public static final RGB_Gammaless BLUE = new RGB_Gammaless(0d, 0d, 1d);
	
	//@formatter:off
	private static final Matrix __CONVERSION_TO_XYZ =
			new Matrix(new double[][] {
				{ 0.4124d, 0.3576d, 0.1805d, 0d },
				{ 0.2126d, 0.7152d, 0.0722d, 0d },
				{ 0.0193d, 0.1192d, 0.9505d, 0d },
				{ 0d,      0d,      0d,      0d }
			});
	//@formatter:on
	
	/**
	 * Construct a new RGB trio from an HSL trio.
	 * 
	 * @param hue
	 *            hue-angle, given in <strong>degrees</strong>
	 * @param saturation
	 *            saturation-value, given in <code>[0,1]</code>
	 * @param lightness
	 *            lightness-value, given in <code>[0,1]</code>
	 * @return
	 */
	public static RGB_Gammaless fromHSL(double hue, double saturation, double lightness) {
		
		final double chroma = (1d - abs(2d * lightness - 1)) * saturation;
		
		final double h_prime = hue / 60d;
		
		final double x = chroma * (1d - abs((h_prime % 2) - 1));
		
		final double r1, g1, b1;
		if (h_prime >= 0d && h_prime <= 1d) {
			r1 = chroma;
			g1 = x;
			b1 = 0d;
		} else if (h_prime >= 1d && h_prime <= 2d) {
			r1 = x;
			g1 = chroma;
			b1 = 0d;
		} else if (h_prime >= 2d && h_prime <= 3d) {
			r1 = 0d;
			g1 = chroma;
			b1 = x;
		} else if (h_prime >= 3d && h_prime <= 4d) {
			r1 = 0d;
			g1 = x;
			b1 = chroma;
		} else if (h_prime >= 4d && h_prime <= 5d) {
			r1 = x;
			g1 = 0d;
			b1 = chroma;
		} else if (h_prime >= 5d && h_prime <= 6d) {
			r1 = chroma;
			g1 = 0d;
			b1 = x;
		} else {
			r1 = 0d;
			g1 = 0d;
			b1 = 0d;
		}
		
		final double m = lightness - chroma / 2d;
		
		return new RGB_Gammaless(r1 + m, g1 + m, b1 + m);
	}
	
	/**
	 * Given an integer containing a packed ARGB quadruple, unpack it into an RGB
	 * instance.
	 * 
	 * @param packedRGB
	 * @return
	 */
	public static RGB_Gammaless fromPacked(int packedRGB) {
		
		final int b = packedRGB & 255;
		
		packedRGB = packedRGB >> 8;
		final int g = packedRGB & 255;
		
		packedRGB = packedRGB >> 8;
		final int r = packedRGB & 255;
		
		return new RGB_Gammaless((double) r / 256d, (double) g / 256d, (double) b / 256d);
	}
	
	/**
	 * Given an RGB instance, transform it to a packed ARGB quadruple (with alpha =
	 * 1.0, fully opaque).
	 * 
	 * @param rgb
	 * @return
	 * @see #toPacked()
	 */
	public static int toPacked(RGB_Gammaless rgb) {
		
		return toPacked(rgb, 1.0);
	}
	
	/**
	 * Given an RGB instance, transform it to a packed ARGB quadruple.
	 * 
	 * @param rgb
	 * @param alpha
	 * @return
	 */
	public static int toPacked(RGB_Gammaless rgb, double alpha) {
		
		final double a = max(min(alpha, 1d), 0d);
		final double r = max(min(rgb.getRed(), 1d), 0d);
		final double g = max(min(rgb.getGreen(), 1d), 0d);
		final double b = max(min(rgb.getBlue(), 1d), 0d);
		
		return ((int) (a * 255d)) << 24 | ((int) (r * 255d)) << 16 | ((int) (g * 255d)) << 8 | ((int) (b * 255d));
		
	}
	
	/**
	 * Pack this RGB instance into an ARGB quadruple.
	 * 
	 * @return
	 * @see #toPacked(RGB_Gammaless)
	 */
	public int toPacked() {
		
		return RGB_Gammaless.toPacked(this);
	}
	
	public RGB_Gammaless(double red, double green, double blue) {
		
		this(new Triplet(red, green, blue));
	}
	
	public RGB_Gammaless(Triplet representation) {
		
		super(representation);
	}
	
	public double getRed() {
		
		return get().get(0);
	}
	
	public double getGreen() {
		
		return get().get(1);
	}
	
	public double getBlue() {
		
		return get().get(2);
	}
	
	@Override
	public RGB_Gammaless clamp() {
		
		return new RGB_Gammaless(get().clamp(0d, 1d));
	}
	
	/**
	 * Remove gamma-correction from an sRGB triplet.
	 * 
	 * @param rgb
	 * @return
	 */
	protected static RGB_Gammaless degammafy(RGB rgb) {
		
		return new RGB_Gammaless(
				rgb.get().apply(c -> (c <= 0.04045d) ? (c / 12.92d) : (pow((c + 0.055d) / 1.055d, 2.4d))));
	}
	
	/**
	 * Add gamma-correction, producing an sRGB triplet.
	 * 
	 * @param rgb
	 * @return
	 */
	protected static RGB gammafy(RGB_Gammaless rgb) {
		
		return new RGB(rgb.get().apply(c -> (c <= 0.0031308d) ? (12.92d * c) : (1.055d * pow(c, 1d / 2.4d)) - 0.055d));
	}
	
	@Override
	protected void registerConverters(ColorspaceConverterRegistry<RGB_Gammaless> registry) {
		
		registry.register(RGB_Gammaless.class, (rgb) -> rgb);
		registry.register(RGB.class, (rgb) -> gammafy(rgb));
		registry.register(XYZ.class, (rgb) -> new XYZ(__CONVERSION_TO_XYZ.multiply(rgb.get(), 0d)));
	}
	
	@Override
	public String toString() {
		
		return "RGB_Gammaless [red=" + getRed() + ", green=" + getGreen() + ", blue=" + getBlue() + "]";
	}
	
	public static class Loader implements IsLoadable<RGB_Gammaless> {
		
		@Override
		public RGB_Gammaless deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			
			if (!json.isJsonObject())
				throw new JsonParseException("Cannot deserialize a RGB from JSON that is not given as an object!");
			
			final var obj = json.getAsJsonObject();
			
			final var red = obj.get("red");
			final var green = obj.get("green");
			final var blue = obj.get("blue");
			
			return new RGB_Gammaless(new Triplet((red == null) ? 0 : red.getAsDouble(),
					(green == null) ? 0 : green.getAsDouble(), (blue == null) ? 0 : blue.getAsDouble()));
		}
		
		@Override
		public JsonElement serialize(RGB_Gammaless src, Type typeOfSrc, JsonSerializationContext context) {
			
			final var obj = new JsonObject();
			
			obj.addProperty("red", src.getRed());
			obj.addProperty("green", src.getGreen());
			obj.addProperty("blue", src.getBlue());
			
			return obj;
		}
		
	}
	
}
