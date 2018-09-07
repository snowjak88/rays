package org.snowjak.rays.frontend.ui.components;

import javax.annotation.PostConstruct;

import org.snowjak.rays.frontend.messages.frontend.AddWindowRequest;
import org.snowjak.rays.frontend.messages.frontend.RunInUIThread;
import org.snowjak.rays.frontend.messages.frontend.SuccessfulLogin;
import org.snowjak.rays.frontend.messages.frontend.SuccessfulLogout;
import org.snowjak.rays.frontend.security.SecurityOperationException;
import org.snowjak.rays.frontend.security.SecurityOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.server.UserError;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.VaadinSessionScope;
import com.vaadin.ui.LoginForm;
import com.vaadin.ui.LoginForm.LoginEvent;
import com.vaadin.ui.Window;

@SpringComponent
@VaadinSessionScope
public class ModalLoginWindow extends Window {
	
	private static final long serialVersionUID = -4500861536749715325L;
	
	private final MessageSource messages;
	
	@Autowired
	private SecurityOperations security;
	
	@Autowired
	private EventBus bus;
	
	@Autowired
	private ModalLoginWindow(MessageSource messages) {
		
		super(messages.getMessage("security.login.form.title", null, LocaleContextHolder.getLocale()));
		this.messages = messages;
	}
	
	@PostConstruct
	public void init() {
		
		center();
		setModal(true);
		setVisible(true);
		setContent(createNewLoginForm());
		
		bus.register(this);
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
	@AllowConcurrentEvents
	public void onSuccessfulLogin(SuccessfulLogin event) {
		
		bus.post(new RunInUIThread(() -> {
			close();
		}));
	}
	
	@Subscribe
	@AllowConcurrentEvents
	public void onSuccessfulLogout(SuccessfulLogout event) {
		
		bus.post(new RunInUIThread(() -> {
			setContent(createNewLoginForm());
			setVisible(true);
			bus.post(new AddWindowRequest(this));
		}));
	}
	
}
