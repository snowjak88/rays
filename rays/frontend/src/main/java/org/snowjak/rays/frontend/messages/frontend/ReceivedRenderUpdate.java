package org.snowjak.rays.frontend.messages.frontend;

import org.snowjak.rays.support.model.entity.Render;

public class ReceivedRenderUpdate {
	
	private final Render render;
	
	public ReceivedRenderUpdate(Render render) {
		
		this.render = render;
	}
	
	public Render getRender() {
		
		return render;
	}
	
}
