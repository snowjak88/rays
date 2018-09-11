package org.snowjak.rays.frontend.ui.presentation;

@FunctionalInterface
public interface AbstractListener<E extends AbstractEvent> {
	
	public void listen(E event);
}