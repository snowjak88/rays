/**
 * 
 */
package org.snowjak.rays.specgen;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.snowjak.rays.geometry.util.Point;
import org.snowjak.rays.specgen.SpectrumGenerator.StatusReporter;
import org.snowjak.rays.spectrum.colorspace.RGB;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * A {@link SpectrumSearch} that attempts to identify a
 * {@link SpectralPowerDistribution} that is completely uniform -- i.e., with
 * all measurements of equal power -- that produces a color that closely matches
 * the given target-color.
 * 
 * @author snowjak88
 *
 */
@Component
@ConfigurationProperties("uniform")
public class UniformSpectrumSearch implements SpectrumSearch {
	
	private int maxIterations;
	
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
	
	@Override
	public Result doSearch(BiFunction<SpectralPowerDistribution, RGB, Result> distanceCalculator, RGB targetColor,
			Supplier<SpectralPowerDistribution> startingSpdSupplier, StatusReporter reporter) {
		
		final var startingSPD = startingSpdSupplier.get();
		
		var bestResult = evaluateSPD(startingSPD, targetColor, distanceCalculator);
		
		reporter.reportResult(bestResult);
		
		if (bestResult.getDistance() <= targetDistance && bestResult.getBumpiness() <= targetBumpiness)
			return bestResult;
		
		final var forkPool = new ForkJoinPool(parallelism);
		
		final var searchResult = forkPool.submit(new UniformSearchTask(this, minEnergy, maxEnergy, distanceCalculator,
				targetColor, reporter, 0, maxIterations)).join();
		
		if (searchResult.getDistance() <= bestResult.getDistance()
				&& searchResult.getBumpiness() <= bestResult.getBumpiness())
			return searchResult;
		
		return bestResult;
	}
	
	public static class UniformSearchTask extends RecursiveTask<Result> {
		
		private static final long serialVersionUID = 6771736131365384098L;
		
		private final UniformSpectrumSearch searcher;
		private final double low, high;
		private final BiFunction<SpectralPowerDistribution, RGB, Result> distanceCalculator;
		private final RGB targetColor;
		private final StatusReporter reporter;
		private final int depth;
		private final int maxDepth;
		
		public UniformSearchTask(UniformSpectrumSearch searcher, double low, double high,
				BiFunction<SpectralPowerDistribution, RGB, Result> distanceCalculator, RGB targetColor,
				StatusReporter reporter, int depth, int maxDepth) {
			
			this.searcher = searcher;
			this.low = low;
			this.high = high;
			this.distanceCalculator = distanceCalculator;
			this.targetColor = targetColor;
			this.reporter = reporter;
			this.depth = depth;
			this.maxDepth = maxDepth;
		}
		
		@Override
		protected Result compute() {
			
			final var midpoint = (low + high) / 2d;
			final var midResult = searcher.evaluateSPD(searcher.buildSPD(new Point(midpoint)), targetColor,
					distanceCalculator);
			
			reporter.reportResult(midResult);
			
			if (midResult.getDistance() <= searcher.targetDistance
					&& midResult.getBumpiness() <= searcher.targetBumpiness)
				return midResult;
			
			if (depth < maxDepth) {
				final var lowResultTask = getPool().submit(new UniformSearchTask(searcher, low, midpoint,
						distanceCalculator, targetColor, reporter, depth + 1, maxDepth));
				final var highResultTask = getPool().submit(new UniformSearchTask(searcher, midpoint, high,
						distanceCalculator, targetColor, reporter, depth + 1, maxDepth));
				
				final var lowResult = lowResultTask.join();
				final var highResult = highResultTask.join();
				
				if (lowResult.getDistance() < midResult.getDistance()
						&& lowResult.getDistance() < highResult.getDistance())
					return lowResult;
				else if (midResult.getDistance() < lowResult.getDistance()
						&& midResult.getDistance() < highResult.getDistance())
					return midResult;
				else
					return highResult;
				
			} else
				return midResult;
		}
		
	}
	
	public SpectralPowerDistribution buildSPD(Point value) {
		
		final var values = new Point[binCount];
		Arrays.fill(values, value);
		return new SpectralPowerDistribution(values);
	}
	
	public int getMaxIterations() {
		
		return maxIterations;
	}
	
	public void setMaxIterations(int maxIterations) {
		
		this.maxIterations = maxIterations;
	}
	
}
