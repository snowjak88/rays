package org.snowjak.rays.spectrum;

import static org.apache.commons.math3.util.FastMath.*;
import org.apache.commons.math3.fitting.leastsquares.GaussNewtonOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer.Optimum;
import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleVectorValueChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;
import org.snowjak.rays.geometry.util.Triplet;

/**
 * Represents a {@link Spectrum} by using a CIE XYZ tristimulus triplet
 * ({@link CIEXYZ}, assumed to be in the CIE 1931 color-space) to construct a
 * spectrum from which that XYZ triplet can be derived.
 * <p>
 * <strong>Note</strong> that, since there are an indefinite number of distinct
 * spectra from which you can derive the same XYZ triplet, this construction is
 * fundamentally arbitrary.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class ConstructedSpectrum implements Spectrum {
	
	private static final long serialVersionUID = 1528621571235637519L;
	
	public ConstructedSpectrum(CIEXYZ xyz) {
		
	}
	
	@Override
	public boolean isBlack() {
		
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public Spectrum add(Spectrum addend) {
		
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Spectrum multiply(Spectrum multiplicand) {
		
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Spectrum multiply(double scalar) {
		
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public double getAmplitude() {
		
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public RGB toRGB() {
		
		return null;
	}
	
	/**
	 * Helper class which uses numerical methods to find a spectrum that fits the
	 * given CIE-XYZ triplet.
	 * 
	 * @author snowjak88
	 *
	 */
	public static class Finder {
		
		/**
		 * Find a spectrum (broken into <em>n</em> bins of equal frequency-spread) such
		 * that the found spectrum evaluates to the given CIE-XYZ triplet.
		 * 
		 * @param spectrumBins
		 * @param xyz
		 * @return
		 */
		public static double[] find(int spectrumBins, CIEXYZ xyz) {
			
			final var simplex = new NelderMeadSimplex(spectrumBins);
			
			//
			// Initialize the simplex's starting point to an approximation of the D65
			// standard illuminator (down-sampled to match the given number of bins).
			final var binnedD65 = new double[spectrumBins];
			
			//
			// Note that we look only at that region of the D65 which our
			// color-mapping-functions also cover.
			final var d65 = CIEXYZ.D65_STANDARD_ILLUMINATOR_SPECTRUM;
			final var lowWavelength = max(d65.firstKey(), CIEXYZ.COLOR_MAPPING_FUNCTIONS.firstKey());
			final var highWavelength = min(d65.lastKey(), CIEXYZ.COLOR_MAPPING_FUNCTIONS.lastKey());
			
			final var binWidth = (highWavelength - lowWavelength) / ((double) spectrumBins);
			double currentBinWidth = 0d;
			int currentBin = 0, entriesInBin = 0;
			var currentD65Entry = d65.floorEntry(lowWavelength);
			while (currentBin < binnedD65.length) {
				
				binnedD65[currentBin] += currentD65Entry.getValue();
				entriesInBin++;
				
				var prevD65Entry = currentD65Entry;
				currentD65Entry = d65.ceilingEntry(currentD65Entry.getKey());
				currentBinWidth += (currentD65Entry.getKey() - prevD65Entry.getKey());
				
				if (currentBinWidth >= binWidth) {
					currentBinWidth -= binWidth;
					binnedD65[currentBin] /= entriesInBin;
					currentBin++;
					entriesInBin = 0;
				}
			}
			
			simplex.build(binnedD65);
			
			//
			//
			simplex.evaluate(spectrum -> evaluate(spectrum), (p1, p2) -> compare(p1, p2));
			
			return new double[] { 0d };
		}
		
		private static double evaluate(double[] spectrum) {
			
			return 0d;
		}
		
		private static int compare(PointValuePair p1, PointValuePair p2) {
			
			return 0;
		}
	}
}
