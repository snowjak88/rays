package org.snowjak.rays.frontend.ui.components;

import org.springframework.stereotype.Component;

import com.vaadin.ui.Window;

@Component
public class ResultDisplayModalWindow extends Window {
	
	private static final long serialVersionUID = -8256995412224542957L;
	
	public ResultDisplayModalWindow() {
		
		center();
		setModal(true);
		setVisible(false);
	}
	
}
