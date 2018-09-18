package org.snowjak.rays.frontend.messages.frontend;

public class ReceivedRenderDeletion {
	
	private final String id;
	
	public ReceivedRenderDeletion(String id) {
		
		this.id = id;
	}
	
	public String getId() {
		
		return id;
	}
	
}
