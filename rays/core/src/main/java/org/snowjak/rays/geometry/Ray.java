package org.snowjak.rays.geometry;

import java.io.Serializable;

/**
 * A Ray combines a {@link Point3D} ("origin") and a {@link Vector3D}
 * ("direction") in one object.
 * <p>
 * A Ray has certain core fields that will always be populated:
 * <ul>
 * <li>{@link Point3D} <code>origin</code></li>
 * <li>{@link Vector3D} <code>direction</code></li>
 * <li>double <code>t</code> (default = 0)</li>
 * <li><code>int depth</code> (default = 0)</li>
 * </ul>
 * There are also "window" fields that serve to communicate an interval along
 * the Ray that we are interested in. This may be used by the application to,
 * e.g., restrict intersection-reporting to a specific area of 3-D space.
 * <ul>
 * <li>Window-min-T (default = {@link Double#NEGATIVE_INFINITY})</li>
 * <li>Window-max-T (default = {@link Double#POSITIVE_INFINITY})</li>
 * </ul>
 * </p>
 * 
 * @author snowjak88
 */
public class Ray implements Serializable {
	
	private static final long serialVersionUID = 8542616504681890448L;
	
	private Point3D origin;
	private Vector3D direction;
	private double t;
	
	private int depth;
	
	private double windowMinT = Double.NEGATIVE_INFINITY;
	private double windowMaxT = Double.POSITIVE_INFINITY;
	
	/**
	 * Construct a new Ray with the given origin and direction, and default t of 0
	 * and "ray-depth" of 0.
	 * 
	 * @param origin
	 * @param direction
	 */
	public Ray(Point3D origin, Vector3D direction) {
		
		this(origin, direction, 0d, 0);
	}
	
	/**
	 * Construct a new Ray with the given origin, direction, and t, and default
	 * "ray-depth" of 0.
	 * 
	 * @param origin
	 * @param direction
	 * @param t
	 */
	public Ray(Point3D origin, Vector3D direction, double t) {
		
		this(origin, direction, t, 0);
	}
	
	/**
	 * Construct a new Ray with the given origin, direction, and "ray-depth".
	 * 
	 * @param origin
	 * @param direction
	 * @param t
	 * @param depth
	 */
	public Ray(Point3D origin, Vector3D direction, double t, int depth) {
		
		this(origin, direction, t, depth, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}
	
	/**
	 * Construct a new Ray with the given origin, direction, and "ray-depth", also
	 * supplying values for ray-<em>t</em> windowing.
	 * 
	 * @param origin
	 * @param direction
	 * @param t
	 * @param depth
	 * @param windowMinT
	 * @param windowMaxT
	 */
	public Ray(Point3D origin, Vector3D direction, double t, int depth, double windowMinT, double windowMaxT) {
		
		this.origin = origin;
		this.direction = direction;
		this.t = t;
		this.depth = depth;
		this.windowMinT = windowMinT;
		this.windowMaxT = windowMaxT;
	}
	
	protected Ray() {
		
		this.origin = new Point3D();
		this.direction = new Vector3D();
		this.t = 0;
		this.windowMinT = 0;
		this.windowMaxT = 0;
	}
	
	/**
	 * Given the configured <em>t</em> parameter (see {@link #getT()}), calculate
	 * the corresponding point along this Ray.
	 * 
	 * @return
	 */
	public Point3D getPointAlong() {
		
		return getPointAlong(this.getT());
	}
	
	/**
	 * Given a <em>t</em> parameter, calculate the corresponding point along this
	 * Ray.
	 * 
	 * @param t
	 * @return
	 */
	public Point3D getPointAlong(double t) {
		
		return Point3D.from(origin.add(direction.multiply(t)));
	}
	
	/**
	 * Construct a new {@link Ray} as a copy of this Ray, using a different value
	 * for <em>t</em>.
	 * 
	 * @param t
	 * @return
	 */
	public Ray forT(double t) {
		
		return new Ray(this.getOrigin(), this.getDirection(), t, this.getDepth(), this.getWindowMinT(),
				this.getWindowMaxT());
	}
	
	/**
	 * @param t
	 * @return <code>true</code> if <code>t</code> is within [
	 *         {@link #getWindowMinT()}, {@link #getWindowMaxT()} ]
	 */
	public boolean isInWindow(double t) {
		
		return (t >= windowMinT) && (t <= windowMaxT);
	}
	
	public Point3D getOrigin() {
		
		return origin;
	}
	
	protected void setOrigin(Point3D origin) {
		
		this.origin = origin;
	}
	
	public Vector3D getDirection() {
		
		return direction;
	}
	
	protected void setDirection(Vector3D direction) {
		
		this.direction = direction;
	}
	
	public double getT() {
		
		return t;
	}
	
	protected void setT(double t) {
		
		this.t = t;
	}
	
	public int getDepth() {
		
		return depth;
	}
	
	protected void setDepth(int depth) {
		
		this.depth = depth;
	}
	
	public double getWindowMinT() {
		
		return windowMinT;
	}
	
	public void setWindowMinT(double windowMinT) {
		
		this.windowMinT = windowMinT;
	}
	
	public double getWindowMaxT() {
		
		return windowMaxT;
	}
	
	public void setWindowMaxT(double windowMaxT) {
		
		this.windowMaxT = windowMaxT;
	}
	
	@Override
	public String toString() {
		
		return "Ray:[origin=" + origin.toString() + ", direction=" + direction.toString() + ", t=" + t + ", depth="
				+ depth + ", windowMinT=" + windowMinT + ", windowMaxT=" + windowMaxT + "]";
	}
	
}
