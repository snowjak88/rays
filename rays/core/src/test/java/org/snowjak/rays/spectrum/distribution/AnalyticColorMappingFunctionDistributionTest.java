package org.snowjak.rays.spectrum.distribution;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.stream.DoubleStream;

import org.junit.Test;

public class AnalyticColorMappingFunctionDistributionTest {
	
	@Test
	public void testGet() throws IOException {
		
		final var tabulatedCMF = TabulatedColorMappingFunctionDistribution
				.loadFromCSV(TabulatedColorMappingFunctionDistribution::new, this.getClass().getClassLoader()
						.getResourceAsStream("cie-data/CIE_XYZ_CMF_2-degree_1nm-step_1931.csv"));
		
		final var analyticCMF = new AnalyticColorMappingFunctionDistribution();
		
		DoubleStream
				.iterate(analyticCMF.getLowestWavelength(), d -> (d <= analyticCMF.getHighestWavelength()),
						d -> d += 1.0)
				.peek(lambda -> assertEquals("Tabulated and Analytic CMFs are not approximately equal (X)!",
						tabulatedCMF.get(lambda).get(0), analyticCMF.get(lambda).get(0), 0.05))
				.peek(lambda -> assertEquals("Tabulated and Analytic CMFs are not approximately equal (Y)!",
						tabulatedCMF.get(lambda).get(1), analyticCMF.get(lambda).get(1), 0.05))
				.forEach(lambda -> assertEquals("Tabulated and Analytic CMFs are not approximately equal (Z)!",
						tabulatedCMF.get(lambda).get(2), analyticCMF.get(lambda).get(2), 0.05));
	}
	
}
