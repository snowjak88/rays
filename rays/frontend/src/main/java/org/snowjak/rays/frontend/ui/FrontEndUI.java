package org.snowjak.rays.frontend.ui;

import org.snowjak.rays.frontend.security.AuthenticationLogoutEvent;
import org.snowjak.rays.frontend.security.SecurityOperationException;
import org.snowjak.rays.frontend.security.SecurityOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.context.SecurityContextHolder;

import com.vaadin.server.UserError;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Label;
import com.vaadin.ui.LoginForm;
import com.vaadin.ui.LoginForm.LoginEvent;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SpringUI(path = "")
public class FrontEndUI extends UI implements ApplicationListener<AbstractAuthenticationEvent> {
	
	private static final long serialVersionUID = -8315077204786735072L;
	
	@Autowired
	private MessageSource messages;
	
	@Autowired
	private SecurityOperations security;
	
	/*
	 * 
	 */
	private final Window loginWindow = new Window("Log In");
	
	@Override
	protected void init(VaadinRequest request) {
		
		final boolean isNotAuthenticated = (SecurityContextHolder.getContext().getAuthentication() == null)
				|| (!SecurityContextHolder.getContext().getAuthentication().isAuthenticated())
				|| (SecurityContextHolder.getContext().getAuthentication() instanceof AnonymousAuthenticationToken);
		
		loginWindow.setContent(createNewLoginForm());
		loginWindow.center();
		loginWindow.setModal(true);
		loginWindow.setVisible(isNotAuthenticated);
		
		addWindow(loginWindow);
		
		setContent(new VerticalLayout(new MenuBar().addItem("Log Out", (item) -> security.doLogOut()).getMenuBar(),
				new Label("Some content!")));
		
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
			loginWindow.setVisible(false);
		}
		
		else if (event instanceof AuthenticationLogoutEvent) {
			loginWindow.setContent(createNewLoginForm());
			loginWindow.setVisible(true);
		}
	}
	
}
