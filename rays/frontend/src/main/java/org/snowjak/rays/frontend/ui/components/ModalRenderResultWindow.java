package org.snowjak.rays.frontend.ui.components;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;

import com.vaadin.server.Resource;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.spring.annotation.VaadinSessionScope;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;

@SpringComponent
@VaadinSessionScope
public class ModalRenderResultWindow extends Window {
	
	private static final long serialVersionUID = 5190866332552898618L;
	
	private String uuid = null;
	private Resource resultImage = null;
	
	private final Label emptyContent = new Label("(no render selected)");
	
	public ModalRenderResultWindow() {
		
		super();
		
		center();
		setModal(true);
		setVisible(true);
		setClosable(true);
		setContent(emptyContent);
	}
	
	public void setImage(String uuid, Resource resultImage) {
		
		this.uuid = uuid;
		this.resultImage = resultImage;
		
		if (this.resultImage != null)
			setContent(new Image("Render " + this.uuid, this.resultImage));
		else
			setContent(emptyContent);
		
		setWidth(getContent().getWidth(), getContent().getWidthUnits());
		setHeight(getContent().getHeight(), getContent().getHeightUnits());
	}
}
