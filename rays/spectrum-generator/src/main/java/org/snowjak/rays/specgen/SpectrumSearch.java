package org.snowjak.rays.specgen;

import static org.apache.commons.math3.util.FastMath.pow;

import java.util.stream.IntStream;

import org.snowjak.rays.geometry.util.Point;
import org.snowjak.rays.specgen.SpectrumGenerator.StatusReporter;
import org.snowjak.rays.spectrum.colorspace.XYZ;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;

public interface SpectrumSearch {
	
	public SpectrumSearch.Result doSearch(XYZ targetColor, SpectralPowerDistribution startingSPD,
			StatusReporter reporter);
	
	public static SpectrumSearch.Result evaluateSPD(SpectralPowerDistribution spd, XYZ targetColor) {
		
		final XYZ xyz = XYZ.fromSpectrum(spd);
		final double targetDistance = pow(xyz.getX() - targetColor.getX(), 2) + pow(xyz.getY() - targetColor.getY(), 2)
				+ pow(xyz.getZ() - targetColor.getZ(), 2);
		
		final var spdTable = spd.getTable();
		final Point[] spdPoints = spdTable.navigableKeySet().stream().map(k -> spdTable.get(k))
				.toArray(len -> new Point[len]);
		final double bumpinessDistance = IntStream.range(0, spdPoints.length - 1)
				.mapToDouble(i -> pow(spdPoints[i + 1].get(0) - spdPoints[i].get(0), 2)).sum();
		
		return new Result(targetDistance, bumpinessDistance, spd);
	}
	
	public static class Result {
		
		private final double distance, bumpiness;
		private final SpectralPowerDistribution spd;
		
		public Result(double distance, double bumpiness, SpectralPowerDistribution spd) {
			
			this.distance = distance;
			this.bumpiness = bumpiness;
			this.spd = spd;
		}
		
		public double getDistance() {
			
			return distance;
		}
		
		public double getBumpiness() {
			
			return bumpiness;
		}
		
		public SpectralPowerDistribution getSpd() {
			
			return spd;
		}
		
	}
}
