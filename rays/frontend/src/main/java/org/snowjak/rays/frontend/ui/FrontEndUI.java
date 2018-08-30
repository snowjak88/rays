package org.snowjak.rays.frontend.ui;

import org.snowjak.rays.Primitive;
import org.snowjak.rays.frontend.events.AddTabRequest;
import org.snowjak.rays.frontend.events.AddWindowRequest;
import org.snowjak.rays.frontend.events.Bus;
import org.snowjak.rays.frontend.events.RemoveTabRequest;
import org.snowjak.rays.frontend.events.RemoveWindowRequest;
import org.snowjak.rays.frontend.ui.components.InitialScreen;
import org.snowjak.rays.frontend.ui.components.MainMenuBar;
import org.snowjak.rays.frontend.ui.components.ObjectCreator;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.eventbus.Subscribe;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SpringUI(path = "")
public class FrontEndUI extends UI {
	
	private static final long serialVersionUID = -8315077204786735072L;
	
	@Autowired
	private MainMenuBar menuBar;
	
	@Autowired
	private InitialScreen initialScreen;
	
	@Autowired
	private ObjectCreator creator;
	
	private TabSheet tabs;
	
	private AbstractLayout rootLayout;
	
	@Override
	protected void init(VaadinRequest request) {
		
		Bus.get().register(this);
		
		tabs = new TabSheet();
		
		final var first = new Label("Tab 1");
		first.setCaption("First");
		tabs.addComponent(first);
		
		final var second = new Label("Tab 2");
		second.setCaption("Second");
		tabs.addComponent(second);
		
		rootLayout = new VerticalLayout(menuBar, tabs, creator);
		
		creator.setClass(Primitive.class);
		
		setContent(rootLayout);
		
	}
	
	@Subscribe
	public void receiveWindowRequest(AddWindowRequest request) {
		
		synchronized (this) {
			if (!getWindows().contains(request.getWindow()))
				addWindow(request.getWindow());
		}
	}
	
	@Subscribe
	public void receiveWindowRequest(RemoveWindowRequest request) {
		
		synchronized (this) {
			if (getWindows().contains(request.getWindow()))
				removeWindow(request.getWindow());
		}
	}
	
	@Subscribe
	public void receiveTabAddRequest(AddTabRequest request) {
		
		synchronized (this) {
			tabs.addComponent(request.getComponent());
		}
	}
	
	@Subscribe
	public void receiveTabRemoveRequest(RemoveTabRequest request) {
		
		synchronized (this) {
			tabs.removeComponent(request.getComponent());
		}
	}
	
}
