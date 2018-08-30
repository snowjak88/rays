package org.snowjak.rays.frontend.events;

import com.vaadin.ui.Window;

/**
 * A request to remove a new {@link Window} from the UI.
 * 
 * @author snowjak88
 *
 */
public class RemoveWindowRequest {
	
	private Window window;
	
	public RemoveWindowRequest(Window window) {
		
		this.window = window;
	}
	
	public Window getWindow() {
		
		return window;
	}
	
}
