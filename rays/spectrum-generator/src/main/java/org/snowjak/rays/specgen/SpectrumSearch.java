package org.snowjak.rays.specgen;

import static org.apache.commons.math3.util.FastMath.pow;

import java.util.stream.IntStream;

import org.snowjak.rays.geometry.util.Point;
import org.snowjak.rays.specgen.SpectrumGenerator.StatusReporter;
import org.snowjak.rays.spectrum.colorspace.RGB;
import org.snowjak.rays.spectrum.colorspace.XYZ;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;

public interface SpectrumSearch {
	
	public SpectrumSearch.Result doSearch(XYZ targetColor, SpectralPowerDistribution startingSpd,
			StatusReporter reporter);
	
	public static SpectrumSearch.Result evaluateSPD(SpectralPowerDistribution spd, XYZ targetColor) {
		
		final RGB targetRGB = targetColor.to(RGB.class);
		final XYZ xyz = XYZ.fromSpectrum(spd);
		final RGB rgb = xyz.to(RGB.class);
		
		final double targetDistance = pow(rgb.getRed() - targetRGB.getRed(), 2)
				+ pow(rgb.getGreen() - targetRGB.getGreen(), 2) + pow(rgb.getBlue() - targetRGB.getBlue(), 2);
		
		final var spdTable = spd.getTable();
		final Point[] spdPoints = spdTable.navigableKeySet().stream().map(k -> spdTable.get(k))
				.toArray(len -> new Point[len]);
		final double bumpinessDistance = IntStream.range(0, spdPoints.length - 1)
				.mapToDouble(i -> pow(spdPoints[i + 1].get(0) - spdPoints[i].get(0), 2)).sum();
		
		return new Result(targetDistance, bumpinessDistance, xyz, rgb, spd);
	}
	
	public static class Result {
		
		private final double distance, bumpiness;
		private final XYZ xyz;
		private final RGB rgb;
		private final SpectralPowerDistribution spd;
		
		public Result(double distance, double bumpiness, XYZ xyz, RGB rgb, SpectralPowerDistribution spd) {
			
			this.distance = distance;
			this.bumpiness = bumpiness;
			this.xyz = xyz;
			this.rgb = rgb;
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
		
		public RGB getRgb() {
			
			return rgb;
		}
		
		public SpectralPowerDistribution getSpd() {
			
			return spd;
		}
		
	}
}
