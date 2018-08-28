package org.snowjak.rays.frontend.ui.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.frontend.security.AfterSuccessfulLoginEvent;
import org.snowjak.rays.frontend.security.AuthenticationLogoutEvent;
import org.snowjak.rays.frontend.security.SecurityOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.stereotype.Component;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.MenuBar;

@Component
public class MainMenuBar extends MenuBar implements ApplicationListener<AbstractAuthenticationEvent> {
	
	private static final long serialVersionUID = 1635170014857768776L;
	private static final Logger LOG = LoggerFactory.getLogger(MainMenuBar.class);
	
	private final SecurityOperations security;
	private final MessageSource messages;
	
	private final MenuItem logInOutItem;
	
	private final MenuItem newRenderItem;
	
	private ModalLoginWindow loginWindow;
	
	@Autowired
	public MainMenuBar(MessageSource messages, SecurityOperations security, ModalLoginWindow loginWindow,
			ModalRenderCreateWindow renderCreationWindow) {
		
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
					VaadinIcons.SIGN_IN, (item) -> loginWindow.setVisible(true));
		
		this.newRenderItem = addItem(
				messages.getMessage("mainmenu.button.create-render", null, LocaleContextHolder.getLocale()),
				VaadinIcons.PLUS_CIRCLE, (item) -> renderCreationWindow.setVisible(true));
		this.newRenderItem.setEnabled(security.hasAuthority("ROLE_CREATE_RENDER"));
	}
	
	@Override
	public void onApplicationEvent(AbstractAuthenticationEvent event) {
		
		if (event instanceof AfterSuccessfulLoginEvent) {
			LOG.trace("Detected authentication-success. Manipulating main-menu items.");
			
			logInOutItem.setText(messages.getMessage("mainmenu.button.logout", null, LocaleContextHolder.getLocale()));
			logInOutItem.setIcon(VaadinIcons.SIGN_OUT);
			logInOutItem.setCommand((item) -> security.doLogOut());
			
			newRenderItem.setEnabled(security.hasAuthority("ROLE_CREATE_RENDER"));
		}
		
		else if (event instanceof AuthenticationLogoutEvent) {
			LOG.trace("Detected log-out. Manipulating main-menu items.");
			
			logInOutItem.setText(messages.getMessage("mainmenu.button.login", null, LocaleContextHolder.getLocale()));
			logInOutItem.setIcon(VaadinIcons.SIGN_IN);
			logInOutItem.setCommand((item) -> loginWindow.setVisible(true));
			
			newRenderItem.setEnabled(false);
		}
	}
	
}
