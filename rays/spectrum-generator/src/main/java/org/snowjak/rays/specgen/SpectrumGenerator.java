package org.snowjak.rays.specgen;

import static org.apache.commons.math3.util.FastMath.max;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

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
		
		runFor("white", 16, RGB.WHITE, new File(directory, "white.csv"),
				Settings.getInstance().getIlluminatorSpectralPowerDistribution());
		runFor("red", 16, RGB.RED, new File(directory, "red.csv"),
				Settings.getInstance().getIlluminatorSpectralPowerDistribution());
		runFor("green", 16, RGB.GREEN, new File(directory, "green.csv"),
				Settings.getInstance().getIlluminatorSpectralPowerDistribution());
		runFor("blue", 16, RGB.BLUE, new File(directory, "blue.csv"),
				Settings.getInstance().getIlluminatorSpectralPowerDistribution());
		
	}
	
	public void runFor(String name, int binCount, RGB rgb, File outputCsv, SpectralPowerDistribution startingSPD)
			throws IOException, InterruptedException, ExecutionException {
		
		LOG.info("Generating a spectrum fit for: \"{}\" ({} / {})", name, rgb.toString(), rgb.to(XYZ.class).toString());
		
		final var result = new StochasticSpectrumSearch(binCount, rgb.to(XYZ.class), startingSPD, 32, 0.050d,
				Integer.MAX_VALUE, Runtime.getRuntime().availableProcessors(), new StatusReporter(name, 9, 40))
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
		
		private final boolean graphEnabled;
		private final int graphRows, graphColumns;
		
		private double bestDistance, bestBumpiness;
		private SpectralPowerDistribution bestSPD = null;
		
		public StatusReporter(String name) {
			
			this.name = name;
			this.graphEnabled = false;
			
			this.graphRows = 0;
			this.graphColumns = 0;
		}
		
		public StatusReporter(String name, int graphRows, int graphColumns) {
			
			assert (graphRows > 1);
			assert (graphColumns > 2);
			
			this.name = name;
			this.graphEnabled = true;
			this.graphRows = graphRows;
			this.graphColumns = graphColumns;
		}
		
		public void reportResult(double distance, double bumpiness, SpectralPowerDistribution spd) {
			
			synchronized (this) {
				if (bestSPD == null || (distance <= bestDistance && bumpiness <= bestBumpiness)) {
					bestDistance = distance;
					bestBumpiness = bumpiness;
					bestSPD = spd;
					
					LOG.info("{}: Best SPD: Distance: {} / Bumpiness: {}", name, bestDistance, bestBumpiness);
					
					if (this.graphEnabled) {
						final double lowBound = spd.getBounds().get().getFirst(),
								highBound = spd.getBounds().get().getSecond();
						final double colSpan = (highBound - lowBound) / ((double) graphColumns - 1d);
						
						final double[] measurements = IntStream.range(0, graphColumns - 1)
								.mapToDouble(i -> ((double) i * colSpan) + lowBound).map(k -> spd.get(k).get(0))
								.toArray();
						
						final double maxMeasurement = Arrays.stream(measurements).max().getAsDouble();
						
						final double rowSpan = max(1d, maxMeasurement) / ((double) (graphRows - 1));
						
						for (int row = 0; row < graphRows; row++) {
							
							final var graphBuilder = new StringBuilder();
							
							final var isLastRow = (row == graphRows - 1);
							if (isLastRow)
								graphBuilder.append("+");
							else
								graphBuilder.append("|");
							
							final double rowBoundLow = (double) (graphRows - row - 2) * rowSpan;
							final double rowBoundHigh = (double) (graphRows - row - 1) * rowSpan;
							
							for (int col = 0; col < graphColumns - 1; col++) {
								
								if (isLastRow)
									graphBuilder.append("-");
								else {
									
									if (measurements[col] > rowBoundLow && measurements[col] <= rowBoundHigh)
										graphBuilder.append("*");
									else
										graphBuilder.append(" ");
								}
							}
							
							LOG.info("{}: SPD: {}", name, graphBuilder.toString());
						}
					}
				}
			}
		}
	}
	
}
