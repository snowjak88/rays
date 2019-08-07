package org.snowjak.rays.spectrum.distribution;

import static org.apache.commons.math3.util.FastMath.exp;

import java.util.Optional;

import org.apache.commons.math3.util.Pair;
import org.snowjak.rays.Settings;
import org.snowjak.rays.geometry.util.Triplet;
import org.snowjak.rays.spectrum.ColorMappingFunctions;
import org.snowjak.rays.util.Util;

/**
 * Copied from <a href="http://jcgt.org/published/0002/02/01/">"Simple Analytic
 * Approximations to the CIE XYZ Color Matching Functions"</a>
 * (Wyman/Sloan/Shirley, 2013), 'Journal of Computer Graphics Techniques' (Vol.
 * 2, No. 2, 2013)
 * 
 * @author snowjak88
 *
 */
public class AnalyticColorMappingFunctions implements ColorMappingFunctions, Distribution<Triplet> {
	
	@Override
	public boolean isDefinedAt(double x) {
		
		return true;
	}
	
	@Override
	public Triplet averageOver(double start, double end) {
		
		return Util.integrateTriplet(start, end, Settings.getInstance().getCieXyzIntegrationStepCount(), (l) -> get(l));
	}
	
	@Override
	public Optional<Pair<Double, Double>> getBounds() {
		
		return Optional.empty();
	}
	
	@Override
	public Triplet get(double wavelength) {
		
		return new Triplet(getX(wavelength), getY(wavelength), getZ(wavelength));
	}
	
	private double getX(double lambda) {
		
		final double t1 = (lambda - 442.0d) * ((lambda < 442.0d) ? 0.0624d : 0.0374d);
		final double t2 = (lambda - 599.8d) * ((lambda < 599.8d) ? 0.0264d : 0.0323d);
		final double t3 = (lambda - 501.1d) * ((lambda < 501.1d) ? 0.0490d : 0.0382d);
		return 0.362d * exp(-0.5d * t1 * t1) + 1.056d * exp(-0.5d * t2 * t2) - 0.065d * exp(-0.5d * t3 * t3);
	}
	
	private double getY(double lambda) {
		
		final double t1 = (lambda - 568.8d) * ((lambda < 568.8d) ? 0.0213d : 0.0247d);
		final double t2 = (lambda - 530.9d) * ((lambda < 530.9d) ? 0.0613d : 0.0322d);
		return 0.821d * exp(-0.5d * t1 * t1) + 0.286d * exp(-0.5d * t2 * t2);
	}
	
	private double getZ(double lambda) {
		
		final double t1 = (lambda - 437.0d) * ((lambda < 437.0d) ? 0.0845d : 0.0278d);
		final double t2 = (lambda - 459.0d) * ((lambda < 459.0d) ? 0.0385d : 0.0725d);
		return 1.217d * exp(-0.5d * t1 * t1) + 0.681d * exp(-0.5d * t2 * t2);
	}
	
	@Override
	public boolean isBounded() {
		
		return false;
	}
	
}
