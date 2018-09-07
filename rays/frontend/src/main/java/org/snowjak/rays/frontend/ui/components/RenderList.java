package org.snowjak.rays.frontend.ui.components;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.frontend.messages.frontend.AddWindowRequest;
import org.snowjak.rays.frontend.messages.frontend.ReceivedRenderCreation;
import org.snowjak.rays.frontend.messages.frontend.ReceivedRenderUpdate;
import org.snowjak.rays.frontend.messages.frontend.RunInUIThread;
import org.snowjak.rays.frontend.model.entity.QRender;
import org.snowjak.rays.frontend.model.entity.Render;
import org.snowjak.rays.frontend.model.repository.RenderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.VerticalLayout;

@SpringComponent
@UIScope
public class RenderList extends VerticalLayout {
	
	private static final long serialVersionUID = 7875490599102330170L;
	private static final Logger LOG = LoggerFactory.getLogger(RenderList.class);
	
	private Map<String, RenderListItem> renderedRenders = new HashMap<>();
	
	@Autowired
	private ModalRenderResultWindow resultWindow;
	
	@Autowired
	private EventBus bus;
	
	@Autowired
	private RenderRepository renderRepository;
	
	public RenderList() {
		
		super();
		setWidth(100, Unit.PERCENTAGE);
		setSpacing(false);
	}
	
	@PostConstruct
	public void init() {
		
		bus.register(this);
		
		renderRepository.findAll(QRender.render.parent.isNull(), Sort.by(Direction.DESC, "created"))
				.forEach(r -> renderRender(r));
	}
	
	public void renderRender(Render render) {
		
		LOG.info("RendersList: Rendering render (UUID={}) ...", render.getUuid());
		
		if (renderedRenders.containsKey(render.getUuid()))
			bus.post(new RunInUIThread(() -> {
				LOG.debug("RendersList: Removing render (UUID={})", render.getUuid());
				final var componentToRemove = renderedRenders.remove(render.getUuid());
				removeComponent(componentToRemove);
			}));
		
		LOG.debug("RendersList: Creating new RenderListItem (UUID={})", render.getUuid());
		final var newItem = new RenderListItem(bus, resultWindow, render);
		
		bus.post(new RunInUIThread(() -> {
			LOG.debug("RendersList: Adding render (UUID={})", render.getUuid());
			renderedRenders.put(render.getUuid(), newItem);
			addComponent(newItem);
		}));
	}
	
	@Subscribe
	public void renderCreated(ReceivedRenderCreation renderCreated) {
		
		LOG.debug("RenderCreated (UUID={})", renderCreated.getRender().getUuid().toString());
		
		if (!renderCreated.getRender().isChild()) {
			
			LOG.trace("RenderCreated (UUID={}): is a root-level Render.");
			renderRender(renderCreated.getRender());
			
		} else {
			
			LOG.trace("RenderCreated (UUID={}): is a child-level Render.");
			renderedRenders.values().forEach(rli -> rli.updateRender(renderCreated.getRender()));
			
		}
	}
	
	@Subscribe
	public void renderUpdated(ReceivedRenderUpdate renderUpdated) {
		
		LOG.debug("RenderUpdated (UUID={})", renderUpdated.getRender().getUuid().toString());
		
		if (renderedRenders.containsKey(renderUpdated.getRender().getUuid()))
			renderedRenders.get(renderUpdated.getRender().getUuid()).updateRender(renderUpdated.getRender());
		else
			
			renderedRenders.values().forEach(rli -> rli.updateRender(renderUpdated.getRender()));
	}
	
	public static class RenderListItem extends VerticalLayout {
		
		private static final long serialVersionUID = 614835789627285890L;
		
		private final ModalRenderResultWindow resultWindow;
		private final EventBus bus;
		private Render render;
		private boolean isOpen = false;
		
		private HorizontalLayout itemLayout = new HorizontalLayout();
		
		@SuppressWarnings("rawtypes")
		private List<RenderListColumn> columns = new LinkedList<>();
		
		private Button openCloseButton = null;
		private Button resultButton = null;
		
		private VerticalLayout childrenContainer = null;
		private List<RenderListItem> children = new LinkedList<>();
		
		public RenderListItem(EventBus bus, ModalRenderResultWindow resultWindow, Render render) {
			
			assert (bus != null);
			assert (resultWindow != null);
			assert (render != null);
			
			setMargin(false);
			setWidth(100, Unit.PERCENTAGE);
			
			this.resultWindow = resultWindow;
			this.bus = bus;
			this.render = render;
			
			bus.post(new RunInUIThread(() -> addComponent(itemLayout)));
			
			updateOpenCloseButton();
			
			addColumn("uuid", Label::new, (l) -> l.setWidth(20, Unit.PERCENTAGE),
					(r1, r2) -> !(r1.getUuid().equals(r2.getUuid())), Render::getUuid, Label::setValue);
			addColumn("created", Label::new, (l) -> l.setWidth(20, Unit.PERCENTAGE),
					(r1, r2) -> !(r1.getCreated().equals(r2.getCreated())),
					(r) -> DateTimeFormatter.ISO_INSTANT.format(r.getCreated()), Label::setValue);
			addColumn("size", Label::new, (l) -> l.setWidth(-1, Unit.PERCENTAGE),
					(r1, r2) -> !(r1.getSize().equals(r2.getSize())), Render::getSize, Label::setValue);
			addColumn("progress", ProgressBar::new, (p) -> p.setWidth(30, Unit.PERCENTAGE),
					(r1, r2) -> r1.getPercentComplete() != r2.getPercentComplete(), Render::getPercentCompleteFloat,
					ProgressBar::setValue);
			
			updateResultButton();
			
			updateChildList();
			
		}
		
		public String getUuid() {
			
			return render.getUuid();
		}
		
		private <C extends Component, V> void addColumn(String tag, Supplier<C> columnInstanceSupplier,
				Consumer<C> formatter, BiPredicate<Render, Render> shouldUpdateTest, Function<Render, V> valueSupplier,
				BiConsumer<C, V> valueUpdater) {
			
			LOG.debug("RenderListItem: adding column \"{}\"", tag);
			final var newColumn = new RenderListColumn<>(tag, columnInstanceSupplier, formatter, shouldUpdateTest,
					valueSupplier, valueUpdater, render);
			columns.add(newColumn);
			bus.post(new RunInUIThread(() -> itemLayout.addComponent(newColumn.component)));
		}
		
		@SuppressWarnings("unchecked")
		public void updateRender(Render render) {
			
			if (this.render.getUuid().equals(render.getUuid())) {
				final var prevRender = this.render;
				this.render = render;
				
				LOG.debug("RenderListItem: Updating render (UUID={})", render.getUuid());
				
				for (var c : columns) {
					if (c.columnUpdateTest.test(prevRender, render)) {
						LOG.debug("RenderListItem: Updating render (UUID={}): \"{}\"", render.getUuid(), c.tag);
						bus.post(new RunInUIThread(() -> c.columnValueUpdater.accept(c.component,
								c.columnValueSupplier.apply(this.render))));
					}
				}
				
				if (prevRender.isParent() != render.isParent())
					updateOpenCloseButton();
				
				if (prevRender.getPngBase64() != render.getPngBase64())
					updateResultButton();
				
				if (render.streamChildren()
						.anyMatch(r1 -> !prevRender.streamChildren().anyMatch(r2 -> r1.getUuid().equals(r2.getUuid())))
						|| prevRender.streamChildren().anyMatch(
								r1 -> !render.streamChildren().anyMatch(r2 -> r1.getUuid().equals(r2.getUuid()))))
					updateChildList();
				
			} else
				children.forEach(rli -> rli.updateRender(render));
		}
		
		private void updateOpenCloseButton() {
			
			if (render.isParent()) {
				
				LOG.debug("RenderListItem: Updating open/close button.");
				
				if (openCloseButton == null) {
					openCloseButton = new Button();
					openCloseButton.addClickListener((ce) -> {
						isOpen = !(isOpen);
						bus.post(new RunInUIThread(() -> childrenContainer.setVisible(isOpen)));
						updateOpenCloseButton();
					});
					bus.post(new RunInUIThread(() -> itemLayout.addComponentAsFirst(openCloseButton)));
				}
				
				openCloseButton.setIcon((isOpen) ? VaadinIcons.ANGLE_DOWN : VaadinIcons.ANGLE_RIGHT);
				
			} else {
				
				if (openCloseButton != null) {
					bus.post(new RunInUIThread(() -> itemLayout.removeComponent(openCloseButton)));
					openCloseButton = null;
				}
				
			}
			
		}
		
		private void updateResultButton() {
			
			LOG.debug("RenderListItem: Updating result button.");
			
			if (resultButton == null) {
				resultButton = new Button(VaadinIcons.COMPILE);
				resultButton.addClickListener((ce) -> {
					bus.post(new RunInUIThread(
							() -> resultWindow.setImage(render.getUuid(), render.inflateResultAsResource())));
					bus.post(new AddWindowRequest(resultWindow));
				});
				bus.post(new RunInUIThread(() -> itemLayout.addComponent(resultButton)));
			}
			
			final var hasResult = (render.getPngBase64() != null && !(render.getPngBase64().trim().isEmpty()));
			resultButton.setEnabled(hasResult);
			
			bus.post(new RunInUIThread(() -> itemLayout.addComponent(resultButton)));
			
		}
		
		private void updateChildList() {
			
			if (childrenContainer == null) {
				childrenContainer = new VerticalLayout();
				childrenContainer.setVisible(isOpen);
				bus.post(new RunInUIThread(() -> addComponent(childrenContainer)));
			}
			
			LOG.debug("RenderListItem: Updating child list.");
			
			for (RenderListItem rli : children) {
				if (!render.getChildren().stream().anyMatch(r -> r.getUuid().equals(rli.getUuid())))
					bus.post(new RunInUIThread(() -> {
						LOG.debug("RenderListItem: Removing child (UUID={})", rli.getUuid());
						children.remove(rli);
						childrenContainer.removeComponent(rli);
					}));
			}
			
			for (Render c : render.getChildren()) {
				if (!children.stream().anyMatch(rli -> rli.getUuid().equals(c.getUuid()))) {
					
					LOG.debug("RenderListItem: Adding child (UUID={})", c.getUuid());
					final var newChildItem = new RenderListItem(bus, resultWindow, c);
					children.add(newChildItem);
					bus.post(new RunInUIThread(() -> childrenContainer.addComponent(newChildItem)));
					
				}
			}
		}
		
		private static class RenderListColumn<C extends Component, V> {
			
			private String tag;
			private final BiPredicate<Render, Render> columnUpdateTest;
			private final Function<Render, V> columnValueSupplier;
			private final BiConsumer<C, V> columnValueUpdater;
			
			private final C component;
			
			public RenderListColumn(String tag, Supplier<C> columnInstanceSupplier, Consumer<C> formatter,
					BiPredicate<Render, Render> columnUpdateTest, Function<Render, V> columnValueSupplier,
					BiConsumer<C, V> columnValueUpdater, Render render) {
				
				this.tag = tag;
				this.columnUpdateTest = columnUpdateTest;
				this.columnValueSupplier = columnValueSupplier;
				this.columnValueUpdater = columnValueUpdater;
				
				component = columnInstanceSupplier.get();
				formatter.accept(component);
				columnValueUpdater.accept(component, columnValueSupplier.apply(render));
			}
			
		}
		
	}
	
}
