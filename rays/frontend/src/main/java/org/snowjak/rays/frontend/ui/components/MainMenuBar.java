package org.snowjak.rays.frontend.ui.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.frontend.messages.frontend.AddWindowRequest;
import org.snowjak.rays.frontend.messages.frontend.SuccessfulLogin;
import org.snowjak.rays.frontend.messages.frontend.SuccessfulLogout;
import org.snowjak.rays.frontend.security.SecurityOperations;
import org.snowjak.rays.frontend.ui.FrontEndUI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.i18n.LocaleContextHolder;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.VaadinSessionScope;
import com.vaadin.ui.MenuBar;

@SpringComponent
@VaadinSessionScope
public class MainMenuBar extends MenuBar {
	
	private static final long serialVersionUID = 1635170014857768776L;
	private static final Logger LOG = LoggerFactory.getLogger(MainMenuBar.class);
	
	private final SecurityOperations security;
	private final MessageSource messages;
	private final ModalLoginWindow loginWindow;
	private final EventBus frontendBus;
	
	private final MenuItem logInOutItem;
	
	@Autowired
	@Lazy
	private FrontEndUI ui;
	
	@Autowired
	public MainMenuBar(MessageSource messages, SecurityOperations security, ModalLoginWindow loginWindow,
			@Qualifier("frontendEventBus") EventBus frontendBus) {
		
		super();
		this.messages = messages;
		this.security = security;
		this.loginWindow = loginWindow;
		this.frontendBus = frontendBus;
		
		if (security.isAuthenticated())
			this.logInOutItem = addItem(
					messages.getMessage("mainmenu.button.logout", null, LocaleContextHolder.getLocale()),
					VaadinIcons.SIGN_OUT, (item) -> security.doLogOut());
		else
			this.logInOutItem = addItem(
					messages.getMessage("mainmenu.button.login", null, LocaleContextHolder.getLocale()),
					VaadinIcons.SIGN_IN, (item) -> frontendBus.post(new AddWindowRequest(loginWindow)));
		
		frontendBus.register(this);
	}
	
	@Subscribe
	@AllowConcurrentEvents
	public void onSuccessfulLoginEvent(SuccessfulLogin event) {
		
		ui.access(() -> {
			LOG.trace("Detected authentication-success. Manipulating main-menu items.");
			
			logInOutItem.setText(messages.getMessage("mainmenu.button.logout", null, LocaleContextHolder.getLocale()));
			logInOutItem.setIcon(VaadinIcons.SIGN_OUT);
			logInOutItem.setCommand((item) -> security.doLogOut());
		});
	}
	
	@Subscribe
	@AllowConcurrentEvents
	public void onSuccessfulLogoutEvent(SuccessfulLogout event) {
		
		ui.access(() -> {
			LOG.trace("Detected log-out. Manipulating main-menu items.");
			
			logInOutItem.setText(messages.getMessage("mainmenu.button.login", null, LocaleContextHolder.getLocale()));
			logInOutItem.setIcon(VaadinIcons.SIGN_IN);
			logInOutItem.setCommand((item) -> frontendBus.post(new AddWindowRequest(loginWindow)));
		});
	}
	
}
