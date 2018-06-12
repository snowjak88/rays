package org.snowjak.rays.spectrum;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.snowjak.rays.Settings;
import org.snowjak.rays.spectrum.colorspace.RGB;

public class TabulatedSpectrumTest {
	
	@Test
	public void testIsBlack() {
		
		final var blackSpec = new TabulatedSpectrum();
		final var nonBlackSpec = new TabulatedSpectrum();
		nonBlackSpec.addEntry(1.0, 1.0);
		
		assertFalse(nonBlackSpec.isBlack());
		assertTrue(blackSpec.isBlack());
	}
	
	@Test
	public void testAverage() {
		
		final var spec = new TabulatedSpectrum();
		spec.addEntry(0.0, 100.0);
		spec.addEntry(1.0, 50.0);
		spec.addEntry(2.0, 30.0);
		
		final var avgSpec = spec.average(2);
		assertEquals(2, avgSpec.getAll().size());
		assertEquals("avgSpec.lowestWavelength is not as expected!", 0.5d, avgSpec.getLowestWavelength(), 0.00001);
		assertEquals("avgSpec.highestWavelength is not as expected!", 1.5d, avgSpec.getHighestWavelength(), 0.00001);
		assertEquals("avgSpec(0.5) is not as expected!", 75d, avgSpec.get(0.5d), 0.00001);
		assertEquals("avgSpec(1.5) is not as expected!", 40d, avgSpec.get(1.5d), 0.00001);
	}
	
	@Test
	public void testAdd() {
		
		final var d1 = new TabulatedSpectrum();
		d1.addEntry(0d, 1d);
		d1.addEntry(1d, 2d);
		d1.addEntry(2d, 3d);
		
		final var d2 = new TabulatedSpectrum();
		d2.addEntry(0d, 1d);
		d2.addEntry(2d, 2d);
		d2.addEntry(4d, 3d);
		
		final var sum = d1.add(d2);
		
		assertEquals(4, sum.getAll().size());
		assertEquals("sum.lowestWavelength is not as expected!", 0d, sum.getLowestWavelength(), 0.00001);
		assertEquals("sum.highestWavelength is not as expected!", 4d, sum.getHighestWavelength(), 0.00001);
		
		assertNotNull("sum(0d) should not be null!", sum.get(0d));
		assertEquals("sum(0d) is not as expected!", 2d, sum.get(0d), 0.00001);
		
		assertNotNull("sum(1d) should not be null!", sum.get(1d));
		assertEquals("sum(1d) is not as expected!", 2d, sum.get(1d), 0.00001);
		
		assertNotNull("sum(2d) should not be null!", sum.get(2d));
		assertEquals("sum(2d) is not as expected!", 5d, sum.get(2d), 0.00001);
		
		assertNotNull("sum(4d) should not be null!", sum.get(4d));
		assertEquals("sum(4d) is not as expected!", 3d, sum.get(4d), 0.00001);
	}
	
	@Test
	public void testMultiplyTabulatedSpectrum() {
		
		final var d1 = new TabulatedSpectrum();
		d1.addEntry(0d, 1d);
		d1.addEntry(1d, 2d);
		
		final var d2 = new TabulatedSpectrum();
		d2.addEntry(0d, 1d);
		d2.addEntry(2d, 2d);
		
		final var product = d1.multiply(d2);
		
		assertEquals(3, product.getAll().size());
		assertEquals("sum.lowestWavelength is not as expected!", 0d, product.getLowestWavelength(), 0.00001);
		assertEquals("sum.highestWavelength is not as expected!", 2d, product.getHighestWavelength(), 0.00001);
		
		assertNotNull("sum(0d) should not be null!", product.get(0d));
		assertEquals("sum(0d) is not as expected!", 1d, product.get(0d), 0.00001);
		
		assertNotNull("sum(1d) should not be null!", product.get(1d));
		assertEquals("sum(1d) is not as expected!", 0d, product.get(1d), 0.00001);
		
		assertNotNull("sum(2d) should not be null!", product.get(2d));
		assertEquals("sum(2d) is not as expected!", 0d, product.get(2d), 0.00001);
	}
	
	@Test
	public void testMultiplyDouble() {
		
		final var d = new TabulatedSpectrum();
		d.addEntry(0d, 1d);
		d.addEntry(1d, 2d);
		d.addEntry(2d, 4d);
		
		final var product = d.multiply(3d);
		
		assertEquals(3, product.getAll().size());
		assertEquals("sum.lowestWavelength is not as expected!", 0d, product.getLowestWavelength(), 0.00001);
		assertEquals("sum.highestWavelength is not as expected!", 2d, product.getHighestWavelength(), 0.00001);
		
		assertNotNull("sum(0d) should not be null!", product.get(0d));
		assertEquals("sum(0d) is not as expected!", 3d, product.get(0d), 0.00001);
		
		assertNotNull("sum(1d) should not be null!", product.get(1d));
		assertEquals("sum(1d) is not as expected!", 6d, product.get(1d), 0.00001);
		
		assertNotNull("sum(2d) should not be null!", product.get(2d));
		assertEquals("sum(2d) is not as expected!", 12d, product.get(2d), 0.00001);
	}
	
	@Test
	public void testGetAmplitude() {
		
		final var d = new TabulatedSpectrum();
		d.addEntry(0d, 1d);
		d.addEntry(1d, 2d);
		d.addEntry(2d, 1d);
		
		assertEquals(3d, d.getAmplitude(), 0.00001);
	}
	
	@Test
	public void testToRGB() throws IOException {
		
		final var d65 = new TabulatedSpectrum(Settings.getInstance().getIlluminatorSpectralPowerDistribution());
		
		final var rgb = d65.toRGB();
		final var expected = new RGB(1d, 1d, 1d);
		
		assertNotNull(rgb);
		assertEquals("RGB(R) not as expected!", expected.getRed(), rgb.getRed(), 0.0001);
		assertEquals("RGB(G) not as expected!", expected.getGreen(), rgb.getGreen(), 0.0001);
		assertEquals("RGB(B) not as expected!", expected.getBlue(), rgb.getBlue(), 0.0001);
	}
	
}
