package org.snowjak.rays.frontend.messages.frontend;

import org.snowjak.rays.support.model.entity.Render;

public class ReceivedRenderChildrenUpdate {
	
	private final Render render;
	
	public ReceivedRenderChildrenUpdate(Render render) {
		
		this.render = render;
	}
	
	public Render getRender() {
		
		return render;
	}
	
}
