package org.snowjak.rays.spectrum.distribution;

import static org.apache.commons.math3.util.FastMath.exp;
import static org.apache.commons.math3.util.FastMath.pow;

import org.snowjak.rays.Settings;
import org.snowjak.rays.Util;
import org.snowjak.rays.geometry.util.Triplet;

/**
 * Implements {@link ColorMappingFunctionDistribution} using an analytic
 * implementation.
 * <p>
 * Implemented from "Simple Analytic Approximations to the CIE XYZ Color
 * Matching Functions", Wyman/Sloan/Shirley -- "Journal of Computer Graphics
 * Techniques", Vol.2, No.2, 2013
 * </p>
 * 
 * @author snowjak88
 *
 */
public class AnalyticColorMappingFunctionDistribution implements ColorMappingFunctionDistribution {
	
	@Override
	public Triplet get(Double key) {
		
		return new Triplet(getX(key), getY(key), getZ(key));
	}
	
	private double getX(double lambda) {
		
		final double t1 = (lambda - 442.0d) * ((lambda < 442.0d) ? 0.0624d : 0.0374d);
		final double t2 = (lambda - 599.8d) * ((lambda < 599.8d) ? 0.0264d : 0.0323d);
		final double t3 = (lambda - 501.1d) * ((lambda < 501.1d) ? 0.0490d : 0.0382d);
		return 0.362d * exp(-0.5d * pow(t1, 2)) + 1.056d * exp(-0.5d * pow(t2, 2)) - 0.065d * exp(-0.5d * pow(t3, 2));
	}
	
	private double getY(double lambda) {
		
		final double t1 = (lambda - 568.8d) * ((lambda < 568.8d) ? 0.0213d : 0.0247d);
		final double t2 = (lambda - 530.9d) * ((lambda < 530.9d) ? 0.0613d : 0.0322d);
		return 0.821d * exp(-0.5d * pow(t1, 2)) + 0.286d * exp(-0.5f * pow(t2, 2));
	}
	
	private double getZ(double lambda) {
		
		final double t1 = (lambda - 437.0d) * ((lambda < 437.0d) ? 0.0845d : 0.0278d);
		final double t2 = (lambda - 459.0d) * ((lambda < 459.0d) ? 0.0385d : 0.0725d);
		return 1.217d * exp(-0.5d * pow(t1, 2)) + 0.681d * exp(-0.5f * pow(t2, 2));
	}
	
	@Override
	public Triplet averageOver(Double intervalStart, Double intervalEnd) {
		
		final double start = (intervalEnd > intervalStart) ? intervalStart : intervalEnd;
		final double end = (intervalEnd > intervalStart) ? intervalEnd : intervalStart;
		
		return Util.integrateTriplet(start, end, Settings.getInstance().getCieXyzIntegrationStepCount(), (d) -> get(d))
				.divide(end - start);
	}
	
}
