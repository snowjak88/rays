package org.snowjak.rays.specgen;

import static org.apache.commons.math3.util.FastMath.*;

import java.util.Arrays;
import java.util.concurrent.Callable;

import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.PowellOptimizer;
import org.snowjak.rays.Settings;
import org.snowjak.rays.spectrum.colorspace.RGB;
import org.snowjak.rays.spectrum.colorspace.XYZ;
import org.snowjak.rays.spectrum.distribution.TabulatedColorMappingFunctionDistribution;
import org.snowjak.rays.spectrum.distribution.TabulatedSpectralPowerDistribution;

public class OtherSpectrumGeneratorJob implements Callable<TabulatedSpectralPowerDistribution> {
	
	private final XYZ target;
	private final int binCount;
	
	public OtherSpectrumGeneratorJob(XYZ target, int binCount) {
		
		this.target = target;
		this.binCount = binCount;
	}
	
	public TabulatedSpectralPowerDistribution call() {
		
		final var d65 = Settings.getInstance().getIlluminatorSpectralPowerDistribution();
		final var d65Table = d65.toTabulatedForm(TabulatedSpectralPowerDistribution::new, d65.getLowestWavelength(),
				d65.getHighestWavelength(), this.binCount);
		
		final var cmf = Settings.getInstance().getColorMappingFunctionDistribution();
		final var cmfTable = cmf.toTabulatedForm(TabulatedColorMappingFunctionDistribution::new,
				cmf.getLowestWavelength(), cmf.getHighestWavelength(), this.binCount);
		
		final var lowWavelength = max(d65Table.getLowestWavelength(), cmfTable.getLowestWavelength());
		final var highWavelength = min(d65Table.getHighestWavelength(), cmfTable.getHighestWavelength());
		final var stepSize = (highWavelength - lowWavelength) / ((double) binCount - 1d);
		
		final double[] d65Measurements = new double[d65Table.getAll().size()];
		int i = 0;
		var d65Iterator = d65Table.getAll().stream().sorted((p1, p2) -> Double.compare(p1.getKey(), p2.getKey()))
				.iterator();
		while (d65Iterator.hasNext())
			d65Measurements[i++] = d65Iterator.next().getValue();
		
		RealMatrix cmfMatrix = new BlockRealMatrix(3, cmfTable.getAll().size());
		i = 0;
		var cmfIterator = cmfTable.getAll().stream().sorted((p1, p2) -> Double.compare(p1.getKey(), p2.getKey()))
				.iterator();
		while (cmfIterator.hasNext())
			cmfMatrix.setColumn(i++, cmfIterator.next().getValue().getAll());
		
		//@formatter:off
		final var result =
				new PowellOptimizer(1e-12, 1e-4).optimize(
						new ObjectiveFunction(spec -> computeCostFunction(spec, lowWavelength, highWavelength)),
						new MaxEval(Integer.MAX_VALUE),
						GoalType.MINIMIZE,
						new InitialGuess(d65Measurements));
		//@formatter:on
		
		System.out.println("Key: " + Arrays.toString(result.getKey()));
		System.out.println("Value: " + result.getSecond().toString());
		
		final var resultingSPD = new TabulatedSpectralPowerDistribution();
		
		double lambda = lowWavelength;
		for (i = 0; i < result.getKey().length; i++) {
			resultingSPD.addEntry(lambda, result.getKey()[i]);
			lambda += stepSize;
		}
		
		final var resultingXYZ = XYZ.fromSpectrum(resultingSPD);
		System.out.println("Resulting XYZ: " + resultingXYZ.toString());
		System.out.println("Resulting RGB: " + resultingXYZ.to(RGB.class).toString());
		
		return resultingSPD;
	}
	
	private double[] xyzFromSpectrum(double[] spec, double lowWavelength, double highWavelength) {
		
		final var spd = new TabulatedSpectralPowerDistribution();
		final var stepSize = (highWavelength - lowWavelength) / ((double) spec.length - 1d);
		
		var lambda = lowWavelength;
		for (int i = 0; i < spec.length; i++) {
			spd.addEntry(lambda, spec[i]);
			lambda += stepSize;
		}
		
		return XYZ.fromSpectrum(spd).get().getAll();
	}
	
	private double computeCostFunction(double[] model, double lowWavelength, double highWavelength) {
		
		final double[] modelXYZ = xyzFromSpectrum(model, lowWavelength, highWavelength);
		final double distanceCost = pow(max(modelXYZ[0], 0) - target.getX(), 2) + pow(modelXYZ[1] - target.getY(), 2)
				+ pow(modelXYZ[2] - target.getZ(), 2);
		
		double bumpinessCost = 0d;
		for (int i = 0; i < model.length - 1; i++)
			bumpinessCost += pow(model[i] - model[i + 1], 2);
		
		double outOfBoundsCost = 0d;
		for (int i = 0; i < model.length; i++)
			if (model[i] < 0)
				outOfBoundsCost += pow(model[i], 8);
			else if (model[i] > 1)
				outOfBoundsCost += pow(model[i] - 1d, 8);
			
		return distanceCost + bumpinessCost + outOfBoundsCost;
	}
	
}