package org.snowjak.rays.frontend.ui.components;

import org.snowjak.rays.frontend.events.AddTabRequest;
import org.snowjak.rays.frontend.events.Bus;
import org.snowjak.rays.frontend.events.RemoveTabRequest;
import org.snowjak.rays.frontend.events.SuccessfulLogin;
import org.snowjak.rays.frontend.events.SuccessfulLogout;
import org.snowjak.rays.frontend.security.SecurityOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.Subscribe;
import com.vaadin.ui.Label;

@Component
public class InitialScreen extends Label {
	
	private static final long serialVersionUID = -511447530099670304L;
	
	@Autowired
	public InitialScreen(SecurityOperations security) {
		
		super("Initial screen here.");
		setCaption("Initial");
		
		Bus.get().register(this);
		
		if (!security.isAuthenticated())
			this.setVisible(false);
	}
	
	@Subscribe
	public void receiveLoginEvent(SuccessfulLogin event) {
		
		Bus.get().post(new AddTabRequest(this));
	}
	
	@Subscribe
	public void receiveLogoutEvent(SuccessfulLogout event) {
		
		Bus.get().post(new RemoveTabRequest(this));
	}
}
