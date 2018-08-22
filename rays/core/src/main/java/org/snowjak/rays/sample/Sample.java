package org.snowjak.rays.sample;

import java.io.Serializable;

import org.snowjak.rays.geometry.Point2D;

/**
 * Represents a single sample-point, along multiple dimensions:
 * <ul>
 * <li>Film - {@link #getFilmPoint()}</li>
 * <li>Lens - {@link #getLensUV()}</li>
 * <li>Time - {@link #getT()}</li>
 * </ul>
 * <p>
 * In addition, this Sample may contain additional 1- and 2-D sample-points for
 * other uses.
 * </p>
 * 
 * @author snowjak88
 *
 */
public interface Sample extends Serializable {
	
	/**
	 * @return this sample's film-point
	 */
	public Point2D getFilmPoint();
	
	/**
	 * @return this sample's lens-point (in the intervals (<code>[0,1]</code>,
	 *         <code>[0,1]</code>)
	 */
	public Point2D getLensUV();
	
	/**
	 * @return this sample's time-point (in the interval <code>[0,1]</code>
	 */
	public double getT();
	
	/**
	 * Return one of this Sample's additional 1-D points, on the interval
	 * <code>[0,1]</code>.
	 * <p>
	 * Note: there is no requirement that this Sample's set of additional 1-D points
	 * be both well-distributed and indefinite in size. Implementations may choose
	 * to pregenerate a set of limited size, iterate through it, and shuffle it
	 * periodically.
	 * </p>
	 * 
	 * @return an additional 1-D point, or <code>0.5</code> by default
	 */
	public default double getAdditional1DSample() {
		
		return 0d;
	}
	
	/**
	 * Return one of this Sample's additional 2-D points, on the interval
	 * (<code>[0,1]</code>, <code>[0,1]</code>).
	 * <p>
	 * Note: there is no requirement that this Sample's set of additional 2-D points
	 * be both well-distributed and indefinite in size. Implementations may choose
	 * to pregenerate a set of limited size, iterate through it, and shuffle it
	 * periodically.
	 * </p>
	 * 
	 * @return an additional 2-D point, or <code>[0.5, 0.5]</code> by default
	 */
	public default Point2D getAdditional2DSample() {
		
		return new Point2D(0.5d, 0.5d);
	}
}
