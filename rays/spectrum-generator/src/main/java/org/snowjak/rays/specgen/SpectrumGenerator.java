package org.snowjak.rays.specgen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.math3.util.Pair;
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
	
	private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
	public static void main(String[] args) {
		
		SpringApplication.run(SpectrumGenerator.class, args);
	}
	
	@Override
	public void run(String... args) throws Exception {
		
		Runtime.getRuntime().addShutdownHook(new Thread(() -> executor.shutdownNow()));
		
		final var directory = new File("/spectra/");
		if (!directory.exists())
			directory.mkdirs();
		
		runFor(RGB.WHITE, new File(directory, "white.csv"));
		runFor(RGB.RED, new File(directory, "red.csv"));
		runFor(RGB.GREEN, new File(directory, "green.csv"));
		runFor(RGB.BLUE, new File(directory, "blue.csv"));
		
		executor.shutdown();
		
	}
	
	public void runFor(RGB rgb, File outputCsv) throws IOException, InterruptedException, ExecutionException {
		
		executor.execute(() -> {
			LOG.info("Generating a spectrum fit for: {} / {}", rgb.toString(), rgb.to(XYZ.class).toString());
			
			final BlockingQueue<Pair<Pair<Double, Double>, SpectralPowerDistribution>> resultQueue = new LinkedBlockingQueue<>();
			
			new BruteForceSearchSpectrumGeneratorJob(rgb.to(XYZ.class), resultQueue,
				Settings.getInstance().getIlluminatorSpectralPowerDistribution(),
				Settings.getInstance().getSpectrumBinCount(),
				new Pair<>(0d, 4d), 0.1, 0.1).run();
			
			//
			//
			if (resultQueue.isEmpty()) {
				LOG.error("Did not identify any close-enough spectra.");
				return;
			}
			
			LOG.info("Identified {} close-enough spectra, getting the least-bumpy version ...");
			final var result = resultQueue.stream()
					.sorted((pp1, pp2) -> Double.compare(pp1.getFirst().getSecond(), pp2.getFirst().getSecond()))
					.findFirst().get();
			
			LOG.info("Best spectrum: RGB = {}", XYZ.fromSpectrum(result.getSecond()).to(RGB.class));
			LOG.info("Distance: {}", result.getFirst().getFirst());
			LOG.info("Bumpiness: {}", result.getFirst().getSecond());
			
			LOG.info("Writing spectrum as CSV ...");
			try (var csv = new FileOutputStream(outputCsv)) {
				result.getSecond().saveToCSV(csv);
			} catch (IOException e) {
				LOG.error("Could not write spectrum to {}: {}, \"{}\"", outputCsv.getPath(),
						e.getClass().getSimpleName(), e.getMessage());
			}
		});
		
	}
	
}
