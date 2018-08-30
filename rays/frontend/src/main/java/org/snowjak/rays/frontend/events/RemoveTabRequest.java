package org.snowjak.rays.frontend.events;

import com.vaadin.ui.Component;

public class RemoveTabRequest {
	private Component component;

	public RemoveTabRequest(Component component) {
		
		this.component = component;
	}
	
	public Component getComponent() {
		
		return component;
	}
	
}
