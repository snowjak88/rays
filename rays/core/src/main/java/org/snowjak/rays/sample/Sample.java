package org.snowjak.rays.sample;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.snowjak.rays.Settings;
import org.snowjak.rays.geometry.Point2D;
import org.snowjak.rays.geometry.Ray;
import org.snowjak.rays.spectrum.Spectrum;

public class Sample implements Serializable {
	
	private static final long serialVersionUID = -7458382323573318480L;
	
	private long renderId;
	
	private int samplesPerPixel;
	
	private Point2D filmPoint;
	private Point2D lensUV = new Point2D(0.5, 0.5);
	private Ray cameraRay = null;
	private Spectrum radiance = null;
	
	private List<Double> additional1DSamples = new LinkedList<>();
	private List<Point2D> additional2DSamples = new LinkedList<>();
	
	private transient List<Double> shuffledAdditional1DSamples = null;
	private transient List<Point2D> shuffledAdditional2DSamples = null;
	private transient Iterator<Double> next1DSample = null;
	private transient Iterator<Point2D> next2DSample = null;
	
	public Sample(long renderId, int samplesPerPixel, Point2D filmPoint, Point2D lensUV, Ray cameraRay,
			Spectrum radiance, List<Double> additional1DSamples, List<Point2D> additional2DSamples) {
		
		this.renderId = renderId;
		this.samplesPerPixel = samplesPerPixel;
		this.filmPoint = filmPoint;
		this.lensUV = lensUV;
		this.cameraRay = cameraRay;
		this.radiance = radiance;
		this.additional1DSamples = additional1DSamples;
		this.additional2DSamples = additional2DSamples;
	}
	
	public Sample() {
		
	}
	
	public long getRenderId() {
		
		return renderId;
	}
	
	public void setRenderId(long renderId) {
		
		this.renderId = renderId;
	}
	
	public int getSamplesPerPixel() {
		
		return samplesPerPixel;
	}
	
	public void setSamplesPerPixel(int samplesPerPixel) {
		
		this.samplesPerPixel = samplesPerPixel;
	}
	
	public Point2D getFilmPoint() {
		
		return filmPoint;
	}
	
	public void setFilmPoint(Point2D filmPoint) {
		
		this.filmPoint = filmPoint;
	}
	
	public Point2D getLensUV() {
		
		return lensUV;
	}
	
	public void setLensUV(Point2D lensUV) {
		
		this.lensUV = lensUV;
	}
	
	public Ray getCameraRay() {
		
		return cameraRay;
	}
	
	public void setCameraRay(Ray cameraRay) {
		
		this.cameraRay = cameraRay;
	}
	
	public Spectrum getRadiance() {
		
		return radiance;
	}
	
	public void setRadiance(Spectrum radiance) {
		
		this.radiance = radiance;
	}
	
	public List<Double> getAdditional1DSamples() {
		
		return additional1DSamples;
	}
	
	public void setAdditional1DSamples(List<Double> additional1dSamples) {
		
		additional1DSamples = additional1dSamples;
	}
	
	public List<Point2D> getAdditional2DSamples() {
		
		return additional2DSamples;
	}
	
	public void setAdditional2DSamples(List<Point2D> additional2dSamples) {
		
		additional2DSamples = additional2dSamples;
	}
	
	/**
	 * Get another 1-dimensional sample from this Sample's list
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
	public Double getAdditional1DSample() {
		
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
	 * Get another 2-dimensional sample from this Sample's list
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
	
}
