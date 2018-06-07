package org.snowjak.rays.spectrum.distribution;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;
import org.snowjak.rays.TriFunction;

/**
 * Implements {@link SpectralPowerDistribution} via a table of wavelength/power
 * measurements. By default, the intervals between measurements are "filled in"
 * with linear interpolation ({@link BlendMethod#LINEAR}).
 * 
 * @author snowjak88
 *
 */
public class TabulatedSpectralPowerDistribution extends TabulatedDistribution<Double>
		implements SpectralPowerDistribution {
	
	/**
	 * Construct an empty, 0-energy TabulatedSpectralPowerDistribution.
	 */
	public TabulatedSpectralPowerDistribution() {
		
		this(Collections.emptyNavigableMap());
	}
	
	/**
	 * Construct a new TabulatedSpectralPowerDistribution, using a linear
	 * {@link BlendMethod}. We assume that the given <code>distribution</code> is
	 * composed of:
	 * <ul>
	 * <li>(keys) wavelength (nm)</li>
	 * <li>(values) power (W/sr*m^2)</li>
	 * </ul>
	 * 
	 * @param distribution
	 */
	public TabulatedSpectralPowerDistribution(NavigableMap<Double, Double> distribution) {
		
		super(distribution);
	}
	
	/**
	 * Construct a new TabulatedSpectralPowerDistribution, using the given
	 * {@link BlendMethod}. We assume that the given <code>distribution</code> is
	 * composed of:
	 * <ul>
	 * <li>(keys) wavelength (nm)</li>
	 * <li>(values) power (W/sr*m^2)</li>
	 * </ul>
	 * 
	 * @param distribution
	 * @param blend
	 */
	public TabulatedSpectralPowerDistribution(NavigableMap<Double, Double> distribution, BlendMethod<Double> blend) {
		
		super(distribution, blend);
	}
	
	@Override
	public TriFunction<Double, Double, Double, Double> getLinearInterpolationFunction() {
		
		return (p1, p2, fraction) -> (p2 - p1) * fraction + p1;
	}
	
	@Override
	public Pair<Double, Double> parseEntry(String csvLine) {
		
		try {
			final List<Double> pieces = Arrays.stream(csvLine.split(",")).map(s -> Double.parseDouble(s))
					.collect(Collectors.toList());
			
			return new Pair<>(pieces.get(0), pieces.get(1));
		} catch (Throwable t) {
			throw new RuntimeException("Could not parse CSV-line \"" + csvLine + "\"!", t);
		}
	}
	
	public static class TabulatedSpectralPowerDistributionLoader {
		
		public static <T> void save(OutputStream csv, NavigableMap<Double, T> table,
				Function<T, List<Double>> piecePicker) {
			
			try (var writer = new BufferedWriter(new OutputStreamWriter(csv))) {
				
				final var lines = table.entrySet().stream()
						.map(e -> Double.toString(e.getKey()) + "," + piecePicker.apply(e.getValue()).stream()
								.map(d -> Double.toString(d)).collect(Collectors.joining(",")))
						.collect(Collectors.toList());
				
				for (String line : lines)
					writer.write(line);
				
			} catch (Throwable t) {
				throw new RuntimeException("Could not save tabulated spectral power-distribution!", t);
			}
			
		}
	}
	
	@Override
	public String writeEntry(Double key, Double entry) {
		
		return Arrays.asList(key, entry).stream().map(d -> Double.toString(d)).collect(Collectors.joining(","));
	}
	
}
