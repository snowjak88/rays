package org.snowjak.rays.frontend.messages.backend.commands;

import java.util.UUID;

public class RequestRenderCreationFromSingleJson extends AbstractChainableCommand<String, UUID> {
	
	public RequestRenderCreationFromSingleJson(String json) {
		
		super(json);
	}
	
	public String getJson() {
		
		return getContext();
	}
}
