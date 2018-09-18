package org.snowjak.rays.frontend.ui.presentation.renderlist;

import java.util.Map;

import org.snowjak.rays.frontend.ui.presentation.renderlist.RenderListPresentation.Field;

public class UpdateRenderListEvent extends AbstractRenderListEvent {
	
	private final String id;
	private final Map<Field, String> updated;
	
	public UpdateRenderListEvent(String id, Map<Field, String> updated) {
		
		super();
		this.id = id;
		this.updated = updated;
	}
	
	public String getId() {
		
		return id;
	}
	
	public Map<Field, String> getUpdated() {
		
		return updated;
	}
	
}