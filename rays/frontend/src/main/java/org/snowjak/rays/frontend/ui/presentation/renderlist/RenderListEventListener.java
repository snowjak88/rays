package org.snowjak.rays.frontend.ui.presentation.renderlist;

import org.snowjak.rays.frontend.ui.presentation.AbstractListener;

@FunctionalInterface
public interface RenderListEventListener<E extends AbstractRenderListEvent> extends AbstractListener<E> {
	
}