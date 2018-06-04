package org.snowjak.rays.transform;

import org.snowjak.rays.geometry.Normal3D;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.geometry.util.Matrix;

/**
 * Represents a scaling Transform in 3-space.
 * 
 * @author snowjak88
 */
public class ScaleTransform implements Transform {

	private double	sx;
	private double	sy;
	private double	sz;

	private Matrix	worldToLocal					= null;
	private Matrix	worldToLocal_inverseTranspose	= null;
	private Matrix	localToWorld					= null;
	private Matrix	localToWorld_inverseTranspose	= null;

	/**
	 * Create a new ScaleTransform, with the specified
	 * <strong>world-to-local</strong> scaling terms.
	 * 
	 * @param sx
	 * @param sy
	 * @param sz
	 */
	public ScaleTransform(double sx, double sy, double sz) {

		this.sx = sx;
		this.sy = sy;
		this.sz = sz;
	}

	private void initializeMatrices() {

		//@formatter:off
		this.localToWorld = new Matrix(new double[][] {	{ sx,    0d,    0d,    0d },
														{ 0d,    sy,    0d,    0d },
														{ 0d,    0d,    sz,    0d },
														{ 0d,    0d,    0d,    1d } });
		this.worldToLocal = new Matrix(new double[][] {	{ 1d/sx, 0d,    0d,    0d },
														{ 0d,    1d/sy, 0d,    0d },
														{ 0d,    0d,    1d/sz, 0d },
														{ 0d,    0d,    0d,    1d } });
		//@formatter:on
	}

	@Override
	public Point3D worldToLocal(Point3D point) {

		if (worldToLocal == null)
			initializeMatrices();

		return new Point3D(apply(worldToLocal, point.getX(), point.getY(), point.getZ(), 1d));
	}

	@Override
	public Point3D localToWorld(Point3D point) {

		if (localToWorld == null)
			initializeMatrices();

		return new Point3D(apply(localToWorld, point.getX(), point.getY(), point.getZ(), 1d));
	}

	@Override
	public Vector3D worldToLocal(Vector3D vector) {

		if (worldToLocal == null)
			initializeMatrices();

		return new Vector3D(apply(worldToLocal, vector.getX(), vector.getY(), vector.getZ(), 1d));
	}

	@Override
	public Vector3D localToWorld(Vector3D vector) {

		if (localToWorld == null)
			initializeMatrices();

		return new Vector3D(apply(localToWorld, vector.getX(), vector.getY(), vector.getZ(), 1d));
	}

	@Override
	public Ray worldToLocal(Ray ray) {

		final Point3D newOrigin = worldToLocal(ray.getOrigin());
		final Vector3D newDirection = worldToLocal(ray.getDirection());
		final double normalizationFactor = 1d / ( newDirection.getMagnitude() );

		return new Ray(newOrigin, newDirection.multiply(normalizationFactor), ray.getT() / normalizationFactor,
				ray.getDepth(), ray.getWindowMinT(), ray.getWindowMaxT());
	}

	@Override
	public Ray localToWorld(Ray ray) {

		final Point3D newOrigin = localToWorld(ray.getOrigin());
		final Vector3D newDirection = localToWorld(ray.getDirection());
		final double newDirectionMagnitude = newDirection.getMagnitude();

		return new Ray(newOrigin, newDirection.multiply(1d / newDirectionMagnitude), ray.getT() * newDirectionMagnitude,
				ray.getDepth(), ray.getWindowMinT(), ray.getWindowMaxT());
	}

	@Override
	public Normal3D worldToLocal(Normal3D normal) {

		if (worldToLocal == null)
			initializeMatrices();

		if (worldToLocal_inverseTranspose == null)
			worldToLocal_inverseTranspose = worldToLocal.inverse().transpose();

		return new Normal3D(apply(worldToLocal_inverseTranspose, normal.getX(), normal.getY(), normal.getZ(), 1d));
	}

	@Override
	public Normal3D localToWorld(Normal3D normal) {

		if (localToWorld == null)
			initializeMatrices();

		if (localToWorld_inverseTranspose == null)
			localToWorld_inverseTranspose = localToWorld.inverse().transpose();

		return new Normal3D(apply(localToWorld_inverseTranspose, normal.getX(), normal.getY(), normal.getZ(), 1d));
	}

	private double[] apply(Matrix matrix, double... coordinates) {

		return matrix.multiply(coordinates);
	}

	@Override
	public Matrix getWorldToLocal() {

		return worldToLocal;
	}

	@Override
	public Matrix getLocalToWorld() {

		return localToWorld;
	}

	public double getSx() {

		return sx;
	}

	protected void setSx(double sx) {

		worldToLocal = null;
		localToWorld = null;
		this.sx = sx;
	}

	public double getSy() {

		return sy;
	}

	protected void setSy(double sy) {

		worldToLocal = null;
		localToWorld = null;
		this.sy = sy;
	}

	public double getSz() {

		return sz;
	}

	protected void setSz(double sz) {

		worldToLocal = null;
		localToWorld = null;
		this.sz = sz;
	}

}
