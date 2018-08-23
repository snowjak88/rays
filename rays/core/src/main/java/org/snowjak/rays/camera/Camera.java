package org.snowjak.rays.camera;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.sample.TracedSample;
import org.snowjak.rays.transform.Transform;
import org.snowjak.rays.transform.Transformable;

/**
 * Models a camera, translating {@link Sample}s into {@link TracedSample}s
 * (i.e., computing the {@link Ray}s resulting from Samples).
 * 
 * @author snowjak88
 *
 */
public abstract class Camera implements Transformable {
	
	private double pixelWidth, pixelHeight;
	private double worldWidth, worldHeight;
	private transient Function<Double, Double> xConversion, yConversion;
	private LinkedList<Transform> worldToLocal;
	private transient LinkedList<Transform> localToWorld = null;
	
	public Camera(double pixelWidth, double pixelHeight, double worldWidth, double worldHeight) {
		
		this(pixelWidth, pixelHeight, worldWidth, worldHeight, Collections.emptyList());
	}
	
	public Camera(double pixelWidth, double pixelHeight, double worldWidth, double worldHeight,
			Transform... worldToLocal) {
		
		this(pixelWidth, pixelHeight, worldWidth, worldHeight, Arrays.asList(worldToLocal));
	}
	
	public Camera(double pixelWidth, double pixelHeight, double worldWidth, double worldHeight,
			Collection<Transform> worldToLocal) {
		
		this.pixelWidth = pixelWidth;
		this.pixelHeight = pixelHeight;
		this.worldWidth = worldWidth;
		this.worldHeight = worldHeight;
		
		this.worldToLocal = new LinkedList<>();
		worldToLocal.stream().forEach(t -> this.appendTransform(t));
	}
	
	/**
	 * Given a {@link Sample}, compute the resulting {@link Ray} (expressed in
	 * world-coordinates) emanating from the given point on this camera's film and
	 * return it, packaged in a {@link TracedSample}.
	 * 
	 * @param sample
	 * @return
	 */
	public abstract TracedSample trace(Sample sample);
	
	@Override
	public List<Transform> getWorldToLocalTransforms() {
		
		return (List<Transform>) worldToLocal;
	}
	
	@Override
	public List<Transform> getLocalToWorldTransforms() {
		
		if (localToWorld == null) {
			localToWorld = new LinkedList<>(worldToLocal);
			Collections.reverse(localToWorld);
		}
		
		return (List<Transform>) localToWorld;
	}
	
	@Override
	public void appendTransform(Transform transform) {
		
		worldToLocal.addLast(transform);
	}
	
	public double getPixelWidth() {
		
		return pixelWidth;
	}
	
	public double getPixelHeight() {
		
		return pixelHeight;
	}
	
	public double getWorldWidth() {
		
		return worldWidth;
	}
	
	public double getWorldHeight() {
		
		return worldHeight;
	}
	
	/**
	 * @return a {@link Function} mapping pixel- to world-X-coordinates
	 */
	public Function<Double, Double> getXConverter() {
		
		if (this.xConversion == null)
			this.xConversion = (pixelX) -> (pixelX - pixelWidth / 2d) * (worldWidth / pixelWidth);
		
		return xConversion;
	}
	
	/**
	 * @return a {@link Function} mapping pixel- to world-Y-coordinates
	 */
	public Function<Double, Double> getYConverter() {
		
		if (this.yConversion == null)
			this.yConversion = (pixelY) -> -(pixelY - pixelHeight / 2d) * (worldHeight / pixelHeight);
		
		return yConversion;
	}
}
