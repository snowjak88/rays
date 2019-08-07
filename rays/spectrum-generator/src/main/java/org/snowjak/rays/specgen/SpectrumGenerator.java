package org.snowjak.rays.specgen;

import static org.apache.commons.math3.util.FastMath.max;
import static org.apache.commons.math3.util.FastMath.pow;

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
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.Settings;
import org.snowjak.rays.geometry.util.Point;
import org.snowjak.rays.geometry.util.Triplet;
import org.snowjak.rays.specgen.SpectrumSearch.Result;
import org.snowjak.rays.spectrum.colorspace.RGB;
import org.snowjak.rays.spectrum.colorspace.RGB_Gammaless;
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
	public static final Random RND = new Random(System.currentTimeMillis());
	
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
	
	@Value("${color-model}")
	private String colorModel;
	
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
	
	private Function<String, SpectralPowerDistribution> startingSpd = null;
	
	private Function<String, OutputStream> outputSupplier = null;
	
	private BiFunction<SpectralPowerDistribution, RGB, SpectrumSearch.Result> distanceCalculator = null;
	
	@Autowired
	private StochasticSpectrumSearch stochastic;
	@Autowired
	private BruteForceSpectrumSearch bruteForce;
	@Autowired
	private UniformSpectrumSearch uniformSearch;
	@Autowired
	private SpectrumResultViewer viewResult;
	
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
		
		distanceCalculator = (spd, targetColor) -> {
			final XYZ xyz = XYZ.fromSpectrum(spd, true);
			final RGB_Gammaless rgb = xyz.to(RGB_Gammaless.class);
			final RGB srgb = xyz.to(RGB.class);
			
			final var targetRGB = targetColor.to(RGB_Gammaless.class);
			
			return new Result(pow(rgb.getRed() - targetRGB.getRed(), 2) + pow(rgb.getGreen() - targetRGB.getGreen(), 2)
					+ pow(rgb.getBlue() - targetRGB.getBlue(), 2), 0, xyz, rgb, srgb, spd);
		};
		
		switch (colorModel.toLowerCase()) {
		case "xyz":
			distanceCalculator = (spd, targetColor) -> {
				final XYZ xyz = XYZ.fromSpectrum(spd, true);
				final RGB_Gammaless rgb = xyz.to(RGB_Gammaless.class);
				final RGB srgb = xyz.to(RGB.class);
				
				final var targetXYZ = targetColor.to(XYZ.class);
				
				return new Result(pow(xyz.getX() - targetXYZ.getX(), 2) + pow(xyz.getY() - targetXYZ.getY(), 2)
						+ pow(xyz.getZ() - targetXYZ.getZ(), 2), 0, xyz, rgb, srgb, spd);
			};
			break;
		case "srgb":
			distanceCalculator = (spd, targetColor) -> {
				final XYZ xyz = XYZ.fromSpectrum(spd, true);
				final RGB srgb = xyz.to(RGB.class);
				final RGB_Gammaless rgb = xyz.to(RGB_Gammaless.class);
				
				final var targetSRGB = targetColor.to(RGB.class);
				
				return new Result(
						pow(srgb.getRed() - targetSRGB.getRed(), 2) + pow(srgb.getGreen() - targetSRGB.getGreen(), 2)
								+ pow(srgb.getBlue() - targetSRGB.getBlue(), 2),
						0, xyz, rgb, srgb, spd);
			};
			break;
		case "rgb":
			break;
		default:
			LOG.error("Unknown color-model ({}) -- must be one of 'xyz', 'rgb'. Falling back to 'rgb'.", colorModel);
		}
		
		if (minEnergy < 0d)
			LOG.warn(
					"WARNING -- you've selected an allowed minimum-energy of less than 0 W*nm. This will allow the Generator to generate non-physical Spectral Power Distributions.");
		
		final Supplier<SpectralPowerDistribution> defaultSpd = () -> Settings.getInstance()
				.getIlluminatorSpectralPowerDistribution();
		final Function<String, SpectralPowerDistribution> spdFileLoader = (name) -> {
			final Optional<String> spdFileName = Arrays.stream(directory.list())
					.filter(fn -> fn.equalsIgnoreCase(name + ".csv")).findFirst();
			
			if (spdFileName.isPresent()) {
				LOG.trace("Loading existing SPD {} as starting-SPD.", spdFileName.get());
				
				try {
					return SpectralPowerDistribution
							.loadFromCSV(new FileInputStream(new File(directory, spdFileName.get())));
				} catch (FileNotFoundException e) {
					LOG.error("Cannot load existing SPD {} as starting-SPD -- file does not exist!");
				} catch (IOException e) {
					LOG.error("Cannot load existing SPD {} as starting-SPD -- unexpected exception!", e);
				}
				
			}
			LOG.info("Cannot use existing SPD {} as starting-SPD. Falling back to default (random).", startingSPDName);
			return defaultSpd.get();
			
		};
		
		if (startingSPDName == null || startingSPDName.trim().equals("")
				|| startingSPDName.trim().equalsIgnoreCase("RANDOM"))
			startingSpd = (name) -> getRandomizedSPD(binCount);
		
		else if (startingSPDName.trim().equalsIgnoreCase("D65"))
			startingSpd = (name) -> Settings.getInstance().getIlluminatorSpectralPowerDistribution();
		
		else if (startingSPDName.trim().equalsIgnoreCase("{}"))
			startingSpd = (name) -> spdFileLoader.apply(name);
		
		else if (startingSPDName != null && !startingSPDName.trim().equals("")) {
			
			startingSpd = (name) -> spdFileLoader.apply(startingSPDName.trim());
			
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
			runFor(generatorType, color, binCount, new RGB(new Triplet(spectrumGeneratorProperties.getColorDefinitions()
					.get(color).stream().mapToDouble(d -> d).toArray())));
		
	}
	
	private SpectralPowerDistribution getRandomizedSPD(int binCount) {
		
		return new SpectralPowerDistribution(IntStream.range(0, binCount)
				.mapToObj(i -> new Point(RND.nextDouble() * (maxEnergy - minEnergy) + minEnergy))
				.toArray(len -> new Point[len]));
	}
	
	public void runFor(String generatorType, String name, int binCount, RGB rgb)
			throws IOException, InterruptedException, ExecutionException {
		
		LOG.info("Generating a spectrum fit for: \"{}\" ({} / {})", name, rgb.toString(), rgb.to(XYZ.class).toString());
		
		final SpectrumSearch.Result result;
		
		switch (generatorType) {
		case "STOCHASTIC":
			result = stochastic.doSearch(distanceCalculator, rgb, () -> startingSpd.apply(name),
					new StatusReporter(name));
			break;
		case "BRUTE-FORCE":
			result = bruteForce.doSearch(distanceCalculator, rgb, () -> startingSpd.apply(name),
					new StatusReporter(name));
			break;
		case "UNIFORM":
			result = uniformSearch.doSearch(distanceCalculator, rgb, () -> startingSpd.apply(name),
					new StatusReporter(name));
			break;
		case "VIEW":
			result = viewResult.doSearch(distanceCalculator, rgb, () -> startingSpd.apply(name), new StatusReporter(name));
			break;
		default:
			result = null;
			return;
		}
		
		LOG.info("{}: writing result to file.", name);
		
		LOG.info("{}: resulting RGB = {}", name, result.getRgb());
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
		private RGB bestSRGB;
		private RGB_Gammaless bestRGB;
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
					bestSRGB = result.getSrgb();
					bestSPD = result.getSpd();
					
					LOG.info("{}: Best SPD: Distance: {} / Bumpiness: {}", name, bestDistance, bestBumpiness);
					LOG.info("{}: XYZ: {}", name, bestXYZ.toString());
					LOG.info("{}: RGB: {}", name, bestRGB.toString());
					LOG.info("{}: sRGB: {}", name, bestSRGB.toString());
					
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
							final var isTickRow = (rowBoundLow <= 1d && rowBoundHigh >= 1d);
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
