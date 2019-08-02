package org.snowjak.rays.sample;

import java.util.Collections;
import java.util.function.Supplier;

import org.snowjak.rays.geometry.Point2D;

/**
 * A modification of {@link FixedSample} that references {@link Supplier}s for
 * its additional 1D and 2D sample-sets.
 * 
 * @author snowjak88
 *
 */
public class SuppliedSample extends FixedSample {
	
	private static final long serialVersionUID = -4376543982076190688L;
	
	private transient Supplier<Double> additional1dSupplier;
	private transient Supplier<Point2D> additional2dSupplier;
	
	/**
	 * 
	 */
	public SuppliedSample() {
		
	}
	
	/**
	 * @param filmPoint
	 * @param lensUV
	 * @param t
	 * @param additional1dSamples
	 * @param additional2dSamples
	 */
	public SuppliedSample(Point2D filmPoint, Point2D lensUV, double t, Supplier<Double> additional1dSupplier,
			Supplier<Point2D> additional2dSupplier) {
		
		super(filmPoint, lensUV, t, Collections.emptyList(), Collections.emptyList());
		this.additional1dSupplier = additional1dSupplier;
		this.additional2dSupplier = additional2dSupplier;
	}
	
	@Override
	public double getAdditional1DSample() {
		
		return additional1dSupplier.get();
	}
	
	@Override
	public Point2D getAdditional2DSample() {
		
		return additional2dSupplier.get();
	}
	
}
