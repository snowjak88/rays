package org.snowjak.rays.frontend.ui.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.frontend.messages.frontend.AddWindowRequest;
import org.snowjak.rays.frontend.messages.frontend.ReceivedRenderCreation;
import org.snowjak.rays.frontend.messages.frontend.ReceivedRenderUpdate;
import org.snowjak.rays.frontend.model.entity.Render;
import org.snowjak.rays.frontend.ui.FrontEndUI;
import org.snowjak.rays.frontend.ui.dataproviders.RenderDataProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.data.provider.GridSortOrderBuilder;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.VaadinSessionScope;
import com.vaadin.ui.TreeGrid;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.renderers.ProgressBarRenderer;

@SpringComponent
@VaadinSessionScope
public class RenderList extends TreeGrid<Render> {
	
	private static final long serialVersionUID = 7875490599102330170L;
	private static final Logger LOG = LoggerFactory.getLogger(RenderList.class);
	
	final private Column<Render, String> uuidColumn;
	final private Column<Render, String> createdColumn;
	final private Column<Render, Long> sceneIdColumn;
	final private Column<Render, Double> progressColumn;
	final private Column<Render, String> sizeColumn;
	final private Column<Render, String> resultColumn;
	
	@Autowired
	private ModalRenderResultWindow resultWindow;
	
	@Autowired
	@Lazy
	private FrontEndUI ui;
	
	@Autowired
	public RenderList(RenderDataProvider renderDataProvider, @Qualifier("frontendEventBus") EventBus frontendBus) {
		
		super(renderDataProvider);
		setWidth(100, Unit.PERCENTAGE);
		
		frontendBus.register(this);
		
		uuidColumn = addColumn(Render::getUuid).setCaption("UUID").setSortable(true);
		setHierarchyColumn(uuidColumn);
		
		createdColumn = addColumn((r) -> r.getCreated().toString()).setSortable(true).setCaption("Created");
		
		sceneIdColumn = addColumn((r) -> r.getScene().getId()).setCaption("Scene").setSortable(true);
		
		progressColumn = addColumn(Render::getPercentCompleteDouble, new ProgressBarRenderer()).setCaption("Progress")
				.setSortable(true);
		
		sizeColumn = addColumn((r) -> r.getWidth() + "x" + r.getHeight() + "(" + r.getSpp() + "spp)").setCaption("Size")
				.setSortable(true);
		
		resultColumn = addColumn(r -> ((r.getPngBase64() == null) ? null : "View"),
				new ButtonRenderer<Render>((bce) -> {
					final var render = bce.getItem();
					resultWindow.setImage(render.getUuid(), render.inflateResultAsResource());
					frontendBus.post(new AddWindowRequest(resultWindow));
				}, "(none)")).setCaption("Result").setSortable(true);
		
		setSortOrder(new GridSortOrderBuilder<Render>().thenDesc(createdColumn).build());
	}
	
	@Subscribe
	@AllowConcurrentEvents
	public void renderCreated(ReceivedRenderCreation renderCreated) {
		
		LOG.debug("RenderCreated (UUID={})", renderCreated.getRender().getUuid().toString());
		
		refreshRenderInGrid(renderCreated.getRender());
	}
	
	@Subscribe
	@AllowConcurrentEvents
	public void renderUpdated(ReceivedRenderUpdate renderUpdated) {
		
		LOG.debug("RenderUpdated (UUID={})", renderUpdated.getRender().getUuid().toString());
		
		refreshRenderInGrid(renderUpdated.getRender());
	}
	
	private void refreshRenderInGrid(Render render) {
		
		ui.access(() -> {
			LOG.trace("Refreshing item ...");
			getDataProvider().refreshItem(render);
		});
	}
	
}
