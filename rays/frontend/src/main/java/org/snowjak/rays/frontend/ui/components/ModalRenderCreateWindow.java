package org.snowjak.rays.frontend.ui.components;

import org.snowjak.rays.frontend.RenderCreationHandler;
import org.snowjak.rays.frontend.security.SecurityOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Window;

@Component
public class ModalRenderCreateWindow extends Window {
	
	private static final long serialVersionUID = -4500861536749715325L;
	
	private MessageSource messages;
	
	private RenderCreationHandler renderCreation;
	
	@Autowired
	private ModalRenderCreateWindow(MessageSource messages, SecurityOperations security,
			RenderCreationHandler renderCreation) {
		
		super(messages.getMessage("render.create.form.title", null, LocaleContextHolder.getLocale()));
		this.messages = messages;
		this.renderCreation = renderCreation;
		
		center();
		setModal(true);
		setVisible(false);
		setContent(createNewRenderForm());
	}
	
	private FormLayout createNewRenderForm() {
		
		final var form = new FormLayout();
		
		final var samplerText = new TextArea();
		samplerText.setCaption(
				messages.getMessage("render.create.form.samplerJson", null, LocaleContextHolder.getLocale()));
		samplerText.setPlaceholder("... JSON ...");
		samplerText.setHeight(2, Unit.EM);
		form.addComponent(samplerText);
		
		final var rendererText = new TextArea();
		rendererText.setCaption(
				messages.getMessage("render.create.form.rendererJson", null, LocaleContextHolder.getLocale()));
		rendererText.setPlaceholder("... JSON ...");
		rendererText.setHeight(2, Unit.EM);
		form.addComponent(rendererText);
		
		final var filmText = new TextArea();
		filmText.setCaption(messages.getMessage("render.create.form.filmJson", null, LocaleContextHolder.getLocale()));
		filmText.setPlaceholder("... JSON ...");
		filmText.setHeight(2, Unit.EM);
		form.addComponent(filmText);
		
		final var sceneText = new TextArea();
		sceneText
				.setCaption(messages.getMessage("render.create.form.sceneJson", null, LocaleContextHolder.getLocale()));
		sceneText.setPlaceholder("... JSON ...");
		sceneText.setHeight(5, Unit.EM);
		sceneText.setWidth(80, Unit.EM);
		form.addComponent(sceneText);
		
		final var submitButton = new Button();
		submitButton
				.setCaption(messages.getMessage("render.create.form.submit", null, LocaleContextHolder.getLocale()));
		submitButton.addClickListener((e) -> {
			final var reply = renderCreation.createRender(samplerText.getValue(), rendererText.getValue(),
					filmText.getValue(), sceneText.getValue());
			
			if (reply) {
				samplerText.setValue("");
				rendererText.setValue("");
				filmText.setValue("");
				sceneText.setValue("");
				close();
			}
		});
		form.addComponent(submitButton);
		
		return form;
	}
	
}
