package org.snowjak.rays.frontend.ui.presentation.renderlist;

public class AddToRenderListEvent extends AbstractRenderListEvent {
	
	private final String id;
	
	public AddToRenderListEvent(String id) {
		
		super();
		this.id = id;
	}
	
	public String getId() {
		
		return id;
	}
	
}