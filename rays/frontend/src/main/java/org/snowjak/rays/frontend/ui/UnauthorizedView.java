package org.snowjak.rays.frontend.ui;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.vaadin.navigator.View;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SpringView(name = UnauthorizedView.NAME)
public class UnauthorizedView extends VerticalLayout implements View {
	
	public static final String NAME = "unauthorized";
	private static final long serialVersionUID = -1120454192753730076L;
	
	@Autowired
	private MessageSource messages;
	
	@PostConstruct
	public void init() {
		
		setSizeFull();
		
		final var messageLabel = new Label(messages.getMessage("unauthorized.message", null, getLocale()));
		messageLabel.addStyleName(ValoTheme.LABEL_FAILURE);
		messageLabel.addStyleName(ValoTheme.LABEL_LARGE);
		
		addComponent(messageLabel);
		setComponentAlignment(messageLabel, Alignment.TOP_CENTER);
	}
	
}
