package org.snowjak.rays.frontend.ui.components;

import org.snowjak.rays.frontend.security.AuthenticationLogoutEvent;
import org.snowjak.rays.frontend.security.SecurityOperationException;
import org.snowjak.rays.frontend.security.SecurityOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import com.vaadin.server.UserError;
import com.vaadin.ui.LoginForm;
import com.vaadin.ui.LoginForm.LoginEvent;
import com.vaadin.ui.Window;

@Component
public class ModalLoginWindow extends Window implements ApplicationListener<AbstractAuthenticationEvent> {
	
	private static final long serialVersionUID = -4500861536749715325L;
	
	private MessageSource messages;
	
	private SecurityOperations security;
	
	@Autowired
	private ModalLoginWindow(MessageSource messages, SecurityOperations security) {
		
		super(messages.getMessage("security.login.form.title", null, LocaleContextHolder.getLocale()));
		this.messages = messages;
		this.security = security;
		
		center();
		setModal(true);
		setVisible(!security.isAuthenticated());
		setContent(createNewLoginForm());
	}
	
	@Override
	public void close() {
		
		if (security.isAuthenticated())
			super.close();
	}
	
	private LoginForm createNewLoginForm() {
		
		final LoginForm form = new LoginForm();
		form.addLoginListener(this::loginListener);
		return form;
	}
	
	private void loginListener(LoginEvent event) {
		
		final String username = event.getLoginParameter("username");
		final String password = event.getLoginParameter("password");
		
		try {
			security.doLogIn(username, password);
			
		} catch (SecurityOperationException e) {
			
			final StringBuilder errorMessage = new StringBuilder();
			errorMessage.append(messages.getMessage("security.login.error", null, getLocale()));
			errorMessage.append(" ");
			errorMessage
					.append(messages.getMessage("security.login.error." + e.getReason().toString(), null, getLocale()));
			
			event.getSource().setComponentError(new UserError(errorMessage.toString()));
		}
		
	}
	
	@Override
	public void onApplicationEvent(AbstractAuthenticationEvent event) {
		
		if (event instanceof AuthenticationSuccessEvent) {
			setVisible(false);
		}
		
		else if (event instanceof AuthenticationLogoutEvent) {
			setContent(createNewLoginForm());
			setVisible(true);
		}
	}
	
}
