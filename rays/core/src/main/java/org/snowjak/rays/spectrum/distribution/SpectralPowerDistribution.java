package org.snowjak.rays.spectrum.distribution;

import static org.apache.commons.math3.util.FastMath.E;
import static org.apache.commons.math3.util.FastMath.PI;
import static org.apache.commons.math3.util.FastMath.pow;

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
import org.snowjak.rays.spectrum.colorspace.RGB_Gammaless;
import org.snowjak.rays.spectrum.colorspace.XYZ;
import org.snowjak.rays.util.Util;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

/**
 * A {@link SpectralPowerDistribution} ("SPD") is a distribution of power-levels
 * across a range of wavelengths.
 * <p>
 * <strong>Units</strong>:
 * <ul>
 * <li>{@link #get(double)}/{@link #getIndex(double)}/{@link #getPoint(double)}
 * = {@code W*nm}</li>
 * <li>integrated = {@code W}</li>
 * </p>
 * <h3>JSON</h3>
 * <p>
 * An SPD may be serialized to JSON in several formats.
 * </p>
 * <h4>Tabulated</h4>
 * 
 * <pre>
 * ...
 * {
 *     "type": "data",
 *     "low": 360.0,
 *     "high": 830.0,
 *     "data": [ <em>measurements</em> ]
 * }
 * ...
 * </pre>
 * 
 * <h4>RGB + Radiant Power</h4>
 * 
 * <pre>
 * ...
 * {
 *     "type": "rgb",
 *     "rgb": <em>RGB serialization</em>,
 *     "radiance": 2.5
 * }
 * ...
 * </pre>
 * 
 * <h4>Blackbody</h4>
 * 
 * <pre>
 * ...
 * {
 *     "type": "blackbody",
 *     "kelvin": 5500,
 *     <em>(OPTIONAL)</em> "radiance": 2.5
 * }
 * ...
 * </pre>
 * 
 * @author snowjak88
 *
 */
public class SpectralPowerDistribution extends TabulatedDistribution<SpectralPowerDistribution, Point>
		implements Spectrum {
	
	private static final long serialVersionUID = -1097611220827388034L;
	private static final Pattern __surrounding_doublequotes_pattern = Pattern.compile("\"(.*)\"");
	
	/**
	 * Velocity of light ({@code m / s})
	 */
	private static final double c = 2.99792458e8;
	
	/**
	 * Planck constant ({@code J * s})
	 */
	private static final double h = 6.62607015e-34;
	
	/**
	 * Boltzmann constant ({@code J / K})
	 */
	private static final double k = 1.380649e-23;
	
	/**
	 * Stefan-Boltzmann constant ({@code W / m^2 * K^4)
	 */
	private static final double sigma = 5.670374419e-8;
	
	/**
	 * A 0-energy SPD. Equivalent to {@link #ZERO}.
	 */
	public static final SpectralPowerDistribution BLACK = new SpectralPowerDistribution();
	
	/**
	 * A 0-energy SPD. Equivalent to {@link #BLACK}.
	 */
	public static final SpectralPowerDistribution ZERO = BLACK;
	
	private transient Double integral = null;
	
	/**
	 * Produce a SpectralPowerDistribution which models the results of evaluating
	 * Planck's Law for blackbody radiation, for a body of the given temperature.
	 * 
	 * @param kelvin
	 * @return
	 */
	public static SpectralPowerDistribution fromBlackbody(double kelvin) {
		
		return SpectralPowerDistribution.fromBlackbody(kelvin, -1.0);
	}
	
	/**
	 * Produce a SpectralPowerDistribution which models the results of evaluating
	 * Planck's Law for blackbody radiation, for a body of the given temperature.
	 *
	 * <p>
	 * This SPD's power is scaled to {@code intensity} W / m^2 sr.
	 * </p>
	 * 
	 * @param kelvin
	 * @param intensity
	 *            if < 0, then no power-scaling is applied
	 * @return
	 */
	public static SpectralPowerDistribution fromBlackbody(double kelvin, double intensity) {
		
		final double wavelengthStepSizeNM = (Settings.getInstance().getSpectrumRangeHigh()
				- Settings.getInstance().getSpectrumRangeLow())
				/ (double) (Settings.getInstance().getSpectrumBinCount() - 1);
		
		final Point[] values = new Point[Settings.getInstance().getSpectrumBinCount()];
		
		//
		// The current wavelength.
		double wavelengthNM = Settings.getInstance().getSpectrumRangeLow();
		
		for (int i = 0; i < values.length; i++) {
			
			final double v = getPlancksLaw(kelvin, wavelengthNM);
			
			values[i] = new Point(v);
			
			wavelengthNM += wavelengthStepSizeNM;
		}
		
		final var spd = new SpectralPowerDistribution(values);
		
		if (intensity < 0.0)
			return spd;
		
		return (SpectralPowerDistribution) spd.rescale(intensity);
	}
	
	/**
	 * Given a temperature in Kelvin and a wavelength in nanometers, evaluate
	 * Planck's Law for blackbody radiation on this wavelength.
	 * 
	 * @param wavelength
	 * @return
	 */
	public static double getPlancksLaw(double kelvin, double wavelength) {
		
		final double lambda = wavelength / 1.0e9;
		
		return ((2.0 * h * pow(c, 2)) / (pow(lambda, 5))) * (1.0 / (pow(E, (h * c) / (lambda * k * kelvin)) - 1.0));
	}
	
	/**
	 * Given a temperature in Kelvin, evaluate Stefan-Boltzmann's Law for blackbody
	 * radiation.
	 * 
	 * @param kelvin
	 * @return
	 */
	public static double getStefanBoltzmannsLaw(double kelvin) {
		
		return sigma * pow(kelvin, 4) / PI;
	}
	
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
	 * @param rgb_gl
	 * @return
	 */
	public static SpectralPowerDistribution fromRGB(RGB rgb) {
		
		Spectrum spd = null;
		
		final var rgb_gl = rgb.to(RGB_Gammaless.class);
		
		final SpectralPowerDistribution white = Settings.getInstance().getComponentSpectra()
				.get(ComponentSpectrumName.WHITE),
				red = Settings.getInstance().getComponentSpectra().get(ComponentSpectrumName.RED),
				green = Settings.getInstance().getComponentSpectra().get(ComponentSpectrumName.GREEN),
				blue = Settings.getInstance().getComponentSpectra().get(ComponentSpectrumName.BLUE),
				cyan = Settings.getInstance().getComponentSpectra().get(ComponentSpectrumName.CYAN),
				magenta = Settings.getInstance().getComponentSpectra().get(ComponentSpectrumName.MAGENTA),
				yellow = Settings.getInstance().getComponentSpectra().get(ComponentSpectrumName.YELLOW);
		
		if (rgb_gl.getRed() <= rgb_gl.getGreen() && rgb_gl.getRed() <= rgb_gl.getBlue()) {
			
			spd = white.multiply(rgb_gl.getRed());
			
			if (rgb_gl.getGreen() <= rgb_gl.getBlue()) {
				spd = spd.add(cyan.multiply(rgb_gl.getGreen() - rgb_gl.getRed()));
				spd = spd.add(blue.multiply(rgb_gl.getBlue() - rgb_gl.getGreen()));
			} else {
				spd = spd.add(cyan.multiply(rgb_gl.getBlue() - rgb_gl.getRed()));
				spd = spd.add(green.multiply(rgb_gl.getGreen() - rgb_gl.getBlue()));
			}
			
		} else if (rgb_gl.getGreen() <= rgb_gl.getRed() && rgb_gl.getGreen() <= rgb_gl.getBlue()) {
			
			spd = white.multiply(rgb_gl.getGreen());
			
			if (rgb_gl.getRed() <= rgb_gl.getBlue()) {
				spd = spd.add(magenta.multiply(rgb_gl.getRed() - rgb_gl.getGreen()));
				spd = spd.add(blue.multiply(rgb_gl.getBlue() - rgb_gl.getRed()));
			} else {
				spd = spd.add(magenta.multiply(rgb_gl.getBlue() - rgb_gl.getGreen()));
				spd = spd.add(red.multiply(rgb_gl.getRed() - rgb_gl.getBlue()));
			}
			
		} else {
			
			spd = white.multiply(rgb_gl.getBlue());
			
			if (rgb_gl.getRed() <= rgb_gl.getGreen()) {
				spd = spd.add(yellow.multiply(rgb_gl.getRed() - rgb_gl.getBlue()));
				spd = spd.add(green.multiply(rgb_gl.getGreen() - rgb_gl.getRed()));
			} else {
				spd = spd.add(yellow.multiply(rgb_gl.getGreen() - rgb_gl.getBlue()));
				spd = spd.add(red.multiply(rgb_gl.getRed() - rgb_gl.getGreen()));
			}
			
		}
		
		final double currentSpdY = XYZ.fromSpectrum((SpectralPowerDistribution) spd).getY();
		
		if (Settings.getInstance().nearlyEqual(currentSpdY, 0d))
			return (SpectralPowerDistribution) spd;
		
		final double targetY = rgb_gl.to(XYZ.class).getY();
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
	protected SpectralPowerDistribution() {
		
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
	
	@Override
	public Spectrum subtract(Spectrum subtrahend) {
		
		if (SpectralPowerDistribution.class.isAssignableFrom(subtrahend.getClass())) {
			return this.apply((SpectralPowerDistribution) subtrahend, (p1, p2) -> p1.subtract(p2));
		} else
			throw new RuntimeException("Subtracting \"" + subtrahend.getClass().getSimpleName() + "\" from \""
					+ this.getClass().getSimpleName() + "\" is not supported yet!");
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
	
	@Override
	public Spectrum divide(Spectrum divisor) {
		
		if (SpectralPowerDistribution.class.isAssignableFrom(divisor.getClass())) {
			return this.apply((SpectralPowerDistribution) divisor, (p1, p2) -> p1
					.divide((p2.get(0) == 0d) ? new Point(Settings.getInstance().getDoubleEqualityEpsilon()) : p2));
		} else
			throw new RuntimeException("Dividing \"" + this.getClass().getSimpleName() + "\" by \""
					+ divisor.getClass().getSimpleName() + "\" is not supported yet!");
	}
	
	@Override
	public double getPower(double lambda) {
		
		return get(lambda).get(0);
	}
	
	/**
	 * Normalize this SPD's components -- i.e., scale it such that its highest-value
	 * measurement = 1.0
	 * 
	 * @return
	 */
	public SpectralPowerDistribution normalizeComponents() {
		
		final var maxValue = Arrays.stream(getEntries()).mapToDouble(p -> p.get(0)).max().orElse(1d);
		
		return apply(p -> p.divide(maxValue));
	}
	
	@Override
	public SpectralPowerDistribution normalizePower() {
		
		final var power = integrate();
		if (power == 0d)
			return this;
		
		return apply(p -> p.divide(power));
	}
	
	@Override
	public double integrate() {
		
		if (this.integral == null) {
			
			final double lowLambda = getBounds().get().getFirst();
			final double highLambda = getBounds().get().getSecond();
			
			this.integral = Util.integrate(lowLambda, highLambda,
					Settings.getInstance().getCieXyzIntegrationStepCount(), (lambda) -> getPower(lambda));
		}
		
		return this.integral;
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
	public RGB toRGB(boolean isRelative) {
		
		return XYZ.fromSpectrum(this, isRelative).to(RGB.class);
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
		
		final RGB rgb = this.toRGB(true);
		return "SpectralPowerDistribution [ " + rgb.toString() + " / " + getTotalPower() + " W ]";
	}
	
	public static class Loader implements IsLoadable<SpectralPowerDistribution> {
		
		@Override
		public SpectralPowerDistribution deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
				throws JsonParseException {
			
			if (!json.isJsonObject())
				throw new JsonParseException(
						"Cannot parse SpectralPowerDistribution if JSON is not given as an object!");
			
			final var obj = json.getAsJsonObject();
			
			if (!obj.has("type"))
				throw new JsonParseException("Cannot parse SpectralPowerDistribution: missing [type]!");
			
			final SpectralPowerDistribution result;
			
			switch (obj.get("type").getAsString()) {
			case "data":
				if (!obj.has("low"))
					throw new JsonParseException("Cannot parse valued SpectralPowerDistribution: missing [low]!");
				if (!obj.has("high"))
					throw new JsonParseException("Cannot parse valued SpectralPowerDistribution: missing [high]!");
				if (!obj.has("data"))
					throw new JsonParseException("Cannot parse valued SpectralPowerDistribution: missing [data]!");
				
				final var low = obj.get("low").getAsDouble();
				final var high = obj.get("high").getAsDouble();
				
				if (!obj.get("data").isJsonArray())
					throw new JsonParseException("Cannot parse SpectralPowerDistribution: [data] is not an array!");
				
				final var data = obj.get("data").getAsJsonArray();
				
				final var values = new ArrayList<Point>();
				for (int i = 0; i < data.size(); i++)
					values.add(new Point(data.get(i).getAsDouble()));
				
				result = new SpectralPowerDistribution(low, high, values.toArray(new Point[0]));
				break;
			
			case "rgb":
				if (!obj.has("rgb"))
					throw new JsonParseException("Cannot parse RGB-SpectralPowerDistribution: missing [rgb]!");
				
				if (!obj.has("radiance"))
					throw new JsonParseException("Cannot parse RGB-SpectralPowerDistribution: missing [radiance]!");
				
				final RGB rgb = context.deserialize(obj.get("rgb").getAsJsonObject(), RGB.class);
				final double radiantPower = obj.get("radiance").getAsDouble();
				
				result = (SpectralPowerDistribution) SpectralPowerDistribution.fromRGB(rgb).rescale(radiantPower);
				break;
			
			case "blackbody":
				if (!obj.has("kelvin"))
					throw new JsonParseException("Cannot parse blackbody SpectralPowerDistribution: missing [kelvin]!");
				
				final double kelvin = obj.get("kelvin").getAsDouble();
				final double radiance;
				if (obj.has("radiance"))
					radiance = obj.get("radiance").getAsDouble();
				else
					radiance = -1.0;
				
				result = SpectralPowerDistribution.fromBlackbody(kelvin, radiance);
				break;
			
			default:
				throw new JsonParseException("Cannot parse SpectralPowerDistribution: unknown [type]!");
			}
			
			return result;
		}
		
		@Override
		public JsonElement serialize(SpectralPowerDistribution src, Type typeOfSrc, JsonSerializationContext context) {
			
			final var obj = new JsonObject();
			
			obj.addProperty("type", "data");
			obj.addProperty("low", src.getBounds().get().getFirst());
			obj.addProperty("high", src.getBounds().get().getSecond());
			
			final var array = new JsonArray();
			Arrays.stream(src.getEntries()).sequential().forEach(p -> array.add(p.get(0)));
			
			obj.add("data", array);
			
			return obj;
		}
		
	}
}
