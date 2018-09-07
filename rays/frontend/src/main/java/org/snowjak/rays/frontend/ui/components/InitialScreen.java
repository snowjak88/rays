package org.snowjak.rays.frontend.ui.components;

import javax.annotation.PostConstruct;

import org.snowjak.rays.frontend.messages.frontend.AddTabRequest;
import org.snowjak.rays.frontend.messages.frontend.RemoveTabRequest;
import org.snowjak.rays.frontend.messages.frontend.SuccessfulLogin;
import org.snowjak.rays.frontend.messages.frontend.SuccessfulLogout;
import org.snowjak.rays.frontend.security.SecurityOperations;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.VaadinSessionScope;
import com.vaadin.ui.Label;

@SpringComponent
@VaadinSessionScope
public class InitialScreen extends Label {
	
	private static final long serialVersionUID = -511447530099670304L;
	
	@Autowired
	private EventBus bus;
	@Autowired
	private SecurityOperations security;
	
	@Autowired
	public InitialScreen() {
		
		super("Initial screen here.");
		setCaption("Initial");
	}
	
	@PostConstruct
	public void init() {
		
		bus.register(this);
		
		if (!security.isAuthenticated())
			this.setVisible(false);
	}
	
	@Subscribe
	public void receiveLoginEvent(SuccessfulLogin event) {
		
		bus.post(new AddTabRequest(this));
	}
	
	@Subscribe
	public void receiveLogoutEvent(SuccessfulLogout event) {
		
		bus.post(new RemoveTabRequest(this));
	}
}
