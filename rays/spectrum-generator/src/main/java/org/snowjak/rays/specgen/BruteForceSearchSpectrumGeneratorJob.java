package org.snowjak.rays.specgen;

import static org.apache.commons.math3.util.FastMath.min;
import static org.apache.commons.math3.util.FastMath.pow;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.spectrum.colorspace.XYZ;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;
import org.snowjak.rays.spectrum.distribution.TabulatedSpectralPowerDistribution;

public class BruteForceSearchSpectrumGeneratorJob {
	
	private static final Logger LOG = LoggerFactory.getLogger(BruteForceSearchSpectrumGeneratorJob.class);
	
	public static final Function<double[], SpectralPowerDistribution> TABLE = (ds) -> {
		final double low = 360d, high = 830d;
		final Map<Double, Double> table = new HashMap<>();
		int i = 0;
		for (double lambda = low; lambda <= high; lambda += (high - low) / ((double) ds.length - 1d), i++)
			table.put(lambda, ds[i]);
		return new TabulatedSpectralPowerDistribution(table);
	};
	
	private final ExecutorService jobExecutor;
	private final ExecutorService resultsGrabberExecutor;
	private final ScheduledExecutorService statusUpdaterExecutor;
	
	private final BlockingQueue<Pair<Double, double[]>> resultsQueue;
	private final AtomicLong searchJobsRunning;
	
	private final Function<double[], SpectralPowerDistribution> spdType;
	private final XYZ target;
	private final int binCount;
	private final double searchMinValue;
	private final double searchMaxValue;
	private final double searchStepSize;
	private final double tolerance;
	private final int resultsRetentionLimit;
	
	public BruteForceSearchSpectrumGeneratorJob(Function<double[], SpectralPowerDistribution> spdType, XYZ target,
			int binCount, double searchMinValue, double searchMaxValue, double searchStepSize, double tolerance,
			int resultsRetentionLimit) {
		
		this.spdType = spdType;
		this.target = target;
		this.binCount = binCount;
		this.searchMinValue = searchMinValue;
		this.searchMaxValue = searchMaxValue;
		this.searchStepSize = searchStepSize;
		this.tolerance = tolerance;
		this.resultsRetentionLimit = resultsRetentionLimit;
		
		this.jobExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		this.resultsGrabberExecutor = Executors.newSingleThreadExecutor();
		this.statusUpdaterExecutor = Executors.newSingleThreadScheduledExecutor();
		
		Runtime.getRuntime().addShutdownHook(new Thread(() -> jobExecutor.shutdownNow()));
		Runtime.getRuntime().addShutdownHook(new Thread(() -> resultsGrabberExecutor.shutdownNow()));
		Runtime.getRuntime().addShutdownHook(new Thread(() -> statusUpdaterExecutor.shutdownNow()));
		
		resultsQueue = new LinkedBlockingQueue<>();
		searchJobsRunning = new AtomicLong();
	}
	
	public SpectralPowerDistribution generate() {
		
		LOG.info("Starting new brute-force-search spectrum-generation job.");
		LOG.info("Searching for solutions of length {}, on the interval [{},{}], by step-size {}", binCount,
				searchMinValue, searchMaxValue, searchStepSize);
		
		final double[] bins = new double[binCount];
		Arrays.fill(bins, searchMinValue);
		
		final NavigableMap<Double, double[]> results = new TreeMap<>();
		
		resultsGrabberExecutor.submit(() -> {
			try {
				while (true) {
					final var p = this.resultsQueue.take();
					results.put(p.getKey(), p.getValue());
					while (results.size() > resultsRetentionLimit)
						results.remove(results.lastKey());
				}
			} catch (InterruptedException e) {
				// nothing to do here
			}
		});
		
		statusUpdaterExecutor.scheduleAtFixedRate(() -> LOG.info("{} search jobs running ...", searchJobsRunning.get()),
				5, 5, TimeUnit.SECONDS);
		
		LOG.info("Preparing and submitting search candidates ...");
		submitJobs(bins, 0);
		
		while (searchJobsRunning.get() > 0) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// do nothing
			}
		}
		
		LOG.info("Completed brute-force-search spectrum-generation job.");
		jobExecutor.shutdownNow();
		resultsGrabberExecutor.shutdownNow();
		statusUpdaterExecutor.shutdownNow();
		
		LOG.info("Top {} candidates are as follows:", min(results.size(), 5));
		results.navigableKeySet().stream().limit(5)
				.forEach(k -> LOG.info("Distance {}: [{}]", Double.toString(k), Arrays.stream(results.get(k))
						.mapToObj(d -> String.format("%+1.5f", d)).collect(Collectors.joining(" "))));
		
		return spdType.apply(results.firstEntry().getValue());
	}
	
	private void submitJobs(double[] currentVector, int currentDimension) {
		
		if (currentDimension >= currentVector.length)
			return;
			
		// if (currentDimension <= (currentVector.length / 2)) {
		// final var pieces = Arrays.stream(currentVector).mapToObj(d ->
		// String.format("%+1.5f", d))
		// .collect(Collectors.toList());
		// pieces.set(currentDimension, "__._____");
		// LOG.info("Submitting search job --> [{}]",
		// pieces.stream().collect(Collectors.joining(" ")));
		// }
		
		searchJobsRunning.incrementAndGet();
		jobExecutor.submit(
				new BruteForceComputeTask(spdType, resultsQueue, searchJobsRunning, currentVector, target, tolerance));
		
		for (double v = searchMinValue; v <= searchMaxValue; v += searchStepSize) {
			
			final double[] newVector = Arrays.copyOf(currentVector, currentVector.length);
			newVector[currentDimension] = v;
			
			submitJobs(newVector, currentDimension + 1);
		}
	}
	
	public static class BruteForceComputeTask implements Runnable {
		
		private final static Logger LOG = LoggerFactory.getLogger(BruteForceComputeTask.class);
		
		private final BlockingQueue<Pair<Double, double[]>> queue;
		private final AtomicLong jobRunningCounter;
		
		private final Function<double[], SpectralPowerDistribution> spdType;
		private final double[] value;
		private final XYZ target;
		private final double tolerance;
		
		public BruteForceComputeTask(Function<double[], SpectralPowerDistribution> spdType,
				BlockingQueue<Pair<Double, double[]>> queue, AtomicLong jobRunningCounter, double[] value, XYZ target,
				double tolerance) {
			
			this.spdType = spdType;
			this.queue = queue;
			this.jobRunningCounter = jobRunningCounter;
			this.value = value;
			this.target = target;
			this.tolerance = tolerance;
		}
		
		@Override
		public void run() {
			
			try {
				final var candidateSpectrum = spdType.apply(value);
				
				final var evaluatedTriplet = XYZ.fromSpectrum(candidateSpectrum).get();
				
				final var distance = target.get().subtract(evaluatedTriplet).apply(c -> pow(c, 2)).summarize();
				
				if (distance <= tolerance) {
					queue.put(new Pair<>(distance, value));
				}
			} catch (InterruptedException e) {
				// nothing
			}
			
			jobRunningCounter.decrementAndGet();
			
		}
		
	}
}
