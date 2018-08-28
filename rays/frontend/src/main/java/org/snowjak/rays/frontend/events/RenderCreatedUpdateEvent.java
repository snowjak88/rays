package org.snowjak.rays.frontend.events;

import java.util.UUID;

public class RenderCreatedUpdateEvent extends AbstractRenderUpdateEvent {
	
	private static final long serialVersionUID = 5689943836244949780L;
	
	public RenderCreatedUpdateEvent(UUID renderID) {
		
		super(renderID);
	}
	
}
