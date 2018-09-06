package org.snowjak.rays.frontend.messages.backend.commands;

import java.util.UUID;

public class RequestSingleRenderTaskSubmission extends AbstractChainableCommand<UUID, Void> {
	
	public RequestSingleRenderTaskSubmission(UUID uuid) {
		
		super(uuid);
	}
	
	public UUID getUuid() {
		
		return getContext();
	}
	
}
