package org.snowjak.rays.camera;

import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.sample.TracedSample;

public abstract class Camera {
	
	private final double width, height;
	
	public Camera(double width, double height) {
		
		this.width = width;
		this.height = height;
		
	}
	
	public abstract TracedSample trace(Sample sample);
	
	public double getWidth() {
		
		return width;
	}
	
	public double getHeight() {
		
		return height;
	}
	
}
