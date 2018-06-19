package org.snowjak.rays.filter;

import static org.junit.Assert.*;

import org.junit.Test;
import org.snowjak.rays.geometry.Point2D;
import org.snowjak.rays.sample.Sample;

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
		
		final var s = new Sample();
		s.setFilmPoint(new Point2D(2.1, 2.1));
		
		assertTrue(f.isContributing(s, 2, 2));
		assertTrue(f.isContributing(s, 1, 0));
		assertFalse(f.isContributing(s, 2, 5));
	}
	
}
