package org.snowjak.rays.renderer;

import org.snowjak.rays.Scene;
import org.snowjak.rays.film.Film;
import org.snowjak.rays.sample.EstimatedSample;
import org.snowjak.rays.sample.Sample;
import org.snowjak.rays.sample.TracedSample;
import org.snowjak.rays.sampler.Sampler;

/**
 * The Renderer is responsible for taking {@link Sample}s from a
 * {@link Sampler}, computing an estimate of the received energy from the given
 * {@link Scene}, and (if the Sampler approves of the resulting estimate --
 * {@link Sampler#reportSampleResult(EstimatedSample)}), add that estimated
 * sample to a {@link Film} instance for compilation into a final image.
 * <p>
 * Renderer provides two stub-methods which implementations may utilize:
 * <ul>
 * <li>{@link #beforeRender(Sampler, Film, Scene)}</li>
 * <li>{@link #afterRender(Sampler, Film, Scene)}</li>
 * </ul>
 * By default, neither of these stub-methods does anything. However,
 * implementations may wish to initialize themselves with the actual parameters
 * that are about to be passed into {@link #render(Sampler, Film, Scene)}.
 * </p>
 * 
 * @author snowjak88
 *
 */
public abstract class Renderer {
	
	/**
	 * Called immediately prior to actually running this Renderer. Useful for
	 * executing any necessary setup tasks. By default, this is a no-op method.
	 * 
	 * @param sampler
	 * @param film
	 * @param scene
	 */
	public void beforeRender(Sampler sampler, Film film, Scene scene) {
		
		// By default, do nothing.
	}
	
	/**
	 * Given a {@link Sampler}, {@link Film}, and {@link Scene}:
	 * <ol>
	 * <li>Grab all available {@link Sample}s from the Sampler</li>
	 * <li>Call this Renderer's {@link #estimate(TracedSample)} method to compute an
	 * {@link EstimatedSample}</li>
	 * <li>If the Sampler approves of the estimate
	 * ({@link Sampler#reportSampleResult(EstimatedSample)}), add the estimate to
	 * the Film</li>
	 * </ol>
	 * 
	 * <p>
	 * This method is <strong>single-threaded</strong>. This method will
	 * <strong>block</strong> until it finishes.
	 * </p>
	 * 
	 * @param sampler
	 * @param film
	 * @param scene
	 */
	public void render(Sampler sampler, Film film, Scene scene) {
		
		while (sampler.hasNextSample()) {
			final var estimated = this.estimate(scene.getCamera().trace(sampler.getNextSample()), scene);
			
			if (sampler.reportSampleResult(estimated))
				film.addSample(estimated);
		}
		
	}
	
	/**
	 * Called immediately after this Renderer completes its current render-task.
	 * Useful for executing any necessary takedown tasks. By default, this is a
	 * no-op method.
	 * 
	 * @param sampler
	 * @param film
	 * @param scene
	 */
	public void afterRender(Sampler sampler, Film film, Scene scene) {
		
		// By default, do nothing.
	}
	
	/**
	 * Given a {@link TracedSample}, estimate the incident energy resulting from the
	 * given {@link Scene}.
	 * 
	 * @param sample
	 * @return
	 */
	public abstract EstimatedSample estimate(TracedSample sample, Scene scene);
	
}
