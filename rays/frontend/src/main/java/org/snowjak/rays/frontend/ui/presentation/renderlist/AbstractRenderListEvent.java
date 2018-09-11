package org.snowjak.rays.frontend.ui.presentation.renderlist;

import org.snowjak.rays.frontend.ui.presentation.AbstractEvent;

public abstract class AbstractRenderListEvent extends AbstractEvent {
	
	private final RenderListItemBean value;
	
	public AbstractRenderListEvent(RenderListItemBean value) {
		
		super();
		this.value = value;
	}
	
	public RenderListItemBean getRender() {
		
		return value;
	}
	
}