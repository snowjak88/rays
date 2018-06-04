package org.snowjak.rays.sample;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.snowjak.rays.geometry.Point2D;

public class SampleTest {
	
	@Test
	public void testGetAdditional1DSample() {
		
		final List<Double> additionalSamples = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);
		final Sample sample = new Sample();
		sample.setAdditional1DSamples(additionalSamples);
		
		for (int i = 0; i < 5; i++) {
			
			final Set<Double> found = new HashSet<>();
			found.addAll(additionalSamples);
			
			for (int j = 0; j < additionalSamples.size(); j++) {
				final Double sampleValue = sample.getAdditional1DSample();
				
				assertTrue(
						"Sample provided additional 1-D value (" + sampleValue.toString()
								+ ") that is not in expected list (" + additionalSamples.toString() + ")!",
						found.contains(sampleValue));
				found.remove(sampleValue);
				
			}
			
			assertTrue("Not all expected sample-values were taken -- remaining (" + found.toString() + ")!",
					found.isEmpty());
			
		}
		
	}
	
	@Test
	public void testGetAdditional2DSample() {
		
		final List<Point2D> additionalSamples = Arrays.asList(new Point2D(1.0, 1.0), new Point2D(2.0, 2.0),
				new Point2D(3.0, 3.0), new Point2D(4.0, 4.0), new Point2D(5.0, 5.0));
		final Sample sample = new Sample();
		sample.setAdditional2DSamples(additionalSamples);
		
		for (int i = 0; i < 5; i++) {
			
			final Set<Point2D> found = new HashSet<>();
			found.addAll(additionalSamples);
			
			for (int j = 0; j < additionalSamples.size(); j++) {
				final Point2D sampleValue = sample.getAdditional2DSample();
				
				assertTrue(
						"Sample provided additional 2-D value (" + sampleValue.toString()
								+ ") that is not in expected list (" + additionalSamples.toString() + ")!",
						found.contains(sampleValue));
				found.remove(sampleValue);
				
			}
			
			assertTrue("Not all expected sample-values were taken -- remaining (" + found.toString() + ")!",
					found.isEmpty());
			
		}
	}
	
}
