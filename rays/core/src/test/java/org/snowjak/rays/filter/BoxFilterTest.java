package org.snowjak.rays.filter;

import static org.junit.Assert.*;
import org.junit.Test;
import org.snowjak.rays.geometry.Point2D;
import org.snowjak.rays.sample.Sample;

public class BoxFilterTest {
	
	@Test
	public void testGetContribution() {
		
		final var f = new BoxFilter(2);
		
		final var s = new Sample();
		s.setFilmPoint(new Point2D(2.1, 2.1));
		s.setSamplesPerPixel(1);
		
		assertEquals(1d / 4d, f.getContribution(s, 2, 2), 0.00001);
		assertEquals(0d, f.getContribution(s, 5, 2), 0.00001);
	}
	
}
