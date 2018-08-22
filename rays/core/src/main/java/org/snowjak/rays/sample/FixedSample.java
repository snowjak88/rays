package org.snowjak.rays.sample;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.snowjak.rays.Settings;
import org.snowjak.rays.geometry.Point2D;

/**
 * A {@link Sample} implementation that contains pregenerated lists of
 * "additional points" (both 1- and 2-D).
 * 
 * <p>
 * Each pregenerated list of additional points is due at construction-time.
 * These lists are each stored with an associated Iterator. When additional
 * points are requested, the corresponding Iterator is advanced. If the Iterator
 * advances completely, the corresponding list is shuffled and its Iterator
 * reset.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class FixedSample implements Sample {
	
	private static final long serialVersionUID = -7458382323573318480L;
	
	private static final List<Double> DEFAULT_ADDITIONAL_1D_SAMPLES = Collections.unmodifiableList(Arrays.asList(0.5d));
	private static final List<Point2D> DEFAULT_ADDITIONAL_2D_SAMPLES = Collections
			.unmodifiableList(Arrays.asList(new Point2D(0.5, 0.5)));
	
	private Point2D filmPoint;
	private Point2D lensUV;
	private double t;
	
	private List<Double> additional1DSamples = new LinkedList<>();
	private List<Point2D> additional2DSamples = new LinkedList<>();
	
	private transient List<Double> shuffledAdditional1DSamples = null;
	private transient List<Point2D> shuffledAdditional2DSamples = null;
	private transient Iterator<Double> next1DSample = null;
	private transient Iterator<Point2D> next2DSample = null;
	
	/**
	 * Construct a new FixedSample with the following defaults:
	 * <dl>
	 * <dt>film-point</dt>
	 * <dd>(0, 0)</dd>
	 * <dt>lens-UV</dt>
	 * <dd>(0.5, 0.5)</dd>
	 * <dt>t</dt>
	 * <dd>0.5</dd>
	 * <dt>additional 1-D samples</dt>
	 * <dd>{ 0.5 }</dd>
	 * <dt>additional 2-D samples</dt>
	 * <dd>{ (0.5,0.5) }</dd>
	 * </dl>
	 */
	public FixedSample() {
		
		this(Point2D.ZERO, Point2D.HALF, 0.5d, DEFAULT_ADDITIONAL_1D_SAMPLES, DEFAULT_ADDITIONAL_2D_SAMPLES);
	}
	
	/**
	 * Construct a new FixedSample with the given film-point, lens-UV,
	 * <code>t</code>, and additional-points.
	 * 
	 * @param sampler
	 * @param filmPoint
	 * @param lensUV
	 * @param t
	 * @param additional1DSamples
	 * @param additional2DSamples
	 */
	public FixedSample(Point2D filmPoint, Point2D lensUV, double t, List<Double> additional1DSamples,
			List<Point2D> additional2DSamples) {
		
		this.filmPoint = filmPoint;
		this.lensUV = lensUV;
		this.t = t;
		this.additional1DSamples = (additional1DSamples == null || additional1DSamples.isEmpty())
				? DEFAULT_ADDITIONAL_1D_SAMPLES
				: additional1DSamples;
		this.additional2DSamples = (additional2DSamples == null || additional2DSamples.isEmpty())
				? DEFAULT_ADDITIONAL_2D_SAMPLES
				: additional2DSamples;
	}
	
	@Override
	public Point2D getFilmPoint() {
		
		return filmPoint;
	}
	
	protected void setFilmPoint(Point2D filmPoint) {
		
		this.filmPoint = filmPoint;
	}
	
	@Override
	public Point2D getLensUV() {
		
		return lensUV;
	}
	
	protected void setLensUV(Point2D lensUV) {
		
		this.lensUV = lensUV;
	}
	
	public List<Double> getAdditional1DSamples() {
		
		return additional1DSamples;
	}
	
	protected void setAdditional1DSamples(List<Double> additional1dSamples) {
		
		additional1DSamples = additional1dSamples;
	}
	
	protected List<Point2D> getAdditional2DSamples() {
		
		return additional2DSamples;
	}
	
	protected void setAdditional2DSamples(List<Point2D> additional2dSamples) {
		
		additional2DSamples = additional2dSamples;
	}
	
	/**
	 * Get another 1-dimensional sample from this FixedSample's list
	 * ({@link #getAdditional1DSamples()}).
	 * <p>
	 * This method is backed by an Iterator. When the backing Iterator reaches the
	 * end of the list of additional 1D samples, that list is shuffled and a new
	 * Iterator created.
	 * </p>
	 * 
	 * @return
	 * @throws NoSuchElementException
	 *             if the underlying list of additional 1D samples is empty
	 */
	@Override
	public double getAdditional1DSample() {
		
		if (next1DSample == null || !next1DSample.hasNext()) {
			
			if (shuffledAdditional1DSamples == null) {
				shuffledAdditional1DSamples = new LinkedList<>();
				shuffledAdditional1DSamples.addAll(additional1DSamples);
			}
			
			synchronized (shuffledAdditional1DSamples) {
				Collections.shuffle(shuffledAdditional1DSamples, Settings.RND);
				next1DSample = shuffledAdditional1DSamples.iterator();
			}
		}
		
		return next1DSample.next();
	}
	
	/**
	 * Get another 2-dimensional sample from this FixedSample's list
	 * ({@link #getAdditional2DSamples()}).
	 * <p>
	 * This method is backed by an Iterator. When the backing Iterator reaches the
	 * end of the list of additional 2D samples, that list is shuffled and a new
	 * Iterator created.
	 * </p>
	 * 
	 * @return
	 * @throws NoSuchElementException
	 *             if the underlying list of additional 2D samples is empty
	 */
	@Override
	public Point2D getAdditional2DSample() {
		
		if (next2DSample == null || !next2DSample.hasNext()) {
			
			if (shuffledAdditional2DSamples == null) {
				shuffledAdditional2DSamples = new LinkedList<>();
				shuffledAdditional2DSamples.addAll(additional2DSamples);
			}
			
			synchronized (shuffledAdditional2DSamples) {
				Collections.shuffle(shuffledAdditional2DSamples, Settings.RND);
				next2DSample = shuffledAdditional2DSamples.iterator();
			}
		}
		
		return next2DSample.next();
	}
	
	@Override
	public double getT() {
		
		return t;
	}
	
	protected void setT(double t) {
		
		this.t = t;
	}
	
}
