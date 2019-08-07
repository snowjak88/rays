package org.snowjak.rays.sampler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.junit.Test;
import org.snowjak.rays.geometry.Point2D;

public class PseudorandomSamplerTest {
	
	@Test
	public void testSamplingRange() {
		
		final var sampler = new PseudorandomSampler(0, 0, 32, 32, 3, 3, 4);
		
		final int[][] sampleCounts = new int[sampler.getXEnd() - sampler.getXStart() + 1][sampler.getYEnd()
				- sampler.getYStart() + 1];
		int sampleCount = 0;
		
		while (sampler.hasNextSample()) {
			sampleCount++;
			final var s = sampler.getNextSample();
			
			assertNotNull(s);
			
			final int filmX = (int) s.getFilmPoint().getX(), filmY = (int) s.getFilmPoint().getY();
			assertTrue("Film-X (" + filmX + ") not in range.", (filmX >= 0) && (filmX < sampleCounts.length));
			assertTrue("Film-Y (" + filmY + ") not in range.", (filmY >= 0) && (filmY < sampleCounts[filmX].length));
			
			sampleCounts[filmX][filmY]++;
			
			final double lensU = s.getLensUV().getX(), lensV = s.getLensUV().getY();
			assertTrue("Lens-U (" + Double.toString(lensU) + ") not in range.", (lensU >= 0d) && (lensU <= 1d));
			assertTrue("Lens-V (" + Double.toString(lensV) + ") not in range.", (lensV >= 0d) && (lensV <= 1d));
			
			final var additional1DSamples = new HashSet<Double>();
			do {
				//
			} while ((additional1DSamples.add(s.getAdditional1DSample())));
			
			final var additional2DSamples = new HashSet<Point2D>();
			do {
				//
			} while ((additional2DSamples.add(s.getAdditional2DSample())));
		}
		
		assertEquals("Not expected number of samples generated.", (sampler.getXEnd() - sampler.getXStart() + 1)
				* (sampler.getYEnd() - sampler.getYStart() + 1) * (sampler.getSamplesPerPixel()), sampleCount);
		
		for (int x = 0; x < sampleCounts.length; x++)
			for (int y = 0; y < sampleCounts[x].length; y++)
				assertEquals("Not enough samples generated for pixel (" + x + "," + y + ").",
						sampler.getSamplesPerPixel(), sampleCounts[x][y]);
	}
	
}
