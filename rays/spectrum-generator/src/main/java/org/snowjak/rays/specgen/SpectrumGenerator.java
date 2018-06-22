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
	
	private final ExecutorService searchExecutor = Executors
			.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	private final ExecutorService resultExecutor = Executors
			.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	
	public static void main(String[] args) {
		
		SpringApplication.run(SpectrumGenerator.class, args);
	}
	
	@Override
	public void run(String... args) throws Exception {
		
		Runtime.getRuntime().addShutdownHook(new Thread(() -> searchExecutor.shutdownNow()));
		Runtime.getRuntime().addShutdownHook(new Thread(() -> resultExecutor.shutdownNow()));
		
		final var directory = new File("./spectra/");
		if (!directory.exists())
			directory.mkdirs();
		
		runFor("white", RGB.WHITE, new File(directory, "white.csv"));
		runFor("red", RGB.RED, new File(directory, "red.csv"));
		runFor("green", RGB.GREEN, new File(directory, "green.csv"));
		runFor("blue", RGB.BLUE, new File(directory, "blue.csv"));
		
		searchExecutor.shutdown();
		
	}
	
	public void runFor(String name, RGB rgb, File outputCsv)
			throws IOException, InterruptedException, ExecutionException {
		
		LOG.info("Generating a spectrum fit for: \"{}\" ({} / {})", name, rgb.toString(), rgb.to(XYZ.class).toString());
		
		final BlockingQueue<Pair<Pair<Double, Double>, SpectralPowerDistribution>> resultQueue = new LinkedBlockingQueue<>();
		
		final Runnable resultGrabber = () -> {
			
			Pair<Pair<Double, Double>, SpectralPowerDistribution> bestResult = null;
			
			try {
				boolean receivedFinalResult = false;
				
				while (!Thread.interrupted() && !receivedFinalResult) {
					final var newResult = resultQueue.take();
					
					if (newResult.getKey() == null || newResult.getValue() == null) {
						LOG.info("{}: received final (empty) result.", name);
						receivedFinalResult = true;
					}
					
					else if (bestResult == null) {
						LOG.info("{}: received first result (d={}, b={}).", name, newResult.getKey().getFirst(),
								newResult.getKey().getSecond());
						bestResult = newResult;
					}
					
					else if ((newResult.getKey().getFirst() <= bestResult.getKey().getFirst())
							&& (newResult.getKey().getSecond() < bestResult.getKey().getSecond())) {
						LOG.info("{}: received new best-result (d={}, b={}).", name, newResult.getKey().getFirst(),
								newResult.getKey().getSecond());
						bestResult = newResult;
					}
				}
			} catch (InterruptedException e) {
				//
				LOG.warn("{}: interrupted!", name);
			} finally {
				
				if (bestResult != null) {
					
					LOG.info("{}: writing results to file.", name);
					
					LOG.info("{}: resulting RGB = {}", name, XYZ.fromSpectrum(bestResult.getValue()).to(RGB.class));
					LOG.info("{}: Distance: {}", name, bestResult.getKey().getFirst());
					LOG.info("{}: Bumpiness: {}", name, bestResult.getKey().getSecond());
					
					LOG.info("{}: Writing spectrum as CSV ...", name);
					try (var csv = new FileOutputStream(outputCsv)) {
						bestResult.getValue().saveToCSV(csv);
					} catch (IOException e) {
						LOG.error("{}: Could not write spectrum to {}: {}, \"{}\"", name, outputCsv.getPath(),
								e.getClass().getSimpleName(), e.getMessage());
					}
					
				}
			}
		};
					
		searchExecutor.execute(new BruteForceSearchSpectrumGeneratorJob(rgb.to(XYZ.class), resultQueue,
				Settings.getInstance().getIlluminatorSpectralPowerDistribution(), 10, new Pair<>(0d, 1.5d), 0.1, 0.1));
		resultExecutor.execute(resultGrabber);
		
	}
	
}
