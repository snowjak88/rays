package org.snowjak.rays.frontend.events;

import com.google.common.eventbus.EventBus;

/**
 * Application-wide holder for an {@link EventBus} instance.
 * 
 * @author snowjak88
 *
 */
public class Bus {
	
	private static EventBus _instance = null;
	
	/**
	 * Get the shared {@link EventBus} instance.
	 * 
	 * @return
	 */
	public static EventBus get() {
		
		if (_instance == null)
			_instance = new EventBus();
		
		return _instance;
	}
	
}
