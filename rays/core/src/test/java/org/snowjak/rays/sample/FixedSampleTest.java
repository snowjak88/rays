package org.snowjak.rays.sample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.snowjak.rays.Settings;
import org.snowjak.rays.geometry.Point2D;

public class FixedSampleTest {
	
	@Test
	public void testGetAdditional1DSample() {
		
		final List<Double> additionalSamples = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);
		final FixedSample fixedSample = new FixedSample();
		fixedSample.setAdditional1DSamples(additionalSamples);
		
		for (int i = 0; i < 5; i++) {
			
			final Set<Double> found = new HashSet<>();
			found.addAll(additionalSamples);
			
			for (int j = 0; j < additionalSamples.size(); j++) {
				final Double sampleValue = fixedSample.getAdditional1DSample();
				
				assertTrue(
						"FixedSample provided additional 1-D value (" + sampleValue.toString()
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
		final FixedSample fixedSample = new FixedSample();
		fixedSample.setAdditional2DSamples(additionalSamples);
		
		for (int i = 0; i < 5; i++) {
			
			final Set<Point2D> found = new HashSet<>();
			found.addAll(additionalSamples);
			
			for (int j = 0; j < additionalSamples.size(); j++) {
				final Point2D sampleValue = fixedSample.getAdditional2DSample();
				
				assertTrue(
						"FixedSample provided additional 2-D value (" + sampleValue.toString()
								+ ") that is not in expected list (" + additionalSamples.toString() + ")!",
						found.contains(sampleValue));
				found.remove(sampleValue);
				
			}
			
			assertTrue("Not all expected sample-values were taken -- remaining (" + found.toString() + ")!",
					found.isEmpty());
			
		}
	}
	
	@Test
	public void testSerialization() {
		
		final var sample = new FixedSample(new Point2D(1, 2), new Point2D(0.25, 0.75), 1.0,
				Arrays.asList(1d, 2d, 3d, 4d),
				Arrays.asList(new Point2D(1, 2), new Point2D(2, 3), new Point2D(3, 4), new Point2D(4, 5)));
		
		final var expected = "{\"filmPoint\":{\"x\":1.0,\"y\":2.0},\"lensUV\":{\"x\":0.25,\"y\":0.75},\"t\":1.0,\"additional1DSamples\":[1.0,2.0,3.0,4.0],\"additional2DSamples\":[{\"x\":1.0,\"y\":2.0},{\"x\":2.0,\"y\":3.0},{\"x\":3.0,\"y\":4.0},{\"x\":4.0,\"y\":5.0}]}";
		
		final var result = Settings.getInstance().getGson().toJson(sample);
		
		assertEquals(expected, result);
	}
	
	@Test
	public void testDeserialization() {
		
		final var json = "{\"filmPoint\":{\"x\":1.0,\"y\":2.0},\"lensUV\":{\"x\":0.25,\"y\":0.75},\"t\":1.0,\"additional1DSamples\":[1.0,2.0,3.0,4.0],\"additional2DSamples\":[{\"x\":1.0,\"y\":2.0},{\"x\":2.0,\"y\":3.0},{\"x\":3.0,\"y\":4.0},{\"x\":4.0,\"y\":5.0}]}";
		
		final var expected = new FixedSample(new Point2D(1, 2), new Point2D(0.25, 0.75), 1.0,
				Arrays.asList(1d, 2d, 3d, 4d),
				Arrays.asList(new Point2D(1, 2), new Point2D(2, 3), new Point2D(3, 4), new Point2D(4, 5)));
		
		final var result = Settings.getInstance().getGson().fromJson(json, FixedSample.class);
		
		assertNotNull(result);
		
		assertEquals(expected.getFilmPoint().getX(), result.getFilmPoint().getX(), 0.00001);
		assertEquals(expected.getFilmPoint().getY(), result.getFilmPoint().getY(), 0.00001);
		
		assertEquals(expected.getLensUV().getX(), result.getLensUV().getX(), 0.00001);
		assertEquals(expected.getLensUV().getY(), result.getLensUV().getY(), 0.00001);
		
		assertEquals(expected.getT(), result.getT(), 0.00001);
		
		assertTrue(expected.getAdditional1DSamples().equals(result.getAdditional1DSamples()));
		assertTrue(expected.getAdditional2DSamples().equals(result.getAdditional2DSamples()));
	}
	
}
