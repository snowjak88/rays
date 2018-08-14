package org.snowjak.rays.specgen;

import static org.apache.commons.math3.util.FastMath.abs;
import static org.apache.commons.math3.util.FastMath.max;
import static org.apache.commons.math3.util.FastMath.min;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.geometry.util.Point;
import org.snowjak.rays.specgen.SpectrumGenerator.StatusReporter;
import org.snowjak.rays.spectrum.colorspace.XYZ;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("brute-force")
public class BruteForceSpectrumSearch implements SpectrumSearch {
	
	private double searchStep;
	private double searchWindow;
	
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
	
	private ForkJoinPool forkJoinPool = null;
	
	public BruteForceSpectrumSearch() {
		
	}
	
	@Override
	public Result doSearch(XYZ targetColor, SpectralPowerDistribution startingSPD, StatusReporter reporter) {
		
		if (forkJoinPool == null)
			forkJoinPool = new ForkJoinPool(parallelism);
		
		final var spdTable = startingSPD.resize(binCount).getTable();
		final var startingPoints = spdTable.navigableKeySet().stream().map(k -> spdTable.get(k))
				.toArray(len -> new Point[len]);
		
		final var table = startingSPD.resize(binCount).getTable();
		final var vector = table.navigableKeySet().stream().map(k -> table.get(k)).toArray(len -> new Point[len]);
		
		return forkJoinPool.submit(new BruteForceSpectrumSearchRecursiveTask(targetColor, minEnergy, maxEnergy,
				searchWindow, searchStep, startingPoints, reporter, vector)).join();
	}
	
	public static class BruteForceSpectrumSearchRecursiveTask extends RecursiveTask<SpectrumSearch.Result> {
		
		private static final long serialVersionUID = -3715483293390660280L;
		private static final Logger LOG = LoggerFactory.getLogger(BruteForceSpectrumSearchRecursiveTask.class);
		
		private final XYZ target;
		private final double searchMin, searchMax, searchWindow;
		private final double searchStep;
		private final Point[] startingPoints;
		private final StatusReporter reporter;
		private final Point[] vector;
		private final int currentIndex;
		
		public BruteForceSpectrumSearchRecursiveTask(XYZ target, double searchMin, double searchMax,
				double searchWindow, double searchStep, Point[] startingPoints, StatusReporter reporter,
				Point[] vector) {
			
			this(target, searchMin, searchMax, searchWindow, searchStep, startingPoints, reporter, vector, 0);
		}
		
		public BruteForceSpectrumSearchRecursiveTask(XYZ target, double searchMin, double searchMax,
				double searchWindow, double searchStep, Point[] startingPoints, StatusReporter reporter, Point[] vector,
				int currentIndex) {
			
			super();
			
			this.target = target;
			this.searchMin = searchMin;
			this.searchMax = searchMax;
			this.searchWindow = searchWindow;
			this.searchStep = searchStep;
			this.startingPoints = startingPoints;
			this.reporter = reporter;
			this.vector = vector;
			this.currentIndex = currentIndex;
		}
		
		@Override
		protected SpectrumSearch.Result compute() {
			
			SpectrumSearch.Result bestResult = SpectrumSearch.evaluateSPD(rescale(constructSPD(vector), target),
					target);
			final Collection<ForkJoinTask<SpectrumSearch.Result>> subtasks = new LinkedList<>();
			
			final double origin = vector[currentIndex].get(0);
			
			final double windowStart = max(searchMin, origin - abs(searchWindow / 2d));
			final double windowEnd = min(searchMax, origin + abs(searchWindow / 2d));
			
			for (double v = windowStart; v <= windowEnd; v += abs(searchStep)) {
				
				final var newVector = Arrays.copyOf(vector, vector.length);
				newVector[currentIndex] = new Point(v);
				final var spd = rescale(constructSPD(newVector), target);
				final var eval = SpectrumSearch.evaluateSPD(spd, target);
				
				if ((bestResult == null || eval.getDistance() < bestResult.getDistance())
						&& !Double.isNaN(eval.getDistance()) && !Double.isNaN(eval.getBumpiness()))
					bestResult = eval;
				
				if (currentIndex < newVector.length - 1)
					subtasks.add(new BruteForceSpectrumSearchRecursiveTask(target, searchMin, searchMax, searchWindow,
							searchStep, startingPoints, reporter, newVector, currentIndex + 1).fork());
				
			}
			
			for (ForkJoinTask<SpectrumSearch.Result> t : subtasks) {
				var p = t.join();
				if (p.getDistance() < bestResult.getDistance() && !Double.isNaN(p.getDistance())
						&& !Double.isNaN(p.getBumpiness()))
					bestResult = p;
			}
			
			reporter.reportResult(bestResult);
			
			return bestResult;
		}
		
		private SpectralPowerDistribution constructSPD(Point[] vector) {
			
			return new SpectralPowerDistribution(vector);
		}
		
		private SpectralPowerDistribution rescale(SpectralPowerDistribution spd, XYZ targetColor) {
			
			final double targetBrightness = targetColor.getY();
			final double currentBrightness = XYZ.fromSpectrum(spd).getY();
			
			if (targetBrightness == 0d)
				return spd;
			
			return (SpectralPowerDistribution) spd.multiply(targetBrightness / currentBrightness);
		}
	}
	
	public double getSearchStep() {
		
		return searchStep;
	}
	
	public void setSearchStep(double searchStep) {
		
		this.searchStep = searchStep;
	}
	
	public double getSearchWindow() {
		
		return searchWindow;
	}
	
	public void setSearchWindow(double searchWindow) {
		
		this.searchWindow = searchWindow;
	}
	
}