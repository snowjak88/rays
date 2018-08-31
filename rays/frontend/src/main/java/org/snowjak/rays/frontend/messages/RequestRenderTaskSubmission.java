package org.snowjak.rays.frontend.messages;

import java.util.UUID;

public class RequestRenderTaskSubmission {
	
	private final UUID uuid;
	
	public RequestRenderTaskSubmission(UUID uuid) {
		
		this.uuid = uuid;
	}
	
	public UUID getUuid() {
		
		return uuid;
	}
	
}
