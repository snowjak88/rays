package org.snowjak.rays.sampler;

import static org.apache.commons.math3.util.FastMath.floor;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StratifiedSamplerTest {
	
	@Test
	public void testSamplingRange() {
		
		final var sampler = new StratifiedSampler(0, 0, 32, 32, 4, 3, 4);
		
		final int[][][][] sampleCounts = new int[sampler.getXEnd() - sampler.getXStart() + 1][sampler.getYEnd()
				- sampler.getYStart() + 1][2][2];
		int sampleCount = 0;
		
		while (sampler.hasNextSample()) {
			sampleCount++;
			final var s = sampler.getNextSample();
			
			assertNotNull(s);
			
			final int filmX = (int) s.getFilmPoint().getX(), filmY = (int) s.getFilmPoint().getY();
			assertTrue("Film-X (" + filmX + ") not in range.", (filmX >= 0) && (filmX < sampleCounts.length));
			assertTrue("Film-Y (" + filmY + ") not in range.", (filmY >= 0) && (filmY < sampleCounts[filmX].length));
			
			final double subPixelX = s.getFilmPoint().getX() - floor(s.getFilmPoint().getX()),
					subPixelY = s.getFilmPoint().getY() - floor(s.getFilmPoint().getY());
			
			sampleCounts[filmX][filmY][(int) (subPixelX * 2.0)][(int) (subPixelY * 2.0)]++;
			
			final double lensU = s.getLensUV().getX(), lensV = s.getLensUV().getY();
			assertTrue("Lens-U (" + Double.toString(lensU) + ") not in range.", (lensU >= 0d) && (lensU <= 1d));
			assertTrue("Lens-V (" + Double.toString(lensV) + ") not in range.", (lensV >= 0d) && (lensV <= 1d));
		}
		
		assertEquals("Not expected number of samples generated.", (sampler.getXEnd() - sampler.getXStart() + 1)
				* (sampler.getYEnd() - sampler.getYStart() + 1) * (sampler.getSamplesPerPixel()), sampleCount);
		
		for (int x = 0; x < sampleCounts.length; x++)
			for (int y = 0; y < sampleCounts[x].length; y++)
				for (int sx = 0; sx < 2; sx++)
					for (int sy = 0; sy < 2; sy++)
						assertEquals(
								"Not enough samples generated for pixel (" + x + "," + y + " [" + sx + "," + sy + "]).",
								1, sampleCounts[x][y][sx][sy]);
	}
	
}
