package org.snowjak.rays.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.snowjak.rays.geometry.Point2D;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.sampler.PseudorandomSampler;
import org.snowjak.rays.sampler.Sampler;

public class FilterTest {
	
	@Test
	public void testIsContributing() {
		
		final var f = new Filter() {
			
			@Override
			public int getExtentX() {
				
				return 1;
			}
			
			@Override
			public int getExtentY() {
				
				return 2;
			}
			
			@Override
			public double getContribution(Sample sample, int pixelX, int pixelY) {
				
				return 0;
			}
			
		};
		
		@SuppressWarnings("serial")
		final var s = new Sample() {
			
			@Override
			public Point2D getFilmPoint() {
				
				return new Point2D(2.1, 2.1);
			}
			
			@Override
			public Point2D getLensUV() {
				
				return null;
			}
			
			@Override
			public double getT() {
				
				return 0;
			}
			
			@Override
			public Sampler getSampler() {
				
				return new PseudorandomSampler(0, 0, 3, 3, 1);
			}
			
		};
		
		assertTrue(f.isContributing(s, 2, 2));
		assertTrue(f.isContributing(s, 1, 0));
		assertFalse(f.isContributing(s, 2, 5));
	}
	
}
