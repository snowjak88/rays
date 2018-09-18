package org.snowjak.rays.frontend.ui.presentation.renderlist;

public class UpdateChildrenListRenderListEvent extends AbstractRenderListEvent {
	
	private final String id;
	
	public UpdateChildrenListRenderListEvent(String id) {
		
		super();
		this.id = id;
	}
	
	public String getId() {
		
		return id;
	}
	
}