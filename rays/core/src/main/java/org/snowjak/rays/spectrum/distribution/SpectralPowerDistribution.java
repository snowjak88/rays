package org.snowjak.rays.spectrum.distribution;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.commons.math3.util.Pair;
import org.snowjak.rays.Settings;
import org.snowjak.rays.Settings.ComponentSpectrumName;
import org.snowjak.rays.geometry.util.Point;
import org.snowjak.rays.serialization.IsLoadable;
import org.snowjak.rays.spectrum.Spectrum;
import org.snowjak.rays.spectrum.colorspace.RGB;
import org.snowjak.rays.spectrum.colorspace.XYZ;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

/**
 * A {@link SpectralPowerDistribution} ("SPD") is a distribution of power-levels
 * (usually given in Watts per square-meter per steradian,
 * <code>W * m^-2 * sr^-1</code>) across a range of wavelengths.
 * 
 * @author snowjak88
 *
 */
public class SpectralPowerDistribution extends TabulatedDistribution<SpectralPowerDistribution, Point>
		implements Spectrum {
	
	private static final long serialVersionUID = -1097611220827388034L;
	private static final Pattern __surrounding_doublequotes_pattern = Pattern.compile("\"(.*)\"");
	
	/**
	 * Given an {@link RGB} triplet, select a SpectralPowerDistribution that can
	 * represent this triplet.
	 * <p>
	 * There are an arbitrary number of SPDs that can represent any single RGB
	 * triplet. As such, the SPD selected by this method must be inherently
	 * arbitrary and approximate. However, it is nevertheless a desirable
	 * approximation, because energy-transport can be more accurately modeled using
	 * an energy-distribution.
	 * </p>
	 * <p>
	 * The algorithm used here is given by Brian Smits
	 * ({@link http://www.cs.utah.edu/~bes/papers/color/}). It is reliant upon
	 * component-specific spectra generated using the companion "spectrum-generator"
	 * sub-project.
	 * </p>
	 * 
	 * @param rgb
	 * @return
	 */
	public static SpectralPowerDistribution fromRGB(RGB rgb) {
		
		Spectrum spd = null;
		
		final SpectralPowerDistribution white = Settings.getInstance().getComponentSpectra()
				.get(ComponentSpectrumName.WHITE),
				red = Settings.getInstance().getComponentSpectra().get(ComponentSpectrumName.RED),
				green = Settings.getInstance().getComponentSpectra().get(ComponentSpectrumName.GREEN),
				blue = Settings.getInstance().getComponentSpectra().get(ComponentSpectrumName.BLUE),
				cyan = Settings.getInstance().getComponentSpectra().get(ComponentSpectrumName.CYAN),
				magenta = Settings.getInstance().getComponentSpectra().get(ComponentSpectrumName.MAGENTA),
				yellow = Settings.getInstance().getComponentSpectra().get(ComponentSpectrumName.YELLOW);
		
		if (rgb.getRed() <= rgb.getGreen() && rgb.getRed() <= rgb.getBlue()) {
			
			spd = white.multiply(rgb.getRed());
			
			if (rgb.getGreen() <= rgb.getBlue()) {
				spd = spd.add(cyan.multiply(rgb.getGreen() - rgb.getRed()));
				spd = spd.add(blue.multiply(rgb.getBlue() - rgb.getGreen()));
			} else {
				spd = spd.add(cyan.multiply(rgb.getBlue() - rgb.getRed()));
				spd = spd.add(green.multiply(rgb.getGreen() - rgb.getBlue()));
			}
			
		} else if (rgb.getGreen() <= rgb.getRed() && rgb.getGreen() <= rgb.getBlue()) {
			
			spd = white.multiply(rgb.getGreen());
			
			if (rgb.getRed() <= rgb.getBlue()) {
				spd = spd.add(magenta.multiply(rgb.getRed() - rgb.getGreen()));
				spd = spd.add(blue.multiply(rgb.getBlue() - rgb.getRed()));
			} else {
				spd = spd.add(magenta.multiply(rgb.getBlue() - rgb.getGreen()));
				spd = spd.add(red.multiply(rgb.getRed() - rgb.getBlue()));
			}
			
		} else {
			
			spd = white.multiply(rgb.getBlue());
			
			if (rgb.getRed() <= rgb.getGreen()) {
				spd = spd.add(yellow.multiply(rgb.getRed() - rgb.getBlue()));
				spd = spd.add(green.multiply(rgb.getGreen() - rgb.getRed()));
			} else {
				spd = spd.add(yellow.multiply(rgb.getGreen() - rgb.getBlue()));
				spd = spd.add(red.multiply(rgb.getRed() - rgb.getGreen()));
			}
			
		}
		
		final double currentSpdY = XYZ.fromSpectrum((SpectralPowerDistribution) spd).getY();
		if (Settings.getInstance().nearlyEqual(currentSpdY, 0d))
			return (SpectralPowerDistribution) spd;
		
		final double targetY = rgb.to(XYZ.class).getY();
		final double scaleFactor = targetY / currentSpdY;
		
		return (SpectralPowerDistribution) spd.multiply(scaleFactor);
	}
	
	/**
	 * Load a SpectralPowerDistribution from a CSV-formatted {@link InputStream}.
	 * 
	 * @param csvStream
	 * @return
	 * @throws IOException
	 */
	public static SpectralPowerDistribution loadFromCSV(InputStream csvStream) throws IOException {
		
		return TabulatedDistribution.loadFromCSV(csvStream,
				(bounds, values) -> new SpectralPowerDistribution(bounds, values),
				line -> SpectralPowerDistribution.parseCSVLine(line), len -> new Point[len]);
	}
	
	/**
	 * Write a SpectralPowerDistribution out to a CSV-formatter
	 * {@link OutputStream}.
	 * 
	 * @param csvStream
	 * @throws IOException
	 */
	public void saveToCSV(OutputStream csvStream) throws IOException {
		
		this.saveToCSV(csvStream, SpectralPowerDistribution::buildCSVLine);
	}
	
	/**
	 * Construct a new 0-value SPD (composed of
	 * {@link Settings#getSpectrumBinCount()} entries covering
	 * {@link Settings#getSpectrumRange()}).
	 */
	public SpectralPowerDistribution() {
		
		super(Settings.getInstance().getSpectrumRange(), Settings.getInstance().getSpectrumBinCount());
	}
	
	/**
	 * Construct a new SPD (assumed to span {@link Settings#getSpectrumRange()}).
	 * <p>
	 * Note that this constructor will not resize your SPD to match the configured
	 * settings (i.e., {@link Settings#getSpectrumBinCount()}). You should resize
	 * the constructed SPD (using {@link #resize()}) as soon as convenient.
	 * </p>
	 * 
	 * @param values
	 */
	public SpectralPowerDistribution(Point[] values) {
		
		super(Settings.getInstance().getSpectrumRange(), values);
	}
	
	/**
	 * Create a new {@link SpectralPowerDistribution} covering the given bounds with
	 * <code>values</code>.
	 * <p>
	 * Note that this constructor will not resize your SPD to match the configured
	 * settings (i.e., {@link Settings#getSpectrumBinCount()},
	 * {@link Settings#getSpectrumRange()}). You should resize the constructed SPD
	 * (using {@link #resize()}) as soon as convenient.
	 * </p>
	 * 
	 * @param bounds
	 * @param values
	 */
	public SpectralPowerDistribution(double lowerBound, double upperBound, Point[] values) {
		
		this(new Pair<>(lowerBound, upperBound), values);
	}
	
	/**
	 * Create a new {@link SpectralPowerDistribution} covering the given
	 * <code>bounds</code> with <code>values</code>.
	 * <p>
	 * Note that this constructor will not resize your SPD to match the configured
	 * settings (i.e., {@link Settings#getSpectrumBinCount()},
	 * {@link Settings#getSpectrumRange()}). You should resize the constructed SPD
	 * (using {@link #resize()}) as soon as convenient.
	 * </p>
	 * 
	 * @param bounds
	 * @param values
	 */
	public SpectralPowerDistribution(Pair<Double, Double> bounds, Point[] values) {
		
		super(bounds, values);
	}
	
	/**
	 * Create a new {@link SpectralPowerDistribution} by sampling the given
	 * {@link Distribution} <code>n</code> times across the given interval
	 * <code>t</code> (where <code>n</code> is given by
	 * {@link Settings#getSpectrumBinCount()}, and <code>t</code> by
	 * [{@link Settings#getSpectrumRangeLow()},
	 * {@link Settings#getSpectrumRangeHigh()}]).
	 * 
	 * @param sample
	 * @param sampleCount
	 */
	public SpectralPowerDistribution(Distribution<Point> sample) {
		
		super(sample, Settings.getInstance().getSpectrumRange(), Settings.getInstance().getSpectrumBinCount());
	}
	
	@Override
	protected Point getZero() {
		
		return new Point();
	}
	
	@Override
	protected Point[] getArray(int len) {
		
		return new Point[len];
	}
	
	@Override
	protected SpectralPowerDistribution getNewInstance(Pair<Double, Double> bounds, Point[] values) {
		
		return new SpectralPowerDistribution(bounds, values);
	}
	
	@Override
	public boolean isBlack() {
		
		return Arrays.stream(getEntries()).allMatch(p -> Settings.getInstance().nearlyEqual(p.get(0), 0));
	}
	
	private SpectralPowerDistribution apply(SpectralPowerDistribution other, BiFunction<Point, Point, Point> operator) {
		
		final var resized1 = this.resize(Settings.getInstance().getSpectrumRange(),
				Settings.getInstance().getSpectrumBinCount());
		final var resized2 = other.resize(Settings.getInstance().getSpectrumRange(),
				Settings.getInstance().getSpectrumBinCount());
		
		final Point[] result = new Point[Settings.getInstance().getSpectrumBinCount()];
		for (int i = 0; i < Settings.getInstance().getSpectrumBinCount(); i++)
			result[i] = operator.apply(resized1.getEntries()[i], resized2.getEntries()[i]);
		
		return getNewInstance(Settings.getInstance().getSpectrumRange(), result);
	}
	
	private SpectralPowerDistribution apply(Function<Point, Point> operator) {
		
		return getNewInstance(this.getBounds().get(),
				Arrays.stream(this.getEntries()).map(operator).toArray(len -> this.getArray(len))).resize();
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Note that this method will resize the backing
	 * {@link SpectralPowerDistribution} to match the configured settings
	 * ({@link Settings#getSpectrumBinCount()},
	 * {@link Settings#getSpectrumRange()}).
	 * </p>
	 */
	@Override
	public Spectrum add(Spectrum addend) {
		
		if (SpectralPowerDistribution.class.isAssignableFrom(addend.getClass())) {
			return this.apply((SpectralPowerDistribution) addend, (p1, p2) -> p1.add(p2));
		} else
			throw new RuntimeException("Adding \"" + this.getClass().getSimpleName() + "\" to \""
					+ addend.getClass().getSimpleName() + "\" is not supported yet!");
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Note that this method will resize the backing
	 * {@link SpectralPowerDistribution} to match the configured settings
	 * ({@link Settings#getSpectrumBinCount()},
	 * {@link Settings#getSpectrumRange()}).
	 * </p>
	 */
	@Override
	public Spectrum multiply(Spectrum multiplicand) {
		
		if (SpectralPowerDistribution.class.isAssignableFrom(multiplicand.getClass())) {
			return this.apply((SpectralPowerDistribution) multiplicand, (p1, p2) -> p1.multiply(p2));
		} else
			throw new RuntimeException("Multiplying \"" + this.getClass().getSimpleName() + "\" by \""
					+ multiplicand.getClass().getSimpleName() + "\" is not supported yet!");
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Note that this method will resize the backing
	 * {@link SpectralPowerDistribution} to match the configured settings
	 * ({@link Settings#getSpectrumBinCount()},
	 * {@link Settings#getSpectrumRange()}).
	 * </p>
	 */
	@Override
	public Spectrum multiply(double scalar) {
		
		return this.apply(p -> p.multiply(scalar));
	}
	
	/**
	 * Normalize this SPD -- i.e., scale all power-readings together so that each
	 * power-reading is in (...,1].
	 * <p>
	 * Note that this method will resize the backing
	 * {@link SpectralPowerDistribution} to match the configured settings
	 * ({@link Settings#getSpectrumBinCount()},
	 * {@link Settings#getSpectrumRange()}).
	 * </p>
	 * 
	 * @return
	 */
	public SpectralPowerDistribution normalize() {
		
		final Point maxValue = Arrays.stream(getEntries()).reduce((p1, p2) -> (p1.get(0) >= p2.get(0)) ? p1 : p2).get();
		
		return apply(p -> p.divide(maxValue));
	}
	
	/**
	 * Resize this SPD so that:
	 * <ul>
	 * <li>new bounds equals {@link Settings#getSpectrumRangeLow()},
	 * {@link Settings#getSpectrumRangeHigh()}</li>
	 * <li>new length equals {@link Settings#getSpectrumBinCount()}</li>
	 * </ul>
	 * 
	 * @return
	 */
	public SpectralPowerDistribution resize() {
		
		return this.resize(Settings.getInstance().getSpectrumRange(), Settings.getInstance().getSpectrumBinCount());
	}
	
	@Override
	public RGB toRGB(boolean isEmissive) {
		
		return XYZ.fromSpectrum(this, isEmissive).to(RGB.class);
	}
	
	protected static Pair<Double, Point> parseCSVLine(String line) throws NumberFormatException {
		
		final var parts = line.split(",");
		
		for (int i = 0; i < parts.length; i++) {
			final var m = __surrounding_doublequotes_pattern.matcher(parts[i]);
			if (m.matches())
				parts[i] = m.group(1);
		}
		
		return new Pair<>(Double.parseDouble(parts[0]), new Point(Double.parseDouble(parts[1])));
	}
	
	protected static String buildCSVLine(double point, Point value) {
		
		return Double.toString(point) + "," + Double.toString(value.get(0));
	}
	
	@Override
	public String toString() {
		
		final RGB rgb = this.toRGB();
		return "SpectralPowerDistribution [ " + rgb.toString() + " ]";
	}
	
	public static class Loader implements IsLoadable<SpectralPowerDistribution> {
		
		@Override
		public SpectralPowerDistribution deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			
			if (!json.isJsonObject())
				throw new JsonParseException(
						"Cannot parse SpectralPowerDistribution if JSON is not given as an object!");
			
			final var obj = json.getAsJsonObject();
			
			if (!obj.has("low"))
				throw new JsonParseException("Cannot parse SpectralPowerDistribution: missing [low]!");
			if (!obj.has("high"))
				throw new JsonParseException("Cannot parse SpectralPowerDistribution: missing [high]!");
			if (!obj.has("data"))
				throw new JsonParseException("Cannot parse SpectralPowerDistribution: missing [data]!");
			
			final var low = obj.get("low").getAsDouble();
			final var high = obj.get("high").getAsDouble();
			
			if (!obj.get("data").isJsonArray())
				throw new JsonParseException("Cannot parse SpectralPowerDistribution: [data] is not an array!");
			
			final var data = obj.get("data").getAsJsonArray();
			
			final var values = new ArrayList<Point>();
			for (int i = 0; i < data.size(); i++)
				values.add(new Point(data.get(i).getAsDouble()));
			
			return new SpectralPowerDistribution(low, high, values.toArray(new Point[0]));
		}
		
		@Override
		public JsonElement serialize(SpectralPowerDistribution src, Type typeOfSrc, JsonSerializationContext context) {
			
			final var obj = new JsonObject();
			
			obj.addProperty("low", src.getBounds().get().getFirst());
			obj.addProperty("high", src.getBounds().get().getSecond());
			
			final var array = new JsonArray();
			Arrays.stream(src.getEntries()).sequential().forEach(p -> array.add(p.get(0)));
			
			obj.add("data", array);
			
			return obj;
		}
		
	}
}
