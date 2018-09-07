package org.snowjak.rays.frontend.ui.components;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.frontend.messages.frontend.AddWindowRequest;
import org.snowjak.rays.frontend.messages.frontend.RunInUIThread;
import org.snowjak.rays.frontend.messages.frontend.SuccessfulLogin;
import org.snowjak.rays.frontend.messages.frontend.SuccessfulLogout;
import org.snowjak.rays.frontend.security.SecurityOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

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
	
	private MenuItem logInOutItem;
	
	@Autowired
	private SecurityOperations security;
	@Autowired
	private MessageSource messages;
	@Autowired
	private ModalLoginWindow loginWindow;
	@Autowired
	private EventBus bus;
	
	public MainMenuBar() {
		
		super();
	}
	
	@PostConstruct
	public void init() {
		
		if (security.isAuthenticated())
			this.logInOutItem = addItem(
					messages.getMessage("mainmenu.button.logout", null, LocaleContextHolder.getLocale()),
					VaadinIcons.SIGN_OUT, (item) -> security.doLogOut());
		else
			this.logInOutItem = addItem(
					messages.getMessage("mainmenu.button.login", null, LocaleContextHolder.getLocale()),
					VaadinIcons.SIGN_IN, (item) -> bus.post(new AddWindowRequest(loginWindow)));
		
		bus.register(this);
	}
	
	@Subscribe
	public void onSuccessfulLoginEvent(SuccessfulLogin event) {
		
		bus.post(new RunInUIThread(() -> {
			LOG.trace("Detected authentication-success. Manipulating main-menu items.");
			
			logInOutItem.setText(messages.getMessage("mainmenu.button.logout", null, LocaleContextHolder.getLocale()));
			logInOutItem.setIcon(VaadinIcons.SIGN_OUT);
			logInOutItem.setCommand((item) -> security.doLogOut());
		}));
	}
	
	@Subscribe
	public void onSuccessfulLogoutEvent(SuccessfulLogout event) {
		
		bus.post(new RunInUIThread(() -> {
			LOG.trace("Detected log-out. Manipulating main-menu items.");
			
			logInOutItem.setText(messages.getMessage("mainmenu.button.login", null, LocaleContextHolder.getLocale()));
			logInOutItem.setIcon(VaadinIcons.SIGN_IN);
			logInOutItem.setCommand((item) -> bus.post(new AddWindowRequest(loginWindow)));
		}));
	}
	
}
