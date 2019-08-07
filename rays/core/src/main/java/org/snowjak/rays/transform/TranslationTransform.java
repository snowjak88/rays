package org.snowjak.rays.transform;

import org.snowjak.rays.annotations.UIField;
import org.snowjak.rays.annotations.UIType;
import org.snowjak.rays.geometry.Normal3D;
import org.snowjak.rays.geometry.Point3D;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.geometry.Vector3D;
import org.snowjak.rays.geometry.util.Matrix;

/**
 * Represents a translating Transform in 3-space.
 * 
 * @author snowjak88
 */
@UIType(type = "translate", fields = { @UIField(name = "dx", defaultValue = "0", type = Double.class),
		@UIField(name = "dy", defaultValue = "0", type = Double.class),
		@UIField(name = "dz", defaultValue = "0", type = Double.class) })
public class TranslationTransform implements Transform {
	
	private double dx = 0;
	private double dy = 0;
	private double dz = 0;
	
	private transient Matrix worldToLocal = null;
	private transient Matrix localToWorld = null;
	
	/**
	 * Create a new TranslationTransform, with the specified
	 * <strong>world-to-local</strong> translation terms.
	 * 
	 * @param dx
	 * @param dy
	 * @param dz
	 */
	public TranslationTransform(double dx, double dy, double dz) {
		
		this.dx = dx;
		this.dy = dy;
		this.dz = dz;
	}
	
	private void initializeMatrices() {
		
		//@formatter:off
		this.worldToLocal = new Matrix(new double[][] {	{ 1d, 0d, 0d, -dx },
														{ 0d, 1d, 0d, -dy },
														{ 0d, 0d, 1d, -dz },
														{ 0d, 0d, 0d,  1d } });
		this.localToWorld = new Matrix(new double[][] {	{ 1d, 0d, 0d, +dx },
														{ 0d, 1d, 0d, +dy },
														{ 0d, 0d, 1d, +dz },
														{ 0d, 0d, 0d,  1d } });
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
	
	/**
	 * As a rule, vectors are considered to be unaffected by translations.
	 */
	@Override
	public Vector3D worldToLocal(Vector3D vector) {
		
		return vector;
	}
	
	/**
	 * As a rule, vectors are considered to be unaffected by translations.
	 */
	@Override
	public Vector3D localToWorld(Vector3D vector) {
		
		return vector;
	}
	
	@Override
	public Ray worldToLocal(Ray ray) {
		
		return new Ray(worldToLocal(ray.getOrigin()), worldToLocal(ray.getDirection()), ray.getT(), ray.getDepth(),
				ray.getWindowMinT(), ray.getWindowMaxT());
	}
	
	@Override
	public Ray localToWorld(Ray ray) {
		
		return new Ray(localToWorld(ray.getOrigin()), localToWorld(ray.getDirection()), ray.getT(), ray.getDepth(),
				ray.getWindowMinT(), ray.getWindowMaxT());
	}
	
	/**
	 * As a rule, normals are considered to be unaffected by translations.
	 */
	@Override
	public Normal3D worldToLocal(Normal3D normal) {
		
		return normal;
	}
	
	/**
	 * As a rule, normals are considered to be unaffected by translations.
	 */
	@Override
	public Normal3D localToWorld(Normal3D normal) {
		
		return normal;
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
	
	public double getDx() {
		
		return dx;
	}
	
	protected void setDx(double dx) {
		
		worldToLocal = null;
		localToWorld = null;
		this.dx = dx;
	}
	
	public double getDy() {
		
		return dy;
	}
	
	protected void setDy(double dy) {
		
		worldToLocal = null;
		localToWorld = null;
		this.dy = dy;
	}
	
	public double getDz() {
		
		return dz;
	}
	
	protected void setDz(double dz) {
		
		worldToLocal = null;
		localToWorld = null;
		this.dz = dz;
	}
	
}
