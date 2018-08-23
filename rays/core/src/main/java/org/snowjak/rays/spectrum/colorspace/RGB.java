package org.snowjak.rays.spectrum.colorspace;

import static org.apache.commons.math3.util.FastMath.abs;
import static org.apache.commons.math3.util.FastMath.max;
import static org.apache.commons.math3.util.FastMath.min;
import static org.apache.commons.math3.util.FastMath.pow;

import java.lang.reflect.Type;

import org.snowjak.rays.geometry.util.Matrix;
import org.snowjak.rays.geometry.util.Triplet;
import org.snowjak.rays.serialization.IsLoadable;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

/**
 * Represents the sRGB colorspace.
 * 
 * @author snowjak88
 *
 */
public class RGB extends Colorspace<RGB, Triplet> {
	
	/**
	 * Represents the RGB triplet { 0, 0, 0 }
	 */
	public static final RGB BLACK = new RGB(0d, 0d, 0d);
	/**
	 * Represents the RGB triplet { 1, 1, 1 }
	 */
	public static final RGB WHITE = new RGB(1d, 1d, 1d);
	/**
	 * Represents the RGB triplet { 1, 0, 0 }
	 */
	public static final RGB RED = new RGB(1d, 0d, 0d);
	/**
	 * Represents the RGB triplet { 0, 1, 0 }
	 */
	public static final RGB GREEN = new RGB(0d, 1d, 0d);
	/**
	 * Represents the RGB triplet { 0, 0, 1 }
	 */
	public static final RGB BLUE = new RGB(0d, 0d, 1d);
	
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
	public static RGB fromHSL(double hue, double saturation, double lightness) {
		
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
		
		return new RGB(r1 + m, g1 + m, b1 + m);
	}
	
	/**
	 * Given an integer containing a packed ARGB quadruple, unpack it into an RGB
	 * instance.
	 * 
	 * @param packedRGB
	 * @return
	 */
	public static RGB fromPacked(int packedRGB) {
		
		final int b = packedRGB & 255;
		
		packedRGB = packedRGB >> 8;
		final int g = packedRGB & 255;
		
		packedRGB = packedRGB >> 8;
		final int r = packedRGB & 255;
		
		return new RGB((double) r / 256d, (double) g / 256d, (double) b / 256d);
	}
	
	/**
	 * Given an RGB instance, transform it to a packed ARGB quadruple.
	 * 
	 * @param rgb
	 * @return
	 * @see #toPacked()
	 */
	public static int toPacked(RGB rgb) {
		
		final double a = 1d;
		final double r = max(min(rgb.getRed(), 1d), 0d);
		final double g = max(min(rgb.getGreen(), 1d), 0d);
		final double b = max(min(rgb.getBlue(), 1d), 0d);
		
		return ((int) (a * 255d)) << 24 | ((int) (r * 255d)) << 16 | ((int) (g * 255d)) << 8 | ((int) (b * 255d));
	}
	
	/**
	 * Pack this RGB instance into an ARGB quadruple.
	 * 
	 * @return
	 * @see #toPacked(RGB)
	 */
	public int toPacked() {
		
		return RGB.toPacked(this);
	}
	
	public RGB(double red, double green, double blue) {
		
		this(new Triplet(red, green, blue));
	}
	
	public RGB(Triplet representation) {
		
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
	public RGB clamp() {
		
		return new RGB(get().clamp(0d, 1d));
	}
	
	@Override
	protected void registerConverters(ColorspaceConverterRegistry<RGB> registry) {
		
		registry.register(RGB.class, (rgb) -> rgb);
		
		registry.register(XYZ.class, (rgb) -> new XYZ(__CONVERSION_TO_XYZ.multiply(
				rgb.get().apply(c -> (c <= 0.04045d) ? (c / 12.92d) : (pow((c + 0.055d) / 1.055d, 2.4d))),
				0d)));
	}
	
	@Override
	public String toString() {
		
		return "RGB [red=" + getRed() + ", green=" + getGreen() + ", blue=" + getBlue() + "]";
	}
	
	public static class Loader implements IsLoadable<RGB> {
		
		@Override
		public RGB deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			
			if (!json.isJsonArray())
				throw new JsonParseException("Cannot deserialize a RGB from JSON that is not given as an array!");
			
			final var array = json.getAsJsonArray();
			
			final var values = new double[array.size()];
			for (int i = 0; i < values.length; i++)
				values[i] = array.get(i).getAsDouble();
			
			return new RGB(new Triplet(values));
		}
		
		@Override
		public JsonElement serialize(RGB src, Type typeOfSrc, JsonSerializationContext context) {
			
			final var array = new JsonArray(3);
			array.add(new JsonPrimitive(src.getRed()));
			array.add(new JsonPrimitive(src.getGreen()));
			array.add(new JsonPrimitive(src.getBlue()));
			
			return array;
		}
		
	}
	
}
