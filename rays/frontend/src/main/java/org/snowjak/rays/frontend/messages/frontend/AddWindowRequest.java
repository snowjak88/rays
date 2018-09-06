package org.snowjak.rays.frontend.messages.frontend;

import com.vaadin.ui.Window;

/**
 * A request to add a new {@link Window} to the UI.
 * 
 * @author snowjak88
 *
 */
public class AddWindowRequest {
	
	private Window window;
	
	public AddWindowRequest(Window window) {
		
		this.window = window;
	}
	
	public Window getWindow() {
		
		return window;
	}
	
}
