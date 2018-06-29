package org.snowjak.rays.specgen;

import static org.apache.commons.math3.util.FastMath.max;
import static org.apache.commons.math3.util.FastMath.min;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.Settings;
import org.snowjak.rays.geometry.util.Point;
import org.snowjak.rays.specgen.SpectrumGenerator.StatusReporter;
import org.snowjak.rays.spectrum.colorspace.RGB;
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
	private int elitePersistence;
	private int minGenerations;
	private int maxGenerations;
	private double mutation;
	private int newMemberSeed;
	private int newMemberSeedInterval;
	private double crossover;
	private String newMemberType;
	
	@Value("${parallelism}")
	private int parallelism;
	
	@Value("${distance}")
	private double targetDistance;
	
	@Value("${bumpiness}")
	private double targetBumpiness;
	
	@Value("${bins}")
	private int binCount;
	
	@Value("${min-energy}")
	private double minEnergy;
	
	@Value("${max-energy}")
	private double maxEnergy;
	
	private ListeningExecutorService executor = null;
	
	public StochasticSpectrumSearch() {
		
	}
	
	private static final Comparator<SpectrumSearch.Result> RESULT_COMPARATOR = (r1, r2) -> (Double.compare(
			(int) (r1.getDistance() * 100d) + r1.getBumpiness(), (int) (r2.getDistance() * 100d) + r2.getBumpiness()));
	
	@Override
	public Result doSearch(XYZ targetColor, StatusReporter reporter) {
		
		if (executor == null)
			this.executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(parallelism));
		
		final Supplier<SpectralPowerDistribution> newMemberSupplier;
		switch (newMemberType) {
		case "RANDOM":
			newMemberSupplier = () -> getRandomizedSPD(binCount);
			break;
		case "WHITE":
			newMemberSupplier = () -> Settings.getInstance().getIlluminatorSpectralPowerDistribution();
			break;
		case "EVEN":
			newMemberSupplier = () -> getUniformSPD(binCount, 0.5d);
			break;
		default:
			throw new RuntimeException(
					"Unrecognized new-member selector. Please use one of 'RANDOM', 'WHITE', 'EVEN'.");
		}
		
		List<SpectrumSearch.Result> currentGeneration = new ArrayList<>(generationSize);
		for (int i = 0; i < generationSize; i++)
			currentGeneration.add(SpectrumSearch.evaluateSPD(newMemberSupplier.get(), targetColor));
		
		int generationCount = 0;
		
		var bestResult = getBestResult(currentGeneration);
		do {
			generationCount++;
			
			if (generationCount % 64 == 0)
				LOG.info("Processing generation #{} (best = {})", generationCount,
						XYZ.fromSpectrum(bestResult.getSpd()).to(RGB.class).toString());
			
			final var fixedCurrentGeneration = currentGeneration;
			final List<ListenableFuture<Result>> nextGenerationFutures = new ArrayList<>(generationSize);
			
			fixedCurrentGeneration.stream().sorted(RESULT_COMPARATOR).limit(elitePersistence)
					.forEach(r -> nextGenerationFutures.add(Futures.immediateFuture(r)));
			
			if (generationCount % newMemberSeedInterval == 0)
				IntStream.range(0, newMemberSeed).forEach(i -> nextGenerationFutures.add(
						Futures.immediateFuture(SpectrumSearch.evaluateSPD(getRandomizedSPD(binCount), targetColor))));
			
			while (nextGenerationFutures.size() < generationSize) {
				nextGenerationFutures.add(executor.submit(() -> {
					
					final var parent1 = IntStream.range(0, reproducerPoolSize)
							.mapToObj(i -> fixedCurrentGeneration.get(RND.nextInt(fixedCurrentGeneration.size())))
							.reduce((r1, r2) -> (RESULT_COMPARATOR.compare(r1, r2) == 1) ? r2 : r1).get();
					
					final var parent2 = IntStream.range(0, reproducerPoolSize)
							.mapToObj(i -> fixedCurrentGeneration.get(RND.nextInt(fixedCurrentGeneration.size())))
							.reduce((r1, r2) -> (RESULT_COMPARATOR.compare(r1, r2) == 1) ? r2 : r1).get();
					
					return SpectrumSearch.evaluateSPD(mutate(cross(parent1.getSpd(), parent2.getSpd())), targetColor);
				}));
			}
			
			try {
				currentGeneration = Futures.whenAllSucceed(nextGenerationFutures)
						.call(() -> nextGenerationFutures.stream().map(fr -> {
							try {
								return fr.get();
							} catch (InterruptedException | ExecutionException e) {
								LOG.error("Interrupted!", e);
								return null;
							}
						}).filter(r -> r != null).collect(Collectors.toList()), executor).get();
			} catch (InterruptedException | ExecutionException e) {
				LOG.error("Interrupted!", e);
				return bestResult;
			}
			
			bestResult = getBestResult(currentGeneration);
			reporter.reportResult(bestResult.getDistance(), bestResult.getBumpiness(), bestResult.getSpd());
			
		} while (!(bestResult.getDistance() <= targetDistance && bestResult.getBumpiness() <= targetBumpiness
				&& generationCount > minGenerations) && generationCount < maxGenerations);
		
		executor.shutdown();
		executor = null;
		
		return new Result(bestResult.getDistance(), bestResult.getBumpiness(), bestResult.getSpd().normalize());
	}
	
	private SpectrumSearch.Result getBestResult(Collection<SpectrumSearch.Result> results) {
		
		assert (results != null);
		assert (!results.isEmpty());
		
		return results.stream().min(RESULT_COMPARATOR).get();
	}
	
	private SpectralPowerDistribution getRandomizedSPD(int binCount) {
		
		return new SpectralPowerDistribution(IntStream.range(0, binCount)
				.mapToObj(i -> new Point(RND.nextDouble() * (maxEnergy - minEnergy) + minEnergy))
				.toArray(len -> new Point[len]));
	}
	
	private SpectralPowerDistribution getUniformSPD(int binCount, double value) {
		
		return new SpectralPowerDistribution(
				IntStream.range(0, binCount).mapToObj(i -> new Point(value)).toArray(len -> new Point[len]));
	}
	
	private SpectralPowerDistribution cross(SpectralPowerDistribution spd1, SpectralPowerDistribution spd2) {
		
		if (RND.nextDouble() > crossover)
			return (RND.nextDouble() > 0.5) ? spd2 : spd1;
		
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
		
		for (int i = 0; i < entries.length; i++)
			if (RND.nextDouble() < mutation)
				entries[i] = entries[i].add(RND.nextDouble() * 0.2d - 0.1d).clamp(minEnergy, maxEnergy);
			
		return new SpectralPowerDistribution(spd.getBounds().get(), entries);
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
	
	public int getElitePersistence() {
		
		return elitePersistence;
	}
	
	public void setElitePersistence(int elitePersistence) {
		
		this.elitePersistence = elitePersistence;
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
	
	public double getMutation() {
		
		return mutation;
	}
	
	public void setMutation(double mutation) {
		
		this.mutation = mutation;
	}
	
	public int getNewMemberSeed() {
		
		return newMemberSeed;
	}
	
	public void setNewMemberSeed(int newMemberSeed) {
		
		this.newMemberSeed = newMemberSeed;
	}
	
	public int getNewMemberSeedInterval() {
		
		return newMemberSeedInterval;
	}
	
	public void setNewMemberSeedInterval(int newMemberSeedInterval) {
		
		this.newMemberSeedInterval = newMemberSeedInterval;
	}
	
	public double getCrossover() {
		
		return crossover;
	}
	
	public void setCrossover(double crossover) {
		
		this.crossover = crossover;
	}
	
	public String getNewMemberType() {
		
		return newMemberType;
	}
	
	public void setNewMemberType(String newMemberType) {
		
		this.newMemberType = newMemberType;
	}
	
}
