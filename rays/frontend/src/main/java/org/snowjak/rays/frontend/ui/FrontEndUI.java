package org.snowjak.rays.frontend.ui;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Base64;

import org.snowjak.rays.frontend.model.entity.Render;
import org.snowjak.rays.frontend.model.repository.RenderRepository;
import org.snowjak.rays.frontend.ui.components.MainMenuBar;
import org.snowjak.rays.frontend.ui.components.ModalLoginWindow;
import org.snowjak.rays.frontend.ui.components.RendersGrid;
import org.snowjak.rays.frontend.ui.components.ResultDisplayModalWindow;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Streams;
import com.vaadin.data.ValueProvider;
import com.vaadin.data.provider.GridSortOrder;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.Button;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@SpringUI(path = "")
public class FrontEndUI extends UI {
	
	private static final long serialVersionUID = -8315077204786735072L;
	
	@Autowired
	private ModalLoginWindow loginWindow;
	
	@Autowired
	private ResultDisplayModalWindow resultWindow;
	
	@Autowired
	private MainMenuBar menuBar;
	
	@Autowired
	private RenderRepository renderRepository;
	
	@Override
	protected void init(VaadinRequest request) {
		
		var grid = new RendersGrid();
		grid.setItems(Streams.stream(renderRepository.findAll()));
		
		grid.addColumn(Render::getUuid).setCaption("UUID");
		grid.addComponentColumn((r) -> {
			final var button = new Button();
			button.setCaption("" + r.getScene().getId());
			return button;
		}).setCaption("Scene ID");
		grid.addColumn((r) -> r.getSampler().getClass().getSimpleName()).setCaption("Sampler");
		grid.addColumn((r) -> r.getRenderer().getClass().getSimpleName()).setCaption("Renderer");
		grid.addColumn(
				(r) -> Integer.toString(r.getFilm().getWidth()) + "x" + Integer.toString(r.getFilm().getHeight()))
				.setCaption("Size");
		grid.addColumn((r) -> Integer.toString(r.getSampler().getSamplesPerPixel())).setCaption("spp");
		
		final var gridCreatedColumn = grid.addColumn(Render::getCreated).setCaption("Created");
		
		grid.addColumn(Render::getCompleted).setCaption("Completed");
		
		grid.addComponentColumn((r) -> new ProgressBar(((float) r.getPercentComplete()) / 100.f))
				.setCaption("Complete %");
		
		grid.addComponentColumn((r) -> {
			final var button = new Button("...");
			button.setEnabled(r.getResult() != null);
			button.addClickListener((e) -> {
				final var img = new Image("Result for " + r.getUuid().toString(), new StreamResource(
						() -> new ByteArrayInputStream(Base64.getDecoder().decode(r.getResult().getPngBase64())),
						r.getUuid().toString() + ".png"));
				resultWindow.setContent(img);
				resultWindow.setVisible(true);
			});
			return button;
		}).setCaption("Result");
		
		grid.setSortOrder(Arrays.asList(new GridSortOrder<Render>(gridCreatedColumn, SortDirection.DESCENDING)));
		
		setContent(new VerticalLayout(menuBar, new Label("Some content!"), grid));
		addWindow(loginWindow);
		addWindow(resultWindow);
		
	}
	
}
