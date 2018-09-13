package org.snowjak.rays.frontend.ui.presentation;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 
 * @author snowjak88
 *
 * @param <L>
 *            base-class for all listeners used by this Presentation
 * @param <E>
 *            base-class for all events used by this Presentation
 */
public abstract class AbstractPresentation<L extends AbstractListener<E>, E extends AbstractEvent> {
	
	private final Map<Class<? extends E>, Collection<L>> listeners = new HashMap<>();
	
	/**
	 * Add the given listener to this Presentation, listening for events of the
	 * given type.
	 * 
	 * @param eventType
	 * @param listener
	 */
	public void addListener(Class<? extends E> eventType, L listener) {
		
		synchronized (this) {
			
			if (!listeners.containsKey(eventType))
				listeners.put(eventType, new LinkedList<>());
			
			if (!listeners.get(eventType).contains(listener))
				listeners.get(eventType).add(listener);
			
		}
	}
	
	public void removeListener(L listener) {
		
		synchronized (this) {
			if (listeners.containsKey(listener.getClass()))
				listeners.get(listener.getClass()).remove(listener);
		}
	}
	
	protected void fireEvent(E event) {
		
		synchronized (this) {
			
			final var applicableEventTypes = listeners.keySet().stream()
					.filter(et -> event.getClass().isAssignableFrom(et)).collect(Collectors.toList());
			
			applicableEventTypes.forEach(et -> listeners.get(et).forEach(l -> l.listen(event)));
			
		}
	}
	
}
