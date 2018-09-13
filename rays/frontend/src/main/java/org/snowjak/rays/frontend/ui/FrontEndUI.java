package org.snowjak.rays.frontend.ui;

import javax.annotation.PostConstruct;

import org.snowjak.rays.frontend.messages.frontend.AddWindowRequest;
import org.snowjak.rays.frontend.messages.frontend.RemoveWindowRequest;
import org.snowjak.rays.frontend.messages.frontend.RunInUIThread;
import org.snowjak.rays.frontend.security.AuthorizedView;
import org.snowjak.rays.frontend.ui.components.MainMenuBar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.annotations.Push;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.SpringViewDisplay;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SpringUI(path = "")
@Push(transport = Transport.LONG_POLLING)
@Lazy
public class FrontEndUI extends UI {
	
	private static final long serialVersionUID = -8315077204786735072L;
	
	private final VerticalLayout rootLayout = new VerticalLayout();
	
	@Autowired
	private EventBus bus;
	
	@Autowired
	@Lazy
	private MainMenuBar menuBar;
	
	@Autowired
	@Lazy
	private ViewContainer viewContainer;
	
	@Autowired
	@Lazy
	private Navigator navigator;
	
	@SpringView(name = HomeView.NAME)
	@Lazy
	@AuthorizedView("ROLE_.+")
	public static class HomeView extends VerticalLayout implements View {
		
		public static final String NAME = "";
		private static final long serialVersionUID = 7499986011808184784L;
		
		@PostConstruct
		public void init() {
			
			addComponent(new Label("Home page. Think of something to be added here."));
		}
	}
	
	@PostConstruct
	public void postConstruct() {
		
		rootLayout.addComponents(menuBar, viewContainer);
	}
	
	@Override
	protected void init(VaadinRequest request) {
		
		setContent(rootLayout);
	}
	
	@Override
	public void attach() {
		
		bus.register(this);
		super.attach();
	}
	
	@Override
	public void detach() {
		
		bus.unregister(this);
		super.detach();
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
	public void receiveUIThreadRequest(RunInUIThread request) {
		
		access(request.getTask());
	}
	
	@SpringViewDisplay
	public static class ViewContainer extends Panel {
		
		private static final long serialVersionUID = 2795652841550371207L;
		
	}
}
