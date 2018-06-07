package org.snowjak.rays.spectrum.distribution;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;
import org.snowjak.rays.spectrum.distribution.TabulatedDistribution.NearestBlendMethod;

public class TabulatedSpectralPowerDistributionTest {
	
	@Test
	public void testLoadSuccess() throws IOException {
		
		TabulatedDistribution.loadFromCSV(TabulatedSpectralPowerDistribution::new,
				this.getClass().getClassLoader().getResourceAsStream("./cie-data/illuminator_d65.csv"));
		
	}
	
	@Test
	public void testLoadData_linear() throws IOException {
		
		final var spd = TabulatedDistribution.loadFromCSV(TabulatedSpectralPowerDistribution::new,
				this.getClass().getClassLoader().getResourceAsStream("./cie-data/illuminator_d65.csv"));
		
		assertEquals("Loaded power is not as expected!", 0.0341, spd.getPower(300.0), 0.0001);
		assertEquals("Loaded power is not as expected!", 0.19712, spd.getPower(300.5), 0.0001);
		assertEquals("Loaded power is not as expected!", 0.36014, spd.getPower(301.0), 0.0001);
		
	}
	
	@Test
	public void testLoadData_nearest() throws IOException {
		
		final var spd = TabulatedDistribution.loadFromCSV(TabulatedSpectralPowerDistribution::new,
				this.getClass().getClassLoader().getResourceAsStream("./cie-data/illuminator_d65.csv"));
		spd.setBlendMethod(new NearestBlendMethod<Double>());
		
		assertEquals("Loaded power is not as expected!", 0.0341, spd.getPower(300.0), 0.0001);
		assertEquals("Loaded power is not as expected!", 0.0341, spd.getPower(300.4), 0.0001);
		assertEquals("Loaded power is not as expected!", 0.36014, spd.getPower(300.6), 0.0001);
		assertEquals("Loaded power is not as expected!", 0.36014, spd.getPower(301.0), 0.0001);
		
	}
	
}
