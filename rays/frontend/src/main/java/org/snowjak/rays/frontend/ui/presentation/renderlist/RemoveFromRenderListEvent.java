package org.snowjak.rays.frontend.ui.presentation.renderlist;

public class RemoveFromRenderListEvent extends AbstractRenderListEvent {
	
	private final String id;
	public RemoveFromRenderListEvent(String id) {
		
		super();
		this.id =id;
	}
	
	public String getId() {
		
		return id;
	}
	
}