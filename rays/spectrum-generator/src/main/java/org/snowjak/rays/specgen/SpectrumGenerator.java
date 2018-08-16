package org.snowjak.rays.specgen;

import static org.apache.commons.math3.util.FastMath.max;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.Settings;
import org.snowjak.rays.geometry.util.Point;
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
	private static final Random RND = new Random(System.currentTimeMillis());
	
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
	
	@Value("${min-energy}")
	private double minEnergy;
	
	@Value("${max-energy}")
	private double maxEnergy;
	
	@Value("${starting-spd}")
	private String startingSPDName;
	
	@Value("${output}")
	private String outputName;
	
	private SpectralPowerDistribution startingSpd = null;
	
	private Function<String, OutputStream> outputSupplier = null;
	
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
		
		if (minEnergy < 0d)
			LOG.warn(
					"WARNING -- you've selected an allowed minimum-energy of less than 0. This will allow the Generator to generate non-physical Spectral Power Distributions.");
		
		if (maxEnergy > 1d)
			LOG.warn(
					"WARNING -- you've selected an allowed maximum-energy of greater than 1. This will allow the Generator to generate non-physical Spectral Power Distributions.");
		
		startingSpd = getRandomizedSPD(binCount);
		if (startingSPDName.equalsIgnoreCase("D65"))
			startingSpd = Settings.getInstance().getIlluminatorSpectralPowerDistribution();
		
		else if (startingSPDName != null && !startingSPDName.trim().equals("")) {
			
			final Optional<String> spdFileName = Arrays.stream(directory.list())
					.filter(fn -> fn.equalsIgnoreCase(startingSPDName + ".csv")).findFirst();
			
			if (spdFileName.isPresent()) {
				LOG.info("Loading existing SPD {} as starting-SPD.", spdFileName.get());
				startingSpd = SpectralPowerDistribution
						.loadFromCSV(new FileInputStream(new File(directory, spdFileName.get())));
			}
			
		}
		
		if (!spectrumGeneratorProperties.getAvailableOutputs().contains(outputName)) {
			LOG.error("Given --output \"{}\" is not recognized. Please use one of: {}", outputName,
					spectrumGeneratorProperties.getAvailableOutputs().stream().collect(Collectors.joining(", ")));
			return;
		} else {
			
			switch (outputName) {
			case "FILE":
				outputSupplier = (color) -> {
					try {
						return new FileOutputStream(new File(directory, color + ".csv"));
					} catch (FileNotFoundException e) {
						//
						//
						e.printStackTrace();
					}
					return null;
				};
				break;
			case "CONSOLE":
			default:
				outputSupplier = (color) -> System.out;
				break;
			}
			
		}
		
		for (String color : colors.split(","))
			runFor(generatorType, parallelism, color, binCount, new RGB(new Triplet(spectrumGeneratorProperties
					.getColorDefinitions().get(color).stream().mapToDouble(d -> d).toArray())));
		
	}
	
	private SpectralPowerDistribution getRandomizedSPD(int binCount) {
		
		return new SpectralPowerDistribution(IntStream.range(0, binCount)
				.mapToObj(i -> new Point(RND.nextDouble() * (maxEnergy - minEnergy) + minEnergy))
				.toArray(len -> new Point[len]));
	}
	
	public void runFor(String generatorType, int parallelism, String name, int binCount, RGB rgb)
			throws IOException, InterruptedException, ExecutionException {
		
		LOG.info("Generating a spectrum fit for: \"{}\" ({} / {})", name, rgb.toString(), rgb.to(XYZ.class).toString());
		
		final SpectrumSearch.Result result;
		
		switch (generatorType) {
		case "STOCHASTIC":
			result = stochastic.doSearch(rgb.to(XYZ.class), startingSpd, new StatusReporter(name, 9, 40));
			break;
		case "BRUTE-FORCE":
			result = bruteForce.doSearch(rgb.to(XYZ.class), startingSpd, new StatusReporter(name, 9, 40));
			break;
		default:
			result = null;
			return;
		}
		
		LOG.info("{}: writing result to file.", name);
		
		LOG.info("{}: resulting RGB = {}", name, XYZ.fromSpectrum(result.getSpd()).to(RGB.class));
		LOG.info("{}: Distance: {}", name, result.getDistance());
		LOG.info("{}: Bumpiness: {}", name, result.getBumpiness());
		
		LOG.info("{}: Writing spectrum ...", name);
		final OutputStream os = outputSupplier.apply(name);
		if (os != null)
			result.getSpd().saveToCSV(os);
		
	}
	
	public static class StatusReporter {
		
		private static final Logger LOG = LoggerFactory.getLogger(StatusReporter.class);
		
		private final String name;
		
		private final boolean graphEnabled;
		private final int graphRows, graphColumns;
		
		private double bestDistance, bestBumpiness;
		private XYZ bestXYZ;
		private RGB bestRGB;
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
		
		public void reportResult(SpectrumSearch.Result result) {
			
			synchronized (this) {
				if (bestSPD == null || (result.getDistance() < bestDistance)) {
					bestDistance = result.getDistance();
					bestBumpiness = result.getBumpiness();
					bestXYZ = result.getXyz();
					bestRGB = result.getRgb();
					bestSPD = result.getSpd();
					
					LOG.info("{}: Best SPD: Distance: {} / Bumpiness: {}", name, bestDistance, bestBumpiness);
					LOG.info("{}: XYZ: {}", name, bestXYZ.toString());
					LOG.info("{}: RGB: {}", name, bestRGB.toString());
					
					if (this.graphEnabled) {
						final double lowBound = bestSPD.getBounds().get().getFirst(),
								highBound = bestSPD.getBounds().get().getSecond();
						final double colSpan = (highBound - lowBound) / ((double) graphColumns - 1d);
						
						final double[] measurements = IntStream.range(0, graphColumns - 1)
								.mapToDouble(i -> ((double) i * colSpan) + lowBound).map(k -> bestSPD.get(k).get(0))
								.toArray();
						
						final double maxMeasurement = Arrays.stream(measurements).max().getAsDouble();
						
						final double rowSpan = max(1d, maxMeasurement) / ((double) (graphRows - 1));
						
						for (int row = 0; row < graphRows; row++) {
							
							final var graphBuilder = new StringBuilder();
							
							final double rowBoundLow = (double) (graphRows - row - 2) * rowSpan;
							final double rowBoundHigh = (double) (graphRows - row - 1) * rowSpan;
							
							final var isLastRow = (row == graphRows - 1);
							final var isTickRow = ( rowBoundLow <= 1d && rowBoundHigh >= 1d );
							if (isLastRow)
								graphBuilder.append("+");
							else if (isTickRow)
								graphBuilder.append("+");
							else
								graphBuilder.append("|");
							
							for (int col = 0; col < graphColumns - 1; col++) {
								
								if (isLastRow)
									graphBuilder.append("-");
								else {
									
									if (measurements[col] > rowBoundLow && measurements[col] <= rowBoundHigh)
										graphBuilder.append("*");
									else if (isTickRow)
										graphBuilder.append("-");
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
		
		public double getBestDistance() {
			
			return bestDistance;
		}
		
		public double getBestBumpiness() {
			
			return bestBumpiness;
		}
		
		public SpectralPowerDistribution getBestSPD() {
			
			return bestSPD;
		}
	}
	
}
