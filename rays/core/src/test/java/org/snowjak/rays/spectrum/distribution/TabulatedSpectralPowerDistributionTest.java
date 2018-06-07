package org.snowjak.rays.spectrum.distribution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;
import org.snowjak.rays.spectrum.distribution.TabulatedDistribution.NearestBlendMethod;

public class TabulatedSpectralPowerDistributionTest {
	
	@Test
	public void testLoadFromCSV() throws IOException {
		
		final var testInputStream = new ByteArrayInputStream(
				("1.0,2.0" + System.lineSeparator() + "2.0,4.0").getBytes());
		
		final var d = TabulatedDistribution.loadFromCSV(TabulatedSpectralPowerDistribution::new, testInputStream);
		
		assertEquals("Distribution did not have expected number of entries!", 2, d.getAll().size());
		
		assertNotNull("Distribution did not have entry for (1.0)!", d.get(1.0));
		assertEquals("Distribution (1.0).(0) is not as expected!", 2.0, d.get(1.0), 0.00001);
		
		assertNotNull("Distribution did not have entry for (2.0)!", d.get(2.0));
		assertEquals("Distribution (2.0).(0) is not as expected!", 4.0, d.get(2.0), 0.00001);
		
	}
	
	@Test
	public void testSaveToCSV() throws IOException {
		
		final var expectedOutput = (Double.toString(1.0) + "," + Double.toString(2.0) + System.lineSeparator()
				+ Double.toString(2.0) + "," + Double.toString(4.0));
		
		final var testOuputStream = new ByteArrayOutputStream();
		
		final var d = new TabulatedSpectralPowerDistribution();
		d.addEntry(1.0, 2.0);
		d.addEntry(2.0, 4.0);
		
		d.saveToCSV(testOuputStream);
		
		assertEquals("Output stream was not written to as expected!", expectedOutput, testOuputStream.toString());
	}
	
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
	
	@Test
	public void testParseEntryString() {
		
		final var p = new TabulatedSpectralPowerDistribution().parseEntry("1.0,2.0");
		
		assertEquals("Parsed key was not as expected!", 1.0, p.getKey(), 0.00001);
		assertEquals("Parsed value was not as expected!", 2.0, p.getValue(), 0.00001);
	}
	
	@Test
	public void testWriteEntryDoubleTriplet() {
		
		final String expected = Double.toString(1.0) + "," + Double.toString(2.0);
		final String actual = new TabulatedSpectralPowerDistribution().writeEntry(1.0, 2.0);
		
		assertEquals("Written entry was not as expected!", expected, actual);
	}
	
}
