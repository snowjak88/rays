package org.snowjak.rays.spectrum.distribution;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.stream.IntStream;

import org.junit.Test;
import org.snowjak.rays.Settings;
import org.snowjak.rays.geometry.util.Point;
import org.snowjak.rays.spectrum.colorspace.RGB;
import org.snowjak.rays.spectrum.colorspace.XYZ;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;

public class SpectralPowerDistributionTest {
	
	@Test
	public void testIsBlack_zeroed() {
		
		final var spd = new SpectralPowerDistribution();
		
		assertTrue(spd.isBlack());
	}
	
	@Test
	public void testIsBlack_nonZeroed() {
		
		final var spd = new SpectralPowerDistribution(0d, 2d, new Point[] { new Point(0), new Point(2), new Point(0) });
		
		assertFalse(spd.isBlack());
	}
	
	@Test
	public void testAdd() {
		
		final var values1 = new Point[Settings.getInstance().getSpectrumBinCount()];
		Arrays.fill(values1, new Point());
		values1[1] = new Point(1);
		values1[2] = new Point(2);
		
		final var values2 = new Point[Settings.getInstance().getSpectrumBinCount()];
		Arrays.fill(values2, new Point());
		values2[0] = new Point(1);
		values2[1] = new Point(2);
		values2[2] = new Point(3);
		
		final var spd1 = new SpectralPowerDistribution(values1);
		final var spd2 = new SpectralPowerDistribution(values2);
		
		final var sum = (SpectralPowerDistribution) spd1.add(spd2);
		assertEquals(Settings.getInstance().getSpectrumBinCount(), sum.size());
		
		assertEquals(Settings.getInstance().getSpectrumRangeLow(), sum.getBounds().get().getFirst(), 0.00001);
		assertEquals(Settings.getInstance().getSpectrumRangeHigh(), sum.getBounds().get().getSecond(), 0.00001);
		
		assertEquals(1d, sum.get(Settings.getInstance().getSpectrumRangeLow() + 0d * sum.getIndexStep()).get(0),
				0.00001);
		assertEquals(3d, sum.get(Settings.getInstance().getSpectrumRangeLow() + 1d * sum.getIndexStep()).get(0),
				0.00001);
		assertEquals(5d, sum.get(Settings.getInstance().getSpectrumRangeLow() + 2d * sum.getIndexStep()).get(0),
				0.00001);
	}
	
	@Test
	public void testMultiplySpectrum() {
		
		final var values1 = new Point[Settings.getInstance().getSpectrumBinCount()];
		Arrays.fill(values1, new Point());
		values1[1] = new Point(1);
		values1[2] = new Point(2);
		
		final var values2 = new Point[Settings.getInstance().getSpectrumBinCount()];
		Arrays.fill(values2, new Point());
		values2[0] = new Point(1);
		values2[1] = new Point(2);
		values2[2] = new Point(3);
		
		final var spd1 = new SpectralPowerDistribution(values1);
		final var spd2 = new SpectralPowerDistribution(values2);
		
		final var product = (SpectralPowerDistribution) spd1.multiply(spd2);
		
		assertEquals(Settings.getInstance().getSpectrumBinCount(), product.getTable().size());
		
		assertEquals(Settings.getInstance().getSpectrumRangeLow(), product.getBounds().get().getFirst(), 0.00001);
		assertEquals(Settings.getInstance().getSpectrumRangeHigh(), product.getBounds().get().getSecond(), 0.00001);
		
		assertEquals(0d, product.get(Settings.getInstance().getSpectrumRangeLow() + 0d * product.getIndexStep()).get(0),
				0.00001);
		assertEquals(2d, product.get(Settings.getInstance().getSpectrumRangeLow() + 1d * product.getIndexStep()).get(0),
				0.00001);
		assertEquals(6d, product.get(Settings.getInstance().getSpectrumRangeLow() + 2d * product.getIndexStep()).get(0),
				0.00001);
	}
	
	@Test
	public void testMultiplyDouble() {
		
		final var values = new Point[Settings.getInstance().getSpectrumBinCount()];
		Arrays.fill(values, new Point());
		values[1] = new Point(1);
		values[2] = new Point(2);
		
		final var spd = new SpectralPowerDistribution(values);
		
		final var product = (SpectralPowerDistribution) spd.multiply(2d);
		
		assertEquals(Settings.getInstance().getSpectrumBinCount(), product.size());
		
		assertEquals(Settings.getInstance().getSpectrumRangeLow(), product.getBounds().get().getFirst(), 0.00001);
		assertEquals(Settings.getInstance().getSpectrumRangeHigh(), product.getBounds().get().getSecond(), 0.00001);
		
		assertEquals(0d, product.get(Settings.getInstance().getSpectrumRangeLow() + 0d * product.getIndexStep()).get(0),
				0.00001);
		assertEquals(2d, product.get(Settings.getInstance().getSpectrumRangeLow() + 1d * product.getIndexStep()).get(0),
				0.00001);
		assertEquals(4d, product.get(Settings.getInstance().getSpectrumRangeLow() + 2d * product.getIndexStep()).get(0),
				0.00001);
	}
	
	@Test
	public void testNormalize() {
		
		final var values = new Point[Settings.getInstance().getSpectrumBinCount()];
		Arrays.fill(values, new Point());
		values[1] = new Point(1);
		values[2] = new Point(2);
		
		final var spd = new SpectralPowerDistribution(values);
		
		final var norm = (SpectralPowerDistribution) spd.normalize();
		
		assertEquals(Settings.getInstance().getSpectrumBinCount(), norm.size());
		
		assertEquals(Settings.getInstance().getSpectrumRangeLow(), norm.getBounds().get().getFirst(), 0.00001);
		assertEquals(Settings.getInstance().getSpectrumRangeHigh(), norm.getBounds().get().getSecond(), 0.00001);
		
		assertEquals(0d, norm.get(Settings.getInstance().getSpectrumRangeLow() + 0d * norm.getIndexStep()).get(0),
				0.00001);
		assertEquals(0.5d, norm.get(Settings.getInstance().getSpectrumRangeLow() + 1d * norm.getIndexStep()).get(0),
				0.00001);
		assertEquals(1d, norm.get(Settings.getInstance().getSpectrumRangeLow() + 2d * norm.getIndexStep()).get(0),
				0.00001);
	}
	
	@Test
	public void testResize() {
		
		final var values = IntStream.range(0, 8).mapToObj(i -> new Point(i)).toArray(len -> new Point[len]);
		final var spd = new SpectralPowerDistribution(values).resize();
		
		assertEquals(Settings.getInstance().getSpectrumBinCount(), spd.size());
		
		assertEquals(Settings.getInstance().getSpectrumRangeLow(), spd.getBounds().get().getFirst(), 0.00001);
		assertEquals(Settings.getInstance().getSpectrumRangeHigh(), spd.getBounds().get().getSecond(), 0.00001);
		
		assertEquals(0d, Arrays.stream(spd.getEntries()).mapToDouble(p -> p.get(0)).min().getAsDouble(), 0.00001);
		assertEquals(7d, Arrays.stream(spd.getEntries()).mapToDouble(p -> p.get(0)).max().getAsDouble(), 0.00001);
		
	}
	
	@Test
	public void testFromRGB_RED() {
		
		final var expected = RGB.RED;
		final var spd = SpectralPowerDistribution.fromRGB(expected);
		final var result = spd.toRGB();
		
		assertEquals("RGB(R) not as expected.", expected.getRed(), result.getRed(), 0.05);
		assertEquals("RGB(G) not as expected.", expected.getGreen(), result.getGreen(), 0.05);
		assertEquals("RGB(B) not as expected.", expected.getBlue(), result.getBlue(), 0.05);
		
	}
	
	@Test
	public void testFromRGB_CYAN() {
		
		final var expected = new RGB(0d, 1d, 1d);
		final var spd = SpectralPowerDistribution.fromRGB(expected);
		final var result = spd.toRGB();
		
		assertEquals("RGB(R) not as expected.", expected.getRed(), result.getRed(), 0.05);
		assertEquals("RGB(G) not as expected.", expected.getGreen(), result.getGreen(), 0.05);
		assertEquals("RGB(B) not as expected.", expected.getBlue(), result.getBlue(), 0.05);
		
	}
	
	@Test
	public void testFromRGB_WHITE() {
		
		final var expected = RGB.WHITE;
		final var spd = SpectralPowerDistribution.fromRGB(expected);
		final var result = spd.toRGB();
		
		assertEquals("RGB(R) not as expected.", expected.getRed(), result.getRed(), 0.05);
		assertEquals("RGB(G) not as expected.", expected.getGreen(), result.getGreen(), 0.05);
		assertEquals("RGB(B) not as expected.", expected.getBlue(), result.getBlue(), 0.05);
		
	}
	
	@Test
	public void testFromRGB_BLACK() {
		
		final var expected = RGB.BLACK;
		final var spd = SpectralPowerDistribution.fromRGB(expected);
		final var result = spd.toRGB();
		
		assertEquals("RGB(R) not as expected.", expected.getRed(), result.getRed(), 0.05);
		assertEquals("RGB(G) not as expected.", expected.getGreen(), result.getGreen(), 0.05);
		assertEquals("RGB(B) not as expected.", expected.getBlue(), result.getBlue(), 0.05);
		
	}
	
	@Test
	public void testFromRGB_GRAY() {
		
		final var expected = new RGB(0.5, 0.5, 0.5);
		final var spd = SpectralPowerDistribution.fromRGB(expected);
		final var result = spd.toRGB();
		
		assertEquals("RGB(R) not as expected.", expected.getRed(), result.getRed(), 0.05);
		assertEquals("RGB(G) not as expected.", expected.getGreen(), result.getGreen(), 0.05);
		assertEquals("RGB(B) not as expected.", expected.getBlue(), result.getBlue(), 0.05);
		
	}
	
	@Test
	public void testToRGB() {
		
		final var d65 = Settings.getInstance().getIlluminatorSpectralPowerDistribution();
		final var rgb = d65.toRGB();
		final var expected = new XYZ(0.95047d, 1.0d, 1.08883d).to(RGB.class);
		
		assertEquals("RGB(R) not as expected.", expected.getRed(), rgb.getRed(), 0.05);
		assertEquals("RGB(G) not as expected.", expected.getGreen(), rgb.getGreen(), 0.05);
		assertEquals("RGB(B) not as expected.", expected.getBlue(), rgb.getBlue(), 0.05);
	}
	
	@Test
	public void testAverageOver() {
		
		final var spd = new SpectralPowerDistribution(1.0, 8.0, new Point[] { new Point(1.0), new Point(1.0),
				new Point(0.0), new Point(4.0), new Point(3.0), new Point(2.0), new Point(4.0), new Point(0.0) });
		
		assertEquals(1d, spd.averageOver(1.0, 2.0).get(0), 0.00001);
		assertEquals(0.5d, spd.averageOver(2.0, 3.0).get(0), 0.00001);
		assertEquals(0.25d, spd.averageOver(2.5, 3.0).get(0), 0.00001);
	}
	
}
