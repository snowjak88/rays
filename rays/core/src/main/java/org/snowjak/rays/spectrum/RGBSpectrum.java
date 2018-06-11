package org.snowjak.rays.spectrum;

import static org.apache.commons.math3.util.FastMath.max;
import static org.apache.commons.math3.util.FastMath.min;

/**
 * Represents a {@link Spectrum} using a simple RGBColorspace trio.
 * 
 * @author snowjak88
 */
public class RGBSpectrum implements Spectrum {
	
	private static final long serialVersionUID = -4926041992553421158L;
	
	/**
	 * Represents a 0-energy Spectrum.
	 */
	public static final RGBSpectrum BLACK = new RGBSpectrum(RGB.BLACK);
	/**
	 * Represents a 1.0-energy Spectrum. (i.e., equivalent to {@link RGBColorspace#WHITE})
	 */
	public static final RGBSpectrum WHITE = new RGBSpectrum(RGB.WHITE);
	
	private RGB rgb;
	private double amplitude = -1d;
	
	/**
	 * Construct a new {@link RGBSpectrum} instance encapsulating {@link RGBColorspace#BLACK}.
	 */
	public RGBSpectrum() {
		
		this(RGB.BLACK);
	}
	
	public RGBSpectrum(RGB rgb) {
		
		this.rgb = rgb;
	}
	
	protected void setRGB(RGB rgb) {
		
		this.rgb = rgb;
		this.amplitude = -1d;
	}
	
	public RGB getRGB() {
		
		return this.rgb;
	}
	
	@Override
	public boolean isBlack() {
		
		return (this.rgb == RGB.BLACK)
				|| (this.rgb.getRed() <= 0d && this.rgb.getGreen() <= 0d && this.rgb.getBlue() <= 0d);
	}
	
	@Override
	public Spectrum add(Spectrum addend) {
		
		return new RGBSpectrum(rgb.add(addend.toRGB()));
	}
	
	@Override
	public Spectrum multiply(Spectrum multiplicand) {
		
		return new RGBSpectrum(rgb.multiply(multiplicand.toRGB()));
	}
	
	@Override
	public Spectrum multiply(double scalar) {
		
		return new RGBSpectrum(rgb.multiply(scalar));
	}
	
	@Override
	public double getAmplitude() {
		
		if (amplitude < 0d) {
			//
			// Compute amplitude of this RGBColorspace trio by partially converting it to
			// an HSL trio -- calculating "L", at least.
			//
			final double minComponent = min(min(rgb.getRed(), rgb.getGreen()), rgb.getBlue());
			final double maxComponent = max(max(rgb.getRed(), rgb.getGreen()), rgb.getBlue());
			
			amplitude = (maxComponent + minComponent) / 2d;
		}
		
		return amplitude;
	}
	
	@Override
	public RGB toRGB() {
		
		return rgb;
	}
	
	@Override
	public int hashCode() {
		
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rgb == null) ? 0 : rgb.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RGBSpectrum other = (RGBSpectrum) obj;
		if (rgb == null) {
			if (other.rgb != null)
				return false;
		} else if (!rgb.equals(other.rgb))
			return false;
		return true;
	}
	
}
