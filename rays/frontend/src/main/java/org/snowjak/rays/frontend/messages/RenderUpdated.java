package org.snowjak.rays.frontend.messages;

import java.util.UUID;

public class RenderUpdated {
	
	private final UUID uuid;
	
	public RenderUpdated(UUID uuid) {
		
		this.uuid = uuid;
	}
	
	public UUID getUuid() {
		
		return uuid;
	}
}
