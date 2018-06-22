package org.snowjak.rays.sample;

import java.io.Serializable;

import org.snowjak.rays.geometry.Ray;

/**
 * Represents the result of associating a {@link Ray} with a {@link Sample}
 * 
 * @author snowjak88
 *
 */
public class TracedSample implements Serializable {
	
	private static final long serialVersionUID = 3606575263045043894L;
	
	private final Sample sample;
	private final Ray ray;
	
	public TracedSample(Sample sample, Ray ray) {
		
		this.sample = sample;
		this.ray = ray;
	}
	
	/**
	 * @return the {@link Sample} associated with this trace
	 */
	public Sample getSample() {
		
		return sample;
	}
	
	/**
	 * @return the {@link Ray} associated with this trace
	 */
	public Ray getRay() {
		
		return ray;
	}
	
}