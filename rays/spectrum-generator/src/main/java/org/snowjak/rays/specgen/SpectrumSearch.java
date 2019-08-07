package org.snowjak.rays.specgen;

import static org.apache.commons.math3.util.FastMath.abs;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.snowjak.rays.geometry.util.Point;
import org.snowjak.rays.specgen.SpectrumGenerator.StatusReporter;
import org.snowjak.rays.spectrum.colorspace.RGB;
import org.snowjak.rays.spectrum.colorspace.RGB_Gammaless;
import org.snowjak.rays.spectrum.colorspace.XYZ;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;

public interface SpectrumSearch {
	
	public SpectrumSearch.Result doSearch(
			BiFunction<SpectralPowerDistribution, RGB, SpectrumSearch.Result> distanceCalculator, RGB targetColor,
			Supplier<SpectralPowerDistribution> startingSpdSupplier, StatusReporter reporter);
	
	public default SpectrumSearch.Result evaluateSPD(SpectralPowerDistribution spd, RGB targetColor,
			BiFunction<SpectralPowerDistribution, RGB, SpectrumSearch.Result> distanceCalculator) {
		
		final Result targetResult = distanceCalculator.apply(spd, targetColor);
		
		final var spdTable = spd.getTable();
		final Point[] spdPoints = spdTable.navigableKeySet().stream().map(k -> spdTable.get(k))
				.toArray(len -> new Point[len]);
		final double bumpinessDistance = IntStream.range(0, spdPoints.length - 1)
				.mapToDouble(i -> abs(spdPoints[i + 1].get(0) - spdPoints[i].get(0))).sum() / (double) spdPoints.length;
		
		return new Result(targetResult.getDistance(), bumpinessDistance, targetResult.getXyz(), targetResult.getRgb(),
				targetResult.getSrgb(), spd);
	}
	
	public static class Result {
		
		private final double distance, bumpiness;
		private final XYZ xyz;
		private final RGB_Gammaless rgb;
		private final RGB srgb;
		private final SpectralPowerDistribution spd;
		
		public Result(double distance, double bumpiness, XYZ xyz, RGB_Gammaless rgb, RGB srgb,
				SpectralPowerDistribution spd) {
			
			this.distance = distance;
			this.bumpiness = bumpiness;
			this.xyz = xyz;
			this.rgb = rgb;
			this.srgb = srgb;
			this.spd = spd;
		}
		
		public double getDistance() {
			
			return distance;
		}
		
		public double getBumpiness() {
			
			return bumpiness;
		}
		
		public XYZ getXyz() {
			
			return xyz;
		}
		
		public RGB_Gammaless getRgb() {
			
			return rgb;
		}
		
		public RGB getSrgb() {
			
			return srgb;
		}
		
		public SpectralPowerDistribution getSpd() {
			
			return spd;
		}
		
	}
}
