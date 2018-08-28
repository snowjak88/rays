package org.snowjak.rays.frontend.events;

import java.util.UUID;

import org.snowjak.rays.frontend.model.entity.Render;
import org.springframework.context.ApplicationEvent;

public abstract class AbstractRenderUpdateEvent extends ApplicationEvent {
	
	private static final long serialVersionUID = 284836652252967828L;
	
	/**
	 * Construct a new AbstractUpdateEvent.
	 * 
	 * @param renderID
	 *            the {@link UUID} associated with the {@link Render} for which this
	 *            event occurred
	 */
	public AbstractRenderUpdateEvent(UUID renderID) {
		
		super(renderID);
	}

	public UUID getRenderID() {
		return (UUID) getSource();
	}
	
}
