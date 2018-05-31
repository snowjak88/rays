package org.snowjak.rays.frontend.ui.components;

import org.snowjak.rays.frontend.security.AuthenticationLogoutEvent;
import org.snowjak.rays.frontend.security.SecurityOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.MenuBar;

@Component
public class MainMenuBar extends MenuBar implements ApplicationListener<AbstractAuthenticationEvent> {
	
	private static final long serialVersionUID = 1635170014857768776L;
	
	private final SecurityOperations security;
	private final MessageSource messages;
	
	private final MenuItem logInOutItem;
	
	@Autowired
	public MainMenuBar(MessageSource messages, SecurityOperations security) {
		
		super();
		this.messages = messages;
		this.security = security;
		
		if (security.isAuthenticated()) {
			this.logInOutItem = addItem(
					messages.getMessage("mainmenu.button.logout", null, LocaleContextHolder.getLocale()),
					VaadinIcons.SIGN_OUT, (item) -> security.doLogOut());
		} else {
			this.logInOutItem = addItem(
					messages.getMessage("mainmenu.button.login", null, LocaleContextHolder.getLocale()),
					VaadinIcons.SIGN_IN, null);
		}
	}
	
	@Override
	public void onApplicationEvent(AbstractAuthenticationEvent event) {
		
		if (event instanceof AuthenticationSuccessEvent) {
			logInOutItem.setText(messages.getMessage("mainmenu.button.logout", null, LocaleContextHolder.getLocale()));
			logInOutItem.setIcon(VaadinIcons.SIGN_OUT);
			logInOutItem.setCommand((item) -> security.doLogOut());
		}
		
		else if (event instanceof AuthenticationLogoutEvent) {
			logInOutItem.setText(messages.getMessage("mainmenu.button.login", null, LocaleContextHolder.getLocale()));
			logInOutItem.setIcon(VaadinIcons.SIGN_IN);
			logInOutItem.setCommand(null);
		}
	}
	
}
