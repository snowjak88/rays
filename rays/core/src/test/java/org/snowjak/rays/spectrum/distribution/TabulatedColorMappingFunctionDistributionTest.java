package org.snowjak.rays.spectrum.distribution;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;
import org.snowjak.rays.geometry.util.Triplet;

public class TabulatedColorMappingFunctionDistributionTest {
	
	@Test
	public void testLoadFromCSV() throws IOException {
		
		final var testInputStream = new ByteArrayInputStream(
				("1.0,2.0,3.0,4.0" + System.lineSeparator() + "2.0,4.0,3.0,2.0").getBytes());
		
		final var d = TabulatedDistribution.loadFromCSV(TabulatedColorMappingFunctionDistribution::new,
				testInputStream);
		
		assertEquals("Distribution did not have expected number of entries!", 2, d.getAll().size());
		
		assertNotNull("Distribution did not have entry for (1.0)!", d.get(1.0));
		assertEquals("Distribution (1.0).(0) is not as expected!", 2.0, d.get(1.0).get(0), 0.00001);
		assertEquals("Distribution (1.0).(1) is not as expected!", 3.0, d.get(1.0).get(1), 0.00001);
		assertEquals("Distribution (1.0).(2) is not as expected!", 4.0, d.get(1.0).get(2), 0.00001);
		
		assertNotNull("Distribution did not have entry for (2.0)!", d.get(2.0));
		assertEquals("Distribution (2.0).(0) is not as expected!", 4.0, d.get(2.0).get(0), 0.00001);
		assertEquals("Distribution (2.0).(1) is not as expected!", 3.0, d.get(2.0).get(1), 0.00001);
		assertEquals("Distribution (2.0).(2) is not as expected!", 2.0, d.get(2.0).get(2), 0.00001);
		
	}
	
	@Test
	public void testSaveToCSV() throws IOException {
		
		final var expectedOutput = (Double.toString(1.0) + "," + Double.toString(2.0) + "," + Double.toString(3.0) + ","
				+ Double.toString(4.0) + System.lineSeparator() + Double.toString(2.0) + "," + Double.toString(4.0)
				+ "," + Double.toString(3.0) + "," + Double.toString(2.0));
		
		final var testOuputStream = new ByteArrayOutputStream();
		
		final var d = new TabulatedColorMappingFunctionDistribution();
		d.addEntry(1.0, new Triplet(2.0, 3.0, 4.0));
		d.addEntry(2.0, new Triplet(4.0, 3.0, 2.0));
		
		d.saveToCSV(testOuputStream);
		
		assertEquals("Output stream was not written to as expected!", expectedOutput, testOuputStream.toString());
	}
	
	@Test
	public void testParseEntryString() {
		
		final var p = new TabulatedColorMappingFunctionDistribution().parseEntry("1.0,2.0,3.0,4.0");
		
		assertEquals("Parsed key was not as expected!", 1.0, p.getKey(), 0.00001);
		assertEquals("Parsed value(0) was not as expected!", 2.0, p.getValue().get(0), 0.00001);
		assertEquals("Parsed value(1) was not as expected!", 3.0, p.getValue().get(1), 0.00001);
		assertEquals("Parsed value(2) was not as expected!", 4.0, p.getValue().get(2), 0.00001);
	}
	
	@Test
	public void testWriteEntryDoubleTriplet() {
		
		final String expected = Double.toString(1.0) + "," + Double.toString(2.0) + "," + Double.toString(3.0) + ","
				+ Double.toString(4.0);
		final String actual = new TabulatedColorMappingFunctionDistribution().writeEntry(1.0,
				new Triplet(2.0, 3.0, 4.0));
		
		assertEquals("Written entry was not as expected!", expected, actual);
	}
	
}
