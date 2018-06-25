package org.snowjak.rays.specgen;

import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;

public interface SpectrumSearch {
	
	public SpectrumSearch.Result doSearch();
	
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
