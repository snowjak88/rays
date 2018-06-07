package org.snowjak.rays.spectrum.distribution;

/**
 * Represents a distribution of some quantity.
 * 
 * @author snowjak88
 *
 */
public interface Distribution<V> {
	
	/**
	 * Get this distribution's value for some key, or <code>null</code> if that
	 * value does not exist.
	 * 
	 * @param k
	 * @return
	 */
	public V get(Double key);
	
	/**
	 * Get the key corresponding to this distribution's low-end (or
	 * <code>null</code> if not applicable).
	 * 
	 * @return
	 */
	public Double getLowKey();
	
	/**
	 * Get the key corresponding to this distribution's high-end (or
	 * <code>null</code> if not applicable).
	 * 
	 * @return
	 */
	public Double getHighKey();
	
}
