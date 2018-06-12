package org.snowjak.rays.spectrum;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import org.snowjak.rays.Settings;
import org.snowjak.rays.Util;
import org.snowjak.rays.geometry.util.Pair;
import org.snowjak.rays.spectrum.colorspace.RGB;
import org.snowjak.rays.spectrum.colorspace.XYZ;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;
import org.snowjak.rays.spectrum.distribution.TabulatedDistribution;
import org.snowjak.rays.spectrum.distribution.TabulatedSpectralPowerDistribution;

/**
 * Represents a {@link Spectrum} that's backed by a
 * {@link TabulatedSpectralPowerDistribution}.
 * 
 * @author snowjak88
 *
 */
public class TabulatedSpectrum extends TabulatedSpectralPowerDistribution implements Spectrum {
	
	private static final long serialVersionUID = 5238589914392913471L;
	
	public TabulatedSpectrum() {
		
		super();
	}
	
	@SuppressWarnings("unchecked")
	public TabulatedSpectrum(SpectralPowerDistribution spd) {
		
		super();
		
		if (TabulatedDistribution.class.isAssignableFrom(spd.getClass()))
			((TabulatedDistribution<Double>) spd).getAll().forEach(p -> this.addEntry(p.getKey(), p.getValue()));
		else
			spd.toTabulatedForm(TabulatedSpectralPowerDistribution::new, spd.getLowestWavelength(),
					spd.getHighestWavelength(), Settings.getInstance().getCieXyzIntegrationStepCount()).getAll()
					.forEach(p -> this.addEntry(p.getKey(), p.getValue()));
	}
	
	public TabulatedSpectrum(Map<Double, Double> distribution, BlendMethod<Double> blend) {
		
		super(distribution, blend);
	}
	
	public TabulatedSpectrum(Map<Double, Double> distribution) {
		
		super(distribution);
	}
	
	@Override
	public boolean isBlack() {
		
		return !(getAll().stream().anyMatch(p -> p.getValue() > Settings.getInstance().getDoubleEqualityEpsilon()));
	}
	
	private TabulatedSpectrum apply(DoubleUnaryOperator operator) {
		
		return new TabulatedSpectrum(this.getTable().entrySet().stream()
				.collect(Collectors.toMap(e -> e.getKey(), e -> operator.applyAsDouble(e.getValue()))));
	}
	
	private TabulatedSpectrum apply(TabulatedSpectrum other, DoubleBinaryOperator operator, double defaultIfMissing) {
		
		final Set<Double> keys = new HashSet<>();
		keys.addAll(this.getTable().keySet());
		keys.addAll(other.getTable().keySet());
		
		return new TabulatedSpectrum(keys.stream()
				.collect(Collectors.toMap(key -> key,
						key -> operator.applyAsDouble(this.getTable().getOrDefault(key, defaultIfMissing),
								other.getTable().getOrDefault(key, defaultIfMissing)))));
	}
	
	/**
	 * Take this TabulatedSpectrum and average it into a new TabulatedSpectrum
	 * containing <code>newEntryCount</code> entries.
	 * <p>
	 * For example:
	 * 
	 * <pre>
	 *          s := { 0.0 : 100.0, 1.0 : 50.0, 2.0 : 0.0 }
	 * 
	 * average(3) := { 0.0 : 100.0, 1.0 : 50.0, 2.0 : 30.0 }
	 * average(2) := { 0.5 :  75.0, 1.5 : 40.0 }
	 * average(1) := { 1.0 :  60.0 }
	 * </pre>
	 * </p>
	 * 
	 * @param newEntryCount
	 * @return
	 */
	public TabulatedSpectrum average(int newEntryCount) {
		
		if (newEntryCount >= this.getTable().size())
			return this;
		
		if (newEntryCount <= 0)
			return new TabulatedSpectrum();
		
		final var intervalSize = (this.getHighestWavelength() - this.getLowestWavelength())
				/ ((double) this.getTable().size() - 1d);
		
		//@formatter:off
		return new TabulatedSpectrum(DoubleStream
				.iterate(this.getLowestWavelength(), d -> d < this.getHighestWavelength(), d -> d + intervalSize)
				.mapToObj(intervalStart ->
						new Pair(intervalStart + intervalSize / 2d,
								this.averageOver(intervalStart, intervalStart + intervalSize)))
				.collect(Collectors.toMap(
						(Pair p) -> p.get(0),
						(Pair p) -> p.get(1))));
		//@formatter:on
	}
	
	@Override
	public TabulatedSpectrum add(Spectrum addend) {
		
		return this.apply(new TabulatedSpectrum(addend), (d1, d2) -> d1 + d2, 0d);
	}
	
	@Override
	public TabulatedSpectrum multiply(Spectrum multiplicand) {
		
		return this.apply(new TabulatedSpectrum(multiplicand), (d1, d2) -> d1 * d2, 0d);
	}
	
	@Override
	public TabulatedSpectrum multiply(double scalar) {
		
		return this.apply(d -> d * scalar);
	}
	
	@Override
	public double getAmplitude() {
		
		return Util.integrate(getLowestWavelength(), getHighestWavelength(),
				Settings.getInstance().getCieXyzIntegrationStepCount(), (l) -> get(l));
	}
	
	@Override
	public RGB toRGB() {
		
		return XYZ.fromSpectrum(this).to(RGB.class);
	}
	
}
