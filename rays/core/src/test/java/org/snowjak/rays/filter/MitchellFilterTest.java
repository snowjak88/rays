package org.snowjak.rays.filter;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.snowjak.rays.geometry.Point2D;
import org.snowjak.rays.sample.Sample;

public class MitchellFilterTest {
	
	@Test
	public void testGetContribution() {
		
		final var f = new MitchellFilter(0, 0.5, 0.25);
		
		@SuppressWarnings("serial")
		final var s = new Sample() {
			
			@Override
			public Point2D getFilmPoint() {
				
				return new Point2D(2.625, 2.625);
			}
			
			@Override
			public Point2D getLensUV() {
				
				return null;
			}
			
			@Override
			public double getT() {
				
				return 0;
			}
			
		};
		
		assertEquals(0.54698350694, f.getContribution(s, 2, 2), 0.00001);
		assertEquals(0d, f.getContribution(s, 5, 2), 0.00001);
	}
	
	@Test
	public void testGetContribution2() {
		
		final var f = new MitchellFilter(0, 0.5, 0.25);
		
		@SuppressWarnings("serial")
		final var s = new Sample() {
			
			@Override
			public Point2D getFilmPoint() {
				
				return new Point2D(2.25, 2.25);
			}
			
			@Override
			public Point2D getLensUV() {
				
				return null;
			}
			
			@Override
			public double getT() {
				
				return 0;
			}
			
		};
		
		assertEquals(0.073350694, f.getContribution(s, 2, 2), 0.00001);
		assertEquals(0d, f.getContribution(s, 5, 2), 0.00001);
	}
	
}
