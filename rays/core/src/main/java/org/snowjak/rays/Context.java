package org.snowjak.rays;

import java.io.Serializable;

import org.snowjak.rays.film.Film;
import org.snowjak.rays.sampler.Sampler;

/**
 * Represents the context in which to interpret a {@link Sample}. Assists
 * runtime components in handling a Sample appropriately.
 * 
 * @author snowjak88
 *
 */
public class Context implements Serializable {
	
	private static final long serialVersionUID = -4351658029715470393L;
	
	private Sampler sampler;
	private Film film;
	
	/**
	 * @return the current render's configured {@link Sampler}
	 */
	public Sampler getSampler() {
		
		return sampler;
	}
	
	/**
	 * Register a {@link Sampler} in this Context. Replaces any
	 * previously-registered Sampler.
	 * 
	 * @param sampler
	 */
	public void setSampler(Sampler sampler) {
		
		this.sampler = sampler;
	}
	
	/**
	 * @return the current render's configured {@link Film}
	 */
	public Film getFilm() {
		
		return film;
	}
	
	/**
	 * Register a {@link Film} in this Context. Replaces any previously-registered
	 * Film.
	 * 
	 * @param film
	 */
	public void setFilm(Film film) {
		
		this.film = film;
	}
}