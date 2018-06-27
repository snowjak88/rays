package org.snowjak.rays.specgen;

import static org.apache.commons.math3.util.FastMath.max;
import static org.apache.commons.math3.util.FastMath.min;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.Settings;
import org.snowjak.rays.geometry.util.Point;
import org.snowjak.rays.specgen.SpectrumGenerator.StatusReporter;
import org.snowjak.rays.spectrum.colorspace.XYZ;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

@Component
@ConfigurationProperties("stochastic")
public class StochasticSpectrumSearch implements SpectrumSearch {
	
	private static final Logger LOG = LoggerFactory.getLogger(StochasticSpectrumSearch.class);
	private static final Random RND = new Random(System.currentTimeMillis());
	
	private int generationSize;
	private int reproducerPoolSize;
	private int minGenerations;
	private int maxGenerations;
	
	@Value("${parallelism}")
	private int parallelism;
	
	@Value("${distance}")
	private double targetDistance;
	
	@Value("${bins}")
	private int binCount;
	
	private ListeningExecutorService executor = null;
	
	public StochasticSpectrumSearch() {
		
	}
	
	private static final Comparator<SpectrumSearch.Result> RESULT_COMPARATOR = (r1,
			r2) -> (Double.compare(r1.getDistance(), r2.getDistance()) * 2
					+ Double.compare(r1.getBumpiness(), r2.getBumpiness()));
	
	@Override
	public Result doSearch(XYZ targetColor, SpectralPowerDistribution startingSPD, StatusReporter reporter) {
		
		if (executor == null)
			this.executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(parallelism));
		
		List<SpectrumSearch.Result> reproducerPool = new ArrayList<>(reproducerPoolSize);
		for (int i = 0; i < reproducerPoolSize; i++)
			// reproducerPool.add(CandidateSPDEvaluator.evaluateSPD(getRandomizedSPD(binCount),
			// originalTarget));
			reproducerPool.add(SpectrumSearch.evaluateSPD(mutate(startingSPD.resize(binCount)), targetColor));
		
		int generationCount = 0;
		
		var bestResult = getBestResult(reproducerPool);
		do {
			generationCount++;
			
			final var currentReproducerPool = reproducerPool;
			final List<ListenableFuture<Result>> nextGeneration = IntStream.range(0, generationSize)
					.mapToObj(i -> currentReproducerPool.get(RND.nextInt(currentReproducerPool.size())))
					.map(r -> executor.submit(new CandidateSPDEvaluator(r,
							mutate(cross(r.getSpd(),
									currentReproducerPool.get(RND.nextInt(currentReproducerPool.size())).getSpd())),
							targetColor)))
					.collect(Collectors.toList());
			
			try {
				
				reproducerPool = Futures.whenAllSucceed(nextGeneration).call(() -> nextGeneration.stream().map(fr -> {
					try {
						return fr.get();
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
					return null;
				}).filter(r -> r != null).sorted(RESULT_COMPARATOR).limit(reproducerPoolSize)
						.collect(Collectors.toList()), executor).get();
				
			} catch (InterruptedException | ExecutionException e) {
				LOG.error("Unexpected exception: {}, \"{}\"", e.getClass().getSimpleName(), e.getMessage());
				throw new RuntimeException("Unexpected exception!", e);
			}
			
			bestResult = getBestResult(reproducerPool);
			reporter.reportResult(bestResult.getDistance(), bestResult.getBumpiness(), bestResult.getSpd());
			
		} while (bestResult.getDistance() > targetDistance
				&& (generationCount < minGenerations || generationCount <= maxGenerations));
		
		return new Result(bestResult.getDistance(), bestResult.getBumpiness(), bestResult.getSpd().normalize());
	}
	
	private SpectrumSearch.Result getBestResult(Collection<SpectrumSearch.Result> results) {
		
		assert (results != null);
		assert (!results.isEmpty());
		
		return results.stream().min(RESULT_COMPARATOR).get();
	}
	
	private SpectralPowerDistribution getRandomizedSPD(int binCount) {
		
		return new SpectralPowerDistribution(
				IntStream.range(0, binCount).mapToObj(i -> new Point(RND.nextDouble())).toArray(len -> new Point[len]));
	}
	
	private SpectralPowerDistribution cross(SpectralPowerDistribution spd1, SpectralPowerDistribution spd2) {
		
		final Point[] entries1 = spd1.getTable().navigableKeySet().stream().map(k -> spd1.get(k))
				.toArray(len -> new Point[len]);
		final Point[] entries2 = spd2.getTable().navigableKeySet().stream().map(k -> spd2.get(k))
				.toArray(len -> new Point[len]);
		
		final int crossPoint = Settings.RND.nextInt(min(entries1.length, entries2.length));
		
		final var shorter = (entries1.length <= entries2.length) ? entries1 : entries2;
		final var longer = (entries1.length <= entries2.length) ? entries2 : entries1;
		
		final Point[] result = new Point[max(entries1.length, entries2.length)];
		for (int i = 0; i < result.length; i++) {
			if (i < crossPoint)
				result[i] = shorter[i];
			else
				result[i] = longer[i];
		}
		
		return new SpectralPowerDistribution(result);
	}
	
	private SpectralPowerDistribution mutate(SpectralPowerDistribution spd) {
		
		final Point[] entries = spd.getTable().navigableKeySet().stream().map(k -> spd.get(k))
				.toArray(len -> new Point[len]);
		
		entries[RND.nextInt(entries.length)] = new Point(RND.nextDouble());
		
		return new SpectralPowerDistribution(spd.getBounds().get(), entries);
	}
	
	public static class CandidateSPDEvaluator implements Callable<SpectrumSearch.Result> {
		
		private final SpectrumSearch.Result ancestor;
		private final SpectralPowerDistribution candidate;
		private final XYZ target;
		
		public CandidateSPDEvaluator(SpectrumSearch.Result ancestor, SpectralPowerDistribution candidate, XYZ target) {
			
			this.ancestor = ancestor;
			this.candidate = candidate;
			this.target = target;
		}
		
		@Override
		public Result call() throws Exception {
			
			final var candidateResult = SpectrumSearch.evaluateSPD(candidate, target);
			
			return (candidateResult.getDistance() <= ancestor.getDistance()
					&& candidateResult.getBumpiness() <= ancestor.getBumpiness()) ? candidateResult : ancestor;
			
		}
		
	}
	
	private SpectralPowerDistribution scaleSPD(SpectralPowerDistribution spd, XYZ targetColor) {
		
		final double brightness = targetColor.getY();
		
		return (SpectralPowerDistribution) spd.multiply(brightness);
	}
	
	public int getGenerationSize() {
		
		return generationSize;
	}
	
	public void setGenerationSize(int generationSize) {
		
		this.generationSize = generationSize;
	}
	
	public int getReproducerPoolSize() {
		
		return reproducerPoolSize;
	}
	
	public void setReproducerPoolSize(int reproducerPoolSize) {
		
		this.reproducerPoolSize = reproducerPoolSize;
	}
	
	public int getMinGenerations() {
		
		return minGenerations;
	}
	
	public void setMinGenerations(int minGenerations) {
		
		this.minGenerations = minGenerations;
	}
	
	public int getMaxGenerations() {
		
		return maxGenerations;
	}
	
	public void setMaxGenerations(int maxGenerations) {
		
		this.maxGenerations = maxGenerations;
	}
	
}
