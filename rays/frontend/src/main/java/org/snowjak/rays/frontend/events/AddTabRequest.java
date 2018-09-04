package org.snowjak.rays.frontend.events;

import com.vaadin.ui.Component;

public class AddTabRequest {
	private Component component;

	public AddTabRequest(Component component) {
		
		this.component = component;
	}
	
	public Component getComponent() {
		
		return component;
	}
	
}