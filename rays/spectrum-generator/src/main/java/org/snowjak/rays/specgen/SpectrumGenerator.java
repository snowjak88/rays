package org.snowjak.rays.specgen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.Settings;
import org.snowjak.rays.geometry.util.Point;
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
	
	private final ForkJoinPool forkJoinPool = new ForkJoinPool();
	
	public static void main(String[] args) {
		
		SpringApplication.run(SpectrumGenerator.class, args);
	}
	
	@Override
	public void run(String... args) throws Exception {
		
		Runtime.getRuntime().addShutdownHook(new Thread(() -> forkJoinPool.shutdownNow()));
		
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
		
		final var originalTarget = rgb.to(XYZ.class);
		final var target = new XYZ(originalTarget.get().divide(originalTarget.getY()));
		final var multiplierRange = new Pair<>(0d, 1.2d);
		final var incStep = 0.1d;
		
		final var startingSPD = Settings.getInstance().getIlluminatorSpectralPowerDistribution();
		final var spdTable = startingSPD.getTable();
		final var startingPoints = spdTable.navigableKeySet().stream().map(k -> spdTable.get(k))
				.toArray(len -> new Point[len]);
		
		final var table = startingSPD.resize(binCount).getTable();
		final var vector = table.navigableKeySet().stream().map(k -> table.get(k)).toArray(len -> new Point[len]);
		
		final var result = forkJoinPool.submit(new BruteForceSpectrumSearchRecursiveTask(originalTarget, target,
				multiplierRange, incStep, startingPoints, new StatusReporter(name), vector)).join();
		
		LOG.info("{}: writing result to file.", name);
		
		LOG.info("{}: resulting RGB = {}", name, XYZ.fromSpectrum(result.getValue()).to(RGB.class));
		LOG.info("{}: Distance: {}", name, result.getKey().getFirst());
		LOG.info("{}: Bumpiness: {}", name, result.getKey().getSecond());
		
		LOG.info("{}: Writing spectrum as CSV ...", name);
		try (var csv = new FileOutputStream(outputCsv)) {
			result.getValue().saveToCSV(csv);
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
