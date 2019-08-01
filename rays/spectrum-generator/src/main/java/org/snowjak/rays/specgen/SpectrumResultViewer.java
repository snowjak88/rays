/**
 * 
 */
package org.snowjak.rays.specgen;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.snowjak.rays.specgen.SpectrumGenerator.StatusReporter;
import org.snowjak.rays.spectrum.colorspace.RGB;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("viewer")
public class SpectrumResultViewer implements SpectrumSearch {
	
	@Override
	public Result doSearch(BiFunction<SpectralPowerDistribution, RGB, Result> distanceCalculator, RGB targetColor,
			Supplier<SpectralPowerDistribution> startingSpdSupplier, StatusReporter reporter) {
		
		final var result = distanceCalculator.apply(startingSpdSupplier.get(), targetColor);
		reporter.reportResult(result);
		
		return null;
	}
	
}
