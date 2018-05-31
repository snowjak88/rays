package org.snowjak.rays.frontend.ui;

import org.snowjak.rays.frontend.ui.components.MainMenuBar;
import org.snowjak.rays.frontend.ui.components.ModalLoginWindow;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SpringUI(path = "")
public class FrontEndUI extends UI {
	
	private static final long serialVersionUID = -8315077204786735072L;
	
	@Autowired
	private ModalLoginWindow loginWindow;
	
	@Autowired
	private MainMenuBar menuBar;
	
	@Override
	protected void init(VaadinRequest request) {
		
		setContent(new VerticalLayout(menuBar, new Label("Some content!")));
		addWindow(loginWindow);
		
	}
	
}
