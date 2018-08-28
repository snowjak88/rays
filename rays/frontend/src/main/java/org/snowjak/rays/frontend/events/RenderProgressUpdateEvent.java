package org.snowjak.rays.frontend.events;

import java.util.UUID;

public class RenderProgressUpdateEvent extends AbstractRenderUpdateEvent {
	
	private static final long serialVersionUID = -8360543971159769766L;
	
	public RenderProgressUpdateEvent(UUID renderID) {
		
		super(renderID);
	}
	
}
