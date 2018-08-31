package org.snowjak.rays.frontend.messages;

import java.util.UUID;

public class RenderCreated {
	
	private final UUID uuid;
	
	public RenderCreated(UUID uuid) {
		
		this.uuid = uuid;
	}
	
	public UUID getUuid() {
		
		return uuid;
	}
	
}
