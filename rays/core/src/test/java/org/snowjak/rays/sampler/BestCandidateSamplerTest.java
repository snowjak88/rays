package org.snowjak.rays.sampler;

import static org.apache.commons.math3.util.FastMath.pow;
import static org.apache.commons.math3.util.FastMath.sqrt;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Test;
import org.snowjak.rays.geometry.Point2D;

import com.google.common.math.Stats;

public class BestCandidateSamplerTest {
	
	@Test
	public void test() {
		
		final var sampler = new BestCandidateSampler(0, 0, 0, 32, 32, 3, 4, 5);
		
		final Collection<Point2D> filmPoints = new LinkedList<>();
		
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
			
			assertEquals("Not expected number of additional 1D samples.", sampler.getAdditional1DSamples(),
					s.getAdditional1DSamples().size());
			assertTrue("Not all 1D samples in range.",
					s.getAdditional1DSamples().stream().allMatch(v -> v >= 0d && v <= 1d));
			
			assertEquals("Not expected number of additional 2D samples.", sampler.getAdditional2DSamples(),
					s.getAdditional2DSamples().size());
			assertTrue("Not all 2D samples in range.", s.getAdditional2DSamples().stream()
					.allMatch(p -> p.getX() >= 0d && p.getY() >= 0d && p.getX() <= 1d && p.getY() <= 1d));
			
			filmPoints.add(s.getFilmPoint());
		}
		
		assertEquals("Not expected number of samples generated.", (sampler.getXEnd() - sampler.getXStart() + 1)
				* (sampler.getYEnd() - sampler.getYStart() + 1) * (sampler.getSamplesPerPixel()), sampleCount);
		
		for (int x = 0; x < sampleCounts.length; x++)
			for (int y = 0; y < sampleCounts[x].length; y++)
				assertEquals("Not enough samples generated for pixel (" + x + "," + y + ").",
						sampler.getSamplesPerPixel(), sampleCounts[x][y]);
			
		final var distanceStats = Stats
				.of(filmPoints.parallelStream().filter(p -> p != null)
						.flatMap(p1 -> filmPoints.stream().filter(p -> p != null).filter(p2 -> p1 != p2)
								.map(p2 -> sqrt(pow(p1.getX() - p2.getX(), 2) + pow(p1.getY() - p2.getY(), 2))))
						.iterator());
		
		System.out.println("Best-candidate sampler: minimum distance between any 2 points: " + distanceStats.min());
		System.out.println("Best-candidate sampler:    mean distance between any 2 points: " + distanceStats.mean());
		System.out.println("Best-candidate sampler: std.dev/distance between any 2 points: "
				+ distanceStats.populationStandardDeviation());
	}
	
}
