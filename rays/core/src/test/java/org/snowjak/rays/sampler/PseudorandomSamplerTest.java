package org.snowjak.rays.sampler;

import static org.junit.Assert.*;

import org.junit.Test;

public class PseudorandomSamplerTest {
	
	@Test
	public void testSamplingRange() {
		
		final var sampler = new PseudorandomSampler(0, 0, 0, 1, 2, 3, 3, 4);
		
		final int[][] sampleCounts = new int[][] { { 0, 0, 0 }, { 0, 0, 0 } };
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
			
			assertEquals("Not expected number of additional 1D samples.", 3, s.getAdditional1DSamples().size());
			assertTrue("Not all 1D samples in range.",
					s.getAdditional1DSamples().stream().allMatch(v -> v >= 0d && v <= 1d));
			
			assertEquals("Not expected number of additional 2D samples.", 4, s.getAdditional2DSamples().size());
			assertTrue("Not all 2D samples in range.", s.getAdditional2DSamples().stream()
					.allMatch(p -> p.getX() >= 0d && p.getY() >= 0d && p.getX() <= 1d && p.getY() <= 1d));
		}
		
		assertEquals("Not expected number of samples generated.", 2 * 3 * 3, sampleCount);
		for (int x = 0; x < sampleCounts.length; x++)
			assertArrayEquals("Not enough samples generated in row (" + x + ").", new int[] { 3, 3, 3 },
					sampleCounts[x]);
	}
	
}
