package org.snowjak.rays.film;

import static org.apache.commons.math3.util.FastMath.max;
import static org.apache.commons.math3.util.FastMath.min;

import java.awt.image.BufferedImage;

import org.snowjak.rays.filter.Filter;
import org.snowjak.rays.sample.EstimatedSample;
import org.snowjak.rays.sample.FixedSample;
import org.snowjak.rays.sampler.Sampler;
import org.snowjak.rays.spectrum.Spectrum;
import org.snowjak.rays.spectrum.distribution.SpectralPowerDistribution;

/**
 * A film object is responsible for accepting a series of
 * {@link EstimatedSample}s and converting them into an image.
 * <p>
 * <strong>Note</strong> that Film is <strong>thread-safe</strong>. This means
 * that multiple threads can all utilize the same Film instance without issue
 * (apart from waiting for any pertinent synchronization locks to be freed).
 * </p>
 * 
 * @author snowjak88
 *
 */
public class Film {
	
	private final Spectrum[][] receivedSpectra;
	private final int[][] receivedSpectraCounts;
	private final int width, height;
	private final Filter filter;
	
	public Film(int width, int height, Filter filter) {
		
		this.width = width;
		this.height = height;
		this.filter = filter;
		
		this.receivedSpectra = new Spectrum[width][height];
		this.receivedSpectraCounts = new int[width][height];
		
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++) {
				receivedSpectra[x][y] = new SpectralPowerDistribution();
				receivedSpectraCounts[x][y] = 0;
			}
	}
	
	/**
	 * Receive the given {@link EstimatedSample}.
	 * <p>
	 * It is assumed that the flow-of-execution has already reported back this
	 * EstimatedSample to the appropriate Sampler (see
	 * {@link Sampler#reportSampleResult(FixedSample)}), and that this
	 * EstimatedSample is judged to be acceptable.
	 * </p>
	 * 
	 * @param estimate
	 */
	public void addSample(EstimatedSample estimate) {
		
		final int filmX = (int) estimate.getSample().getFilmPoint().getX(),
				filmY = (int) estimate.getSample().getFilmPoint().getY();
		
		for (int pixelX = max(0, filmX - filter.getExtentX()); pixelX <= min(height - 1,
				filmX + filter.getExtentX()); pixelX++)
			for (int pixelY = max(0, filmY - filter.getExtentY()); pixelY <= min(width - 1,
					filmY + filter.getExtentY()); pixelY++)
				if (filter.isContributing(estimate.getSample(), pixelX, pixelY)) {
					
					final Spectrum sampleRadiance = estimate.getRadiance()
							.multiply(filter.getContribution(estimate.getSample(), pixelX, pixelY));
					
					synchronized (this) {
						
						if (receivedSpectra[pixelX][pixelY] == null) {
							receivedSpectra[pixelX][pixelY] = sampleRadiance;
							receivedSpectraCounts[pixelX][pixelY] = 1;
						} else {
							receivedSpectra[pixelX][pixelY] = receivedSpectra[pixelX][pixelY].add(sampleRadiance);
							receivedSpectraCounts[pixelX][pixelY]++;
						}
						
					}
					
				}
	}
	
	/**
	 * Compile the image gathered so far by this Film instance.
	 * 
	 * @return
	 */
	public BufferedImage getImage() {
		
		final var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		synchronized (this) {
			
			for (int x = 0; x < width; x++)
				for (int y = 0; y < height; y++) {
					final var rgb = receivedSpectra[x][y].multiply(1d / (double) receivedSpectraCounts[x][y]).toRGB();
					image.setRGB(x, y, rgb.toPacked());
				}
			
		}
		
		return image;
		
	}
	
	public int getWidth() {
		
		return width;
	}
	
	public int getHeight() {
		
		return height;
	}
	
}
