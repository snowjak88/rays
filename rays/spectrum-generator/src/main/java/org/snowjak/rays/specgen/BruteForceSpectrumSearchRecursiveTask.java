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

public class BruteForceSpectrumSearchRecursiveTask
		extends RecursiveTask<Pair<Pair<Double, Double>, SpectralPowerDistribution>> {
	
	private static final long serialVersionUID = -3715483293390660280L;
	
	private final XYZ originalTarget, target;
	private final Pair<Double, Double> multiplierRange;
	private final double incStep;
	private final Point[] startingPoints;
	private final StatusReporter reporter;
	private final Point[] vector;
	private final int currentIndex;
	
	public BruteForceSpectrumSearchRecursiveTask(XYZ originalTarget, XYZ target, Pair<Double, Double> multiplierRange,
			double incStep, Point[] startingPoints, StatusReporter reporter, Point[] vector) {
		
		this(originalTarget, target, multiplierRange, incStep, startingPoints, reporter, vector, 0);
	}
	
	public BruteForceSpectrumSearchRecursiveTask(XYZ originalTarget, XYZ target, Pair<Double, Double> multiplierRange,
			double incStep, Point[] startingPoints, StatusReporter reporter, Point[] vector, int currentIndex) {
		
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
	protected Pair<Pair<Double, Double>, SpectralPowerDistribution> compute() {
		
		Pair<Pair<Double, Double>, SpectralPowerDistribution> bestResult = null;
		final Collection<ForkJoinTask<Pair<Pair<Double, Double>, SpectralPowerDistribution>>> subtasks = new LinkedList<>();
		
		for (double v = multiplierRange.getFirst(); v <= multiplierRange.getSecond(); v += incStep) {
			
			final var mutatedPoint = startingPoints[currentIndex].multiply(v);
			if (mutatedPoint.get(0) < 0d || mutatedPoint.get(0) > 1d)
				continue;
			
			vector[currentIndex] = mutatedPoint;
			final var spd = constructSPD(vector);
			final var eval = evaluateSPD(spd);
			
			if (bestResult == null)
				bestResult = new Pair<>(eval, scaleSPD(spd, originalTarget));
			
			else if (eval.getFirst() <= bestResult.getKey().getFirst()
					&& eval.getSecond() <= bestResult.getKey().getSecond())
				bestResult = new Pair<>(eval, scaleSPD(spd, originalTarget));
			
			if (currentIndex < vector.length - 1)
				subtasks.add(new BruteForceSpectrumSearchRecursiveTask(originalTarget, target, multiplierRange, incStep,
						startingPoints, reporter, vector, currentIndex + 1).fork());
		}
		
		for (ForkJoinTask<Pair<Pair<Double, Double>, SpectralPowerDistribution>> t : subtasks) {
			var p = t.join();
			if (p.getKey().getFirst() <= bestResult.getKey().getFirst()
					&& p.getKey().getSecond() <= bestResult.getKey().getSecond())
				bestResult = p;
		}
		
		reporter.reportResult(bestResult.getKey().getFirst(), bestResult.getKey().getSecond(), bestResult.getValue());
		
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
