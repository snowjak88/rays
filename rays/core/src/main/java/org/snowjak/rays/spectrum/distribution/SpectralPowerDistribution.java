package org.snowjak.rays.spectrum.distribution;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Pattern;

import org.apache.commons.math3.util.Pair;
import org.snowjak.rays.Settings;
import org.snowjak.rays.geometry.util.Point;
import org.snowjak.rays.spectrum.Spectrum;
import org.snowjak.rays.spectrum.colorspace.RGB;
import org.snowjak.rays.spectrum.colorspace.XYZ;

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
		
		return this.resize(
				new Pair<>(Settings.getInstance().getSpectrumRangeLow(), Settings.getInstance().getSpectrumRangeHigh()),
				Settings.getInstance().getSpectrumBinCount());
	}
	
	@Override
	public RGB toRGB() {
		
		return XYZ.fromSpectrum(this).to(RGB.class);
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
	
}
