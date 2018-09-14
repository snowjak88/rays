package org.snowjak.rays.frontend.messages.backend.commands;

import java.util.UUID;

public class RequestRenderDeletion extends AbstractChainableCommand<UUID, Void> {
	
	public RequestRenderDeletion(UUID uuid) {
		
		super(uuid);
	}
	
	public UUID getUuid() {
		
		return getContext();
	}
	
}
