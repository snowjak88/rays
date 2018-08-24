package org.snowjak.rays.film;

import static org.junit.Assert.assertEquals;

import java.awt.image.BufferedImage;

import org.junit.Test;
import org.snowjak.rays.filter.BoxFilter;
import org.snowjak.rays.sample.EstimatedSample;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.sampler.PseudorandomSampler;
import org.snowjak.rays.sampler.Sampler;
import org.snowjak.rays.spectrum.colorspace.RGB;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;

public class FilmTest {
	
	@Test
	public void test() {
		
		final var film = new Film(2, 2, new BoxFilter(0));
		final Sampler sampler = new PseudorandomSampler(0, 0, film.getWidth() - 1, film.getHeight() - 1, 2);
		
		while (sampler.hasNextSample()) {
			
			final Sample sample = sampler.getNextSample();
			
			final RGB rgb;
			if (sample.getFilmPoint().getX() < 1d && sample.getFilmPoint().getY() < 1d)
				rgb = RGB.RED;
			else if (sample.getFilmPoint().getX() >= 1d && sample.getFilmPoint().getY() < 1d)
				rgb = RGB.GREEN;
			else if (sample.getFilmPoint().getX() < 1d && sample.getFilmPoint().getY() >= 1d)
				rgb = RGB.BLUE;
			else
				rgb = RGB.WHITE;
			
			final EstimatedSample estimated = new EstimatedSample(sample, SpectralPowerDistribution.fromRGB(rgb));
			film.addSample(estimated);
			
		}
		
		final BufferedImage img = film.getImage(null).getBufferedImage();
		for (int x = 0; x < img.getWidth(); x++)
			for (int y = 0; y < img.getHeight(); y++) {
				
				final RGB expected;
				if (x == 0 && y == 0)
					expected = RGB.RED;
				else if (x == 1 && y == 0)
					expected = RGB.GREEN;
				else if (x == 0 && y == 1)
					expected = RGB.BLUE;
				else
					expected = RGB.WHITE;
				
				final RGB actual = RGB.fromPacked(img.getRGB(x, y));
				
				assertEquals("RGB(R) at [" + x + "," + y + "] not as expected!", expected.getRed(), actual.getRed(),
						0.05);
				assertEquals("RGB(G) at [" + x + "," + y + "] not as expected!", expected.getGreen(), actual.getGreen(),
						0.05);
				assertEquals("RGB(B) at [" + x + "," + y + "] not as expected!", expected.getBlue(), actual.getBlue(),
						0.05);
				
			}
	}
	
}
