package org.snowjak.rays.specgen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.Settings;
import org.snowjak.rays.spectrum.colorspace.RGB;
import org.snowjak.rays.spectrum.colorspace.XYZ;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SpectrumGenerator is an application that can pre-generate spectra
 * corresponding to different CIE-XYZ triplets.
 * <p>
 * Oftentimes, we will want to model spectrum-transport for more photo-realistic
 * rendering. However, we can rarely find colors and materials defined in terms
 * of their spectral power-distributions! Moreover, constructing arbitrary
 * power-distributions to fit existing color triplets (whether sRGB or CIE-XYZ)
 * is prohibitively time-intensive.
 * </p>
 * <p>
 * Accordingly, this application is intended to help you pre-generate arbitrary
 * spectral power-distributions, to be persisted into the file-system.
 * </p>
 * 
 * @author snowjak88
 *
 */
@SpringBootApplication
public class SpectrumGenerator implements CommandLineRunner {
	
	private static final Logger LOG = LoggerFactory.getLogger(SpectrumGenerator.class);
	
	public static void main(String[] args) {
		
		SpringApplication.run(SpectrumGenerator.class, args);
	}
	
	@Override
	public void run(String... args) throws Exception {
		
		final var directory = new File("./spectra/");
		if (!directory.exists())
			directory.mkdirs();
		
		runFor("white", 8, RGB.WHITE, new File(directory, "white.csv"));
		runFor("red", 8, RGB.RED, new File(directory, "red.csv"));
		runFor("green", 8, RGB.GREEN, new File(directory, "green.csv"));
		runFor("blue", 8, RGB.BLUE, new File(directory, "blue.csv"));
		
	}
	
	public void runFor(String name, int binCount, RGB rgb, File outputCsv)
			throws IOException, InterruptedException, ExecutionException {
		
		LOG.info("Generating a spectrum fit for: \"{}\" ({} / {})", name, rgb.toString(), rgb.to(XYZ.class).toString());
		
		final var result = new BruteForceSpectrumSearch(binCount, rgb.to(XYZ.class),
				Settings.getInstance().getIlluminatorSpectralPowerDistribution(), 0.1, new StatusReporter(name))
						.doSearch();
		
		LOG.info("{}: writing result to file.", name);
		
		LOG.info("{}: resulting RGB = {}", name, XYZ.fromSpectrum(result.getSpd()).to(RGB.class));
		LOG.info("{}: Distance: {}", name, result.getDistance());
		LOG.info("{}: Bumpiness: {}", name, result.getBumpiness());
		
		LOG.info("{}: Writing spectrum as CSV ...", name);
		try (var csv = new FileOutputStream(outputCsv)) {
			result.getSpd().saveToCSV(csv);
		} catch (IOException e) {
			LOG.error("{}: Could not write spectrum to {}: {}, \"{}\"", name, outputCsv.getPath(),
					e.getClass().getSimpleName(), e.getMessage());
		}
		
	}
	
	public static class StatusReporter {
		
		private static final Logger LOG = LoggerFactory.getLogger(StatusReporter.class);
		
		private final String name;
		
		private double bestDistance, bestBumpiness;
		private SpectralPowerDistribution bestSPD = null;
		
		public StatusReporter(String name) {
			
			this.name = name;
		}
		
		public void reportResult(double distance, double bumpiness, SpectralPowerDistribution spd) {
			
			synchronized (this) {
				if (bestSPD == null || (distance <= bestDistance && bumpiness <= bestBumpiness)) {
					bestDistance = distance;
					bestBumpiness = bumpiness;
					bestSPD = spd;
					
					LOG.info("{}: Best SPD: Distance: {} / Bumpiness: {}", name, bestDistance, bestBumpiness);
				}
			}
		}
	}
	
}
