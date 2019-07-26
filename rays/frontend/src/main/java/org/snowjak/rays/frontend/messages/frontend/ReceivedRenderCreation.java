package org.snowjak.rays.frontend.messages.frontend;

import org.snowjak.rays.support.model.entity.Render;

public class ReceivedRenderCreation {
	
	private final Render render;
	
	public ReceivedRenderCreation(Render render) {
		
		this.render = render;
	}
	
	public Render getRender() {
		
		return render;
	}
	
}
