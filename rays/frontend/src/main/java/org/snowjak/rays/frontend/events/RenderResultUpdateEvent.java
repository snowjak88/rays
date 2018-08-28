package org.snowjak.rays.frontend.events;

import java.util.UUID;

public class RenderResultUpdateEvent extends AbstractRenderUpdateEvent {
	
	private static final long serialVersionUID = -1221740012927572771L;
	
	public RenderResultUpdateEvent(UUID renderID) {
		
		super(renderID);
	}
	
}
