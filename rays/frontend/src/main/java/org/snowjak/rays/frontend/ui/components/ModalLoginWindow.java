package org.snowjak.rays.frontend.ui.components;

import org.snowjak.rays.frontend.events.AddWindowRequest;
import org.snowjak.rays.frontend.events.Bus;
import org.snowjak.rays.frontend.events.SuccessfulLogin;
import org.snowjak.rays.frontend.events.SuccessfulLogout;
import org.snowjak.rays.frontend.security.SecurityOperationException;
import org.snowjak.rays.frontend.security.SecurityOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import com.google.common.eventbus.Subscribe;
import com.vaadin.server.UserError;
import com.vaadin.ui.LoginForm;
import com.vaadin.ui.LoginForm.LoginEvent;
import com.vaadin.ui.Window;

@Component
public class ModalLoginWindow extends Window {
	
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
		setVisible(true);
		setContent(createNewLoginForm());
		
		Bus.get().register(this);
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
	
	@Subscribe
	public void onSuccessfulLogin(SuccessfulLogin event) {
		
		close();
	}
	
	@Subscribe
	public void onSuccessfulLogout(SuccessfulLogout event) {
		
		setContent(createNewLoginForm());
		setVisible(true);
		Bus.get().post(new AddWindowRequest(this));
	}
	
}
