package org.snowjak.rays.specgen;

import static org.apache.commons.math3.util.FastMath.max;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.geometry.util.Triplet;
import org.snowjak.rays.spectrum.colorspace.RGB;
import org.snowjak.rays.spectrum.colorspace.XYZ;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

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
@EnableConfigurationProperties
public class SpectrumGenerator implements CommandLineRunner {
	
	private static final Logger LOG = LoggerFactory.getLogger(SpectrumGenerator.class);
	
	@Value("${generator-type}")
	private String generatorType;
	
	@Value("${colors}")
	private String colors;
	
	@Value("${bins}")
	private int binCount;
	
	@Value("${distance}")
	private double targetDistance;
	
	@Value("${bumpiness}")
	private double targetBumpiness;
	
	@Value("${parallelism}")
	private int parallelism;
	
	@Autowired
	private StochasticSpectrumSearch stochastic;
	@Autowired
	private BruteForceSpectrumSearch bruteForce;
	
	@Autowired
	SpectrumGeneratorProperties spectrumGeneratorProperties;
	
	public static void main(String[] args) {
		
		SpringApplication.run(SpectrumGenerator.class, args);
	}
	
	@Override
	public void run(String... args) throws Exception {
		
		final var directory = new File("./spectra/");
		if (!directory.exists())
			directory.mkdirs();
		
		if (generatorType.trim().isEmpty()) {
			LOG.warn("No generator-type specified. Please specify one of: {}",
					spectrumGeneratorProperties.getAvailableGenerators().stream().collect(Collectors.joining(",")));
			return;
		}
		
		if (!spectrumGeneratorProperties.getAvailableGenerators().contains(generatorType)) {
			LOG.error("Given generator-type \"{}\" is not recognized! Please use one of: {}", generatorType,
					spectrumGeneratorProperties.getAvailableGenerators().stream().collect(Collectors.joining(",")));
			return;
		}
		
		if (colors.trim().isEmpty())
			LOG.warn("No colors for which to generate spectra. Nothing to do.");
		
		for (String color : colors.split(",")) {
			
			if (!spectrumGeneratorProperties.getAvailableColors().contains(color)) {
				LOG.error("Given color \"{}\" is not recognized! Please use one of: {}", color,
						spectrumGeneratorProperties.getAvailableColors().stream().collect(Collectors.joining(",")));
				return;
			}
			
			if (!spectrumGeneratorProperties.getColorDefinitions().containsKey(color)) {
				LOG.error("Given color \"{}\" does not have a configured RGB definition! Please use one of: {}", color,
						spectrumGeneratorProperties.getAvailableColors().stream().collect(Collectors.joining(",")));
				return;
			}
		}
		
		if (parallelism <= 0) {
			LOG.error("--parallelism must be greater than 0!");
			return;
		}
		
		if (targetDistance <= 0) {
			LOG.error("--distance must be greater than 0!");
			return;
		}
		
		if (targetBumpiness <= 0) {
			LOG.error("--bumpiness must be greater than 0!");
			return;
		}
		
		for (String color : colors.split(","))
			runFor(generatorType, parallelism, color, binCount, new RGB(new Triplet(spectrumGeneratorProperties
					.getColorDefinitions().get(color).stream().mapToDouble(d -> d).toArray())),
					new File(directory, color + ".csv"));
		
	}
	
	public void runFor(String generatorType, int parallelism, String name, int binCount, RGB rgb, File outputCsv)
			throws IOException, InterruptedException, ExecutionException {
		
		LOG.info("Generating a spectrum fit for: \"{}\" ({} / {})", name, rgb.toString(), rgb.to(XYZ.class).toString());
		
		final SpectrumSearch.Result result;
		
		switch (generatorType) {
		case "STOCHASTIC":
			result = stochastic.doSearch(rgb.to(XYZ.class), new StatusReporter(name, 9, 40));
			break;
		case "BRUTE-FORCE":
			result = bruteForce.doSearch(rgb.to(XYZ.class), new StatusReporter(name, 9, 40));
			break;
		default:
			result = null;
			return;
		}
		
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
				if (bestSPD == null || (distance < bestDistance)) {
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
