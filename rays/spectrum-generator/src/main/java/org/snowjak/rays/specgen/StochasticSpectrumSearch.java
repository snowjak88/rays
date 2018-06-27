package org.snowjak.rays.specgen;

import static org.apache.commons.math3.util.FastMath.pow;

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

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class StochasticSpectrumSearch implements SpectrumSearch {
	
	private static final Logger LOG = LoggerFactory.getLogger(StochasticSpectrumSearch.class);
	private static final Random RND = new Random(System.currentTimeMillis());
	
	private final XYZ target, originalTarget;
	private final SpectralPowerDistribution startingSPD;
	private final int generationSize, reproducerPoolSize;
	private final double targetDistance;
	private final int maxGenerations;
	private final ListeningExecutorService executor;
	private final StatusReporter reporter;
	
	public StochasticSpectrumSearch(int binCount, XYZ target, SpectralPowerDistribution startingSPD, int generationSize,
			int reproducerPoolSize, double targetDistance, int maxGenerations, int parallelism,
			StatusReporter reporter) {
		
		assert (binCount > 1);
		assert (target != null);
		assert (startingSPD != null);
		assert (generationSize > 0);
		
		this.originalTarget = target;
		this.target = target.normalize();
		this.startingSPD = startingSPD.resize(binCount);
		this.executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(parallelism));
		this.generationSize = generationSize;
		this.reproducerPoolSize = reproducerPoolSize;
		this.targetDistance = targetDistance;
		this.maxGenerations = maxGenerations;
		this.reporter = reporter;
	}
	
	private static final Comparator<SpectrumSearch.Result> RESULT_COMPARATOR = (r1,
			r2) -> (Double.compare(r1.getDistance(), r2.getDistance()) * 10
					+ Double.compare(r1.getBumpiness(), r2.getBumpiness()));
	
	@Override
	public Result doSearch() {
		
		List<SpectrumSearch.Result> reproducerPool = new ArrayList<>(reproducerPoolSize);
		for (int i = 0; i < reproducerPoolSize; i++)
			reproducerPool.add(CandidateSPDEvaluator.evaluateSPD(startingSPD, originalTarget));
		
		int generationCount = 0;
		
		var bestResult = getBestResult(reproducerPool);
		do {
			generationCount++;
			
			final var currentReproducerPool = reproducerPool;
			final List<ListenableFuture<Result>> nextGeneration = IntStream.range(0, generationSize)
					.mapToObj(i -> currentReproducerPool.get(Settings.RND.nextInt(currentReproducerPool.size())))
					.map(r -> executor.submit(new CandidateSPDEvaluator(r, mutate(r.getSpd()), originalTarget)))
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
			
		} while (bestResult.getDistance() > targetDistance && generationCount <= maxGenerations);
		
		return new Result(bestResult.getDistance(), bestResult.getBumpiness(), bestResult.getSpd().normalize());
	}
	
	private SpectrumSearch.Result getBestResult(Collection<SpectrumSearch.Result> results) {
		
		if (results == null || results.isEmpty())
			return null;
		
		return results.stream().min(RESULT_COMPARATOR).get();
	}
	
	private SpectralPowerDistribution mutate(SpectralPowerDistribution spd) {
		
		final Point[] entries = spd.getTable().navigableKeySet().stream().map(k -> spd.get(k))
				.toArray(len -> new Point[len]);
		
		int mutateIndex;
		Point mutatedEntry;
		
		do {
			
			final var mutateFactor = RND.nextDouble() * 0.75d + 0.75d;
			mutateIndex = RND.nextInt(spd.size());
			mutatedEntry = entries[mutateIndex].multiply(mutateFactor);
			
		} while (mutatedEntry.get(0) >= 0d && mutatedEntry.get(0) <= entries[mutateIndex].get(0));
		
		entries[mutateIndex] = mutatedEntry;
		
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
			
			final var candidateResult = evaluateSPD(candidate, target);
			
			return (candidateResult.getDistance() <= ancestor.getDistance()
					&& candidateResult.getBumpiness() <= ancestor.getBumpiness()) ? candidateResult : ancestor;
			
		}
		
		public static SpectrumSearch.Result evaluateSPD(SpectralPowerDistribution spd, XYZ targetColor) {
			
			final XYZ xyz = XYZ.fromSpectrum(spd);
			final double targetDistance = pow(xyz.getX() - targetColor.getX(), 2)
					+ pow(xyz.getY() - targetColor.getY(), 2) + pow(xyz.getZ() - targetColor.getZ(), 2);
			
			final var spdTable = spd.getTable();
			final Point[] spdPoints = spdTable.navigableKeySet().stream().map(k -> spdTable.get(k))
					.toArray(len -> new Point[len]);
			final double bumpinessDistance = IntStream.range(0, spdPoints.length - 1)
					.mapToDouble(i -> pow(spdPoints[i + 1].get(0) - spdPoints[i].get(0), 2)).sum();
			
			return new Result(targetDistance, bumpinessDistance, spd);
		}
		
	}
	
	private SpectralPowerDistribution scaleSPD(SpectralPowerDistribution spd, XYZ targetColor) {
		
		final double brightness = targetColor.getY();
		
		return (SpectralPowerDistribution) spd.multiply(brightness);
	}
	
}
