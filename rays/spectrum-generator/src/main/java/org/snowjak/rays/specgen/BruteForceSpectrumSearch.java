package org.snowjak.rays.specgen;

import static org.apache.commons.math3.util.FastMath.pow;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.stream.IntStream;

import org.apache.commons.math3.util.Pair;
import org.snowjak.rays.geometry.util.Point;
import org.snowjak.rays.specgen.SpectrumGenerator.StatusReporter;
import org.snowjak.rays.spectrum.colorspace.XYZ;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;

public class BruteForceSpectrumSearch implements SpectrumSearch {
	
	private final XYZ originalTarget;
	private final XYZ target;
	private final Pair<Double, Double> multiplierRange;
	private final double incStep;
	private final Point[] startingPoints;
	private final StatusReporter reporter;
	private final Point[] vector;
	
	public BruteForceSpectrumSearch(int binCount, XYZ target, SpectralPowerDistribution startingSPD, double incStep,
			StatusReporter reporter) {
		
		this.originalTarget = target;
		this.target = target.normalize();
		this.multiplierRange = new Pair<>(0d, 2d);
		this.incStep = incStep;
		
		final var spdTable = startingSPD.getTable();
		this.startingPoints = spdTable.navigableKeySet().stream().map(k -> spdTable.get(k))
				.toArray(len -> new Point[len]);
		
		this.reporter = reporter;
		
		final var table = startingSPD.resize(binCount).getTable();
		this.vector = table.navigableKeySet().stream().map(k -> table.get(k)).toArray(len -> new Point[len]);
	}
	
	@Override
	public Result doSearch() {
		
		return new BruteForceSpectrumSearchRecursiveTask(originalTarget, target, multiplierRange, incStep,
				startingPoints, reporter, vector).fork().join();
	}
	
	public static class BruteForceSpectrumSearchRecursiveTask extends RecursiveTask<SpectrumSearch.Result> {
		
		private static final long serialVersionUID = -3715483293390660280L;
		
		private final XYZ originalTarget, target;
		private final Pair<Double, Double> multiplierRange;
		private final double incStep;
		private final Point[] startingPoints;
		private final StatusReporter reporter;
		private final Point[] vector;
		private final int currentIndex;
		
		public BruteForceSpectrumSearchRecursiveTask(XYZ originalTarget, XYZ target,
				Pair<Double, Double> multiplierRange, double incStep, Point[] startingPoints, StatusReporter reporter,
				Point[] vector) {
			
			this(originalTarget, target, multiplierRange, incStep, startingPoints, reporter, vector, 0);
		}
		
		public BruteForceSpectrumSearchRecursiveTask(XYZ originalTarget, XYZ target,
				Pair<Double, Double> multiplierRange, double incStep, Point[] startingPoints, StatusReporter reporter,
				Point[] vector, int currentIndex) {
			
			super();
			
			this.originalTarget = originalTarget;
			this.target = target;
			this.multiplierRange = multiplierRange;
			this.incStep = incStep;
			this.startingPoints = startingPoints;
			this.reporter = reporter;
			this.vector = vector;
			this.currentIndex = currentIndex;
		}
		
		@Override
		protected SpectrumSearch.Result compute() {
			
			SpectrumSearch.Result bestResult = null;
			final Collection<ForkJoinTask<SpectrumSearch.Result>> subtasks = new LinkedList<>();
			
			for (double v = multiplierRange.getFirst(); v <= multiplierRange.getSecond(); v += incStep) {
				
				final var mutatedPoint = startingPoints[currentIndex].multiply(v);
				if (mutatedPoint.get(0) < 0d || mutatedPoint.get(0) > 1d)
					continue;
				
				vector[currentIndex] = mutatedPoint;
				final var spd = constructSPD(vector);
				final var eval = evaluateSPD(spd);
				
				if (bestResult == null || (eval.getFirst() <= bestResult.getDistance()
						&& eval.getSecond() <= bestResult.getBumpiness()))
					bestResult = new Result(eval.getFirst(), eval.getSecond(), scaleSPD(spd, originalTarget));
				
				if (currentIndex < vector.length - 1)
					subtasks.add(new BruteForceSpectrumSearchRecursiveTask(originalTarget, target, multiplierRange,
							incStep, startingPoints, reporter, vector, currentIndex + 1).fork());
			}
			
			for (ForkJoinTask<SpectrumSearch.Result> t : subtasks) {
				var p = t.join();
				if (p.getDistance() <= bestResult.getDistance() && p.getBumpiness() <= bestResult.getBumpiness())
					bestResult = p;
			}
			
			reporter.reportResult(bestResult.getDistance(), bestResult.getBumpiness(), bestResult.getSpd());
			
			return bestResult;
		}
		
		public Pair<Double, Double> evaluateSPD(SpectralPowerDistribution spd) {
			
			final XYZ xyz = XYZ.fromSpectrum(spd);
			final double targetDistance = pow(xyz.getX() - target.getX(), 2) + pow(xyz.getY() - target.getY(), 2)
					+ pow(xyz.getZ() - target.getZ(), 2);
			
			final var spdTable = spd.getTable();
			final Point[] spdPoints = spdTable.navigableKeySet().stream().map(k -> spdTable.get(k))
					.toArray(len -> new Point[len]);
			final double bumpinessDistance = IntStream.range(0, spdPoints.length - 1)
					.mapToDouble(i -> pow(spdPoints[i + 1].get(0) - spdPoints[i].get(0), 2)).sum();
			
			return new Pair<>(targetDistance, bumpinessDistance);
		}
		
		private SpectralPowerDistribution constructSPD(Point[] vector) {
			
			return new SpectralPowerDistribution(vector);
		}
		
		private SpectralPowerDistribution scaleSPD(SpectralPowerDistribution spd, XYZ targetColor) {
			
			final double brightness = targetColor.getY();
			
			return (SpectralPowerDistribution) spd.multiply(brightness);
		}
		
	}
}