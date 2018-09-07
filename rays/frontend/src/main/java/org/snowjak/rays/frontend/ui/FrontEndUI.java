package org.snowjak.rays.frontend.ui;

import org.snowjak.rays.RenderTask;
import org.snowjak.rays.frontend.messages.frontend.AddTabRequest;
import org.snowjak.rays.frontend.messages.frontend.AddWindowRequest;
import org.snowjak.rays.frontend.messages.frontend.RemoveTabRequest;
import org.snowjak.rays.frontend.messages.frontend.RemoveWindowRequest;
import org.snowjak.rays.frontend.messages.frontend.RunInUIThread;
import org.snowjak.rays.frontend.ui.components.InitialScreen;
import org.snowjak.rays.frontend.ui.components.MainMenuBar;
import org.snowjak.rays.frontend.ui.components.ObjectCreator;
import org.snowjak.rays.frontend.ui.components.RenderList;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.annotations.Push;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SpringUI(path = "")
@Push(transport = Transport.LONG_POLLING)
@UIScope
public class FrontEndUI extends UI {
	
	private static final long serialVersionUID = -8315077204786735072L;
	
	@Autowired
	private EventBus bus;
	
	@Autowired
	private MainMenuBar menuBar;
	
	@Autowired
	private InitialScreen initialScreen;
	
	@Autowired
	private ObjectCreator creator;
	
	@Autowired
	private RenderList renderGrid;
	
	private TabSheet tabs;
	
	private AbstractLayout rootLayout;
	
	@Override
	protected void init(VaadinRequest request) {
		
		bus.register(this);
		
		tabs = new TabSheet();
		
		creator.setCaption("Creator");
		tabs.addComponent(creator);
		
		renderGrid.setCaption("List");
		tabs.addComponent(renderGrid);
		
		rootLayout = new VerticalLayout(menuBar, tabs);
		
		creator.setClass(RenderTask.class);
		
		setContent(rootLayout);
		
	}
	
	@Subscribe
	public void receiveWindowRequest(AddWindowRequest request) {
		
		access(() -> {
			request.getWindow().close();
			addWindow(request.getWindow());
		});
		
	}
	
	@Subscribe
	public void receiveWindowRequest(RemoveWindowRequest request) {
		
		access(() -> {
			removeWindow(request.getWindow());
		});
	}
	
	@Subscribe
	public void receiveTabAddRequest(AddTabRequest request) {
		
		access(() -> {
			tabs.addComponent(request.getComponent());
		});
	}
	
	@Subscribe
	public void receiveTabRemoveRequest(RemoveTabRequest request) {
		
		access(() -> {
			tabs.removeComponent(request.getComponent());
		});
	}
	
	@Subscribe
	public void receiveUIThreadRequest(RunInUIThread request) {
		
		access(request.getTask());
	}
	
}
