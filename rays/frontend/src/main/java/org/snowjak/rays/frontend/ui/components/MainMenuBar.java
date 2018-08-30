package org.snowjak.rays.frontend.ui.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.frontend.events.AddWindowRequest;
import org.snowjak.rays.frontend.events.Bus;
import org.snowjak.rays.frontend.events.SuccessfulLogin;
import org.snowjak.rays.frontend.events.SuccessfulLogout;
import org.snowjak.rays.frontend.security.SecurityOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.Subscribe;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.MenuBar;

@Component
public class MainMenuBar extends MenuBar {
	
	private static final long serialVersionUID = 1635170014857768776L;
	private static final Logger LOG = LoggerFactory.getLogger(MainMenuBar.class);
	
	private final SecurityOperations security;
	private final MessageSource messages;
	
	private final MenuItem logInOutItem;
	
	private ModalLoginWindow loginWindow;
	
	@Autowired
	public MainMenuBar(MessageSource messages, SecurityOperations security, ModalLoginWindow loginWindow) {
		
		super();
		this.messages = messages;
		this.security = security;
		this.loginWindow = loginWindow;
		
		if (security.isAuthenticated())
			this.logInOutItem = addItem(
					messages.getMessage("mainmenu.button.logout", null, LocaleContextHolder.getLocale()),
					VaadinIcons.SIGN_OUT, (item) -> security.doLogOut());
		else
			this.logInOutItem = addItem(
					messages.getMessage("mainmenu.button.login", null, LocaleContextHolder.getLocale()),
					VaadinIcons.SIGN_IN, (item) -> Bus.get().post(new AddWindowRequest(loginWindow)));
		
		Bus.get().register(this);
	}
	
	@Subscribe
	public void onSuccessfulLoginEvent(SuccessfulLogin event) {
		
		LOG.trace("Detected authentication-success. Manipulating main-menu items.");
		
		logInOutItem.setText(messages.getMessage("mainmenu.button.logout", null, LocaleContextHolder.getLocale()));
		logInOutItem.setIcon(VaadinIcons.SIGN_OUT);
		logInOutItem.setCommand((item) -> security.doLogOut());
	}
	
	@Subscribe
	public void onSuccessfulLogoutEvent(SuccessfulLogout event) {
		
		LOG.trace("Detected log-out. Manipulating main-menu items.");
		
		logInOutItem.setText(messages.getMessage("mainmenu.button.login", null, LocaleContextHolder.getLocale()));
		logInOutItem.setIcon(VaadinIcons.SIGN_IN);
		logInOutItem.setCommand((item) -> Bus.get().post(new AddWindowRequest(loginWindow)));
	}
	
}
