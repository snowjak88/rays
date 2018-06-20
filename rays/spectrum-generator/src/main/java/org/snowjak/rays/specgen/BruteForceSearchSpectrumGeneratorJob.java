package org.snowjak.rays.specgen;

import static org.apache.commons.math3.util.FastMath.pow;

import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.stream.IntStream;

import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.Settings;
import org.snowjak.rays.geometry.util.Point;
import org.snowjak.rays.spectrum.colorspace.XYZ;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;

public class BruteForceSearchSpectrumGeneratorJob implements Runnable {
	
	private static final Logger LOG = LoggerFactory.getLogger(BruteForceSearchSpectrumGeneratorJob.class);
	
	private final XYZ target, originalTarget;
	
	private final SpectralPowerDistribution startingSPD;
	private final Point[] startingPoints;
	
	private final int binCount;
	
	private final Pair<Double, Double> multiplierRange;
	private final double incStep;
	private final double targetDistance;
	
	private final BlockingQueue<Pair<Pair<Double, Double>, SpectralPowerDistribution>> resultQueue;
	
	public BruteForceSearchSpectrumGeneratorJob(XYZ target,
			BlockingQueue<Pair<Pair<Double, Double>, SpectralPowerDistribution>> resultQueue, double targetDistance) {
		
		this(target, resultQueue, Settings.getInstance().getIlluminatorSpectralPowerDistribution(),
				Settings.getInstance().getSpectrumBinCount(), new Pair<>(0d, 1d), 0.1, targetDistance);
	}
	
	public BruteForceSearchSpectrumGeneratorJob(XYZ target,
			BlockingQueue<Pair<Pair<Double, Double>, SpectralPowerDistribution>> resultQueue,
			SpectralPowerDistribution startingSPD, int binCount, Pair<Double, Double> multiplierRange, double incStep,
			double targetDistance) {
		
		assert (target != null);
		assert (resultQueue != null);
		assert (startingSPD != null);
		assert (binCount > 1);
		assert (multiplierRange != null);
		assert (multiplierRange.getFirst() <= multiplierRange.getSecond());
		assert (incStep > 0d);
		assert (targetDistance > 0d);
		
		this.originalTarget = target;
		this.target = target.normalize();
		
		this.resultQueue = resultQueue;
		
		this.startingSPD = startingSPD;
		
		final var spdTable = startingSPD.getTable();
		this.startingPoints = spdTable.navigableKeySet().stream().map(k -> spdTable.get(k))
				.toArray(len -> new Point[len]);
		
		this.binCount = binCount;
		
		this.multiplierRange = multiplierRange;
		this.incStep = incStep;
		this.targetDistance = targetDistance;
	}
	
	@Override
	public void run() {
		
		final Point[] vector;
		
		if (startingSPD == null) {
			vector = new Point[this.binCount];
			Arrays.fill(vector, new Point(1.0d));
		} else {
			final var table = startingSPD.resize(this.binCount).getTable();
			vector = table.navigableKeySet().stream().map(k -> table.get(k)).toArray(len -> new Point[len]);
		}
		
		try {
			
			depthSearchVector(vector);
			
		} catch (InterruptedException e) {
			// Do nothing if interrupted
		}
	}
	
	private void depthSearchVector(Point[] vector) throws InterruptedException {
		
		depthSearchVector(vector, 0);
	}
	
	private void depthSearchVector(Point[] vector, int currentIndex) throws InterruptedException {
		
		if (currentIndex >= vector.length)
			return;
		
		for (double v = multiplierRange.getFirst(); v <= multiplierRange.getSecond(); v += incStep) {
			
			final var mutatedPoint = startingPoints[currentIndex].multiply(v);
			if( mutatedPoint.get(0) < 0d || mutatedPoint.get(0) > 1d )
				continue;
			
			vector[currentIndex] = mutatedPoint;
			final var spd = constructSPD(vector);
			
			final var resultingEval = evaluateSPD(spd);
			if (resultingEval.getFirst() <= this.targetDistance) {
				LOG.info("Found spectrum for {} within target-distance. Distance = {}, bumpiness = {}",
						this.originalTarget.toString(), resultingEval.getFirst(), resultingEval.getSecond());
				resultQueue.put(new Pair<>(resultingEval, scaleSPD(spd, originalTarget)));
			}
			
			depthSearchVector(vector, currentIndex + 1);
		}
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
