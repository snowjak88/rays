package org.snowjak.rays.frontend.ui.components;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Function;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.frontend.messages.frontend.RunInUIThread;
import org.snowjak.rays.frontend.security.AuthorizedView;
import org.snowjak.rays.frontend.ui.presentation.renderlist.AddRenderEventListener;
import org.snowjak.rays.frontend.ui.presentation.renderlist.AddToRenderListEvent;
import org.snowjak.rays.frontend.ui.presentation.renderlist.RemoveFromRenderListEvent;
import org.snowjak.rays.frontend.ui.presentation.renderlist.RemoveRenderEventListener;
import org.snowjak.rays.frontend.ui.presentation.renderlist.RenderListEventListener;
import org.snowjak.rays.frontend.ui.presentation.renderlist.RenderListPresentation;
import org.snowjak.rays.frontend.ui.presentation.renderlist.RenderListPresentation.Field;
import org.snowjak.rays.frontend.ui.presentation.renderlist.UpdateRenderEventListener;
import org.snowjak.rays.frontend.ui.presentation.renderlist.UpdateRenderListEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import com.google.common.eventbus.EventBus;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.navigator.View;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

@SpringView(name = RenderList.NAME)
@ViewScope
@AuthorizedView({ "ROLE_VIEW_ALL_RENDERS" })
public class RenderList extends VerticalLayout implements View {
	
	public static final String NAME = "render-list";
	private static final long serialVersionUID = 7875490599102330170L;
	private static final Logger LOG = LoggerFactory.getLogger(RenderList.class);
	
	@Autowired
	private EventBus bus;
	
	@Autowired
	private MessageSource messages;
	
	@Autowired
	private RenderListPresentation presentation;
	
	private final Map<String, Item> items = new HashMap<>();
	
	private final AddRenderEventListener addRenderListener = (e) -> {
		LOG.trace("addRenderListener (UUID={})", e.getId());
		this.addRender(e.getId());
	};
	private final UpdateRenderEventListener updateRenderListener = (e) -> {
		LOG.trace("updateRenderListener (UUID={})", e.getId());
		this.updateRender(e.getId(), e.getUpdated());
	};
	private final RemoveRenderEventListener removeRenderListener = (e) -> {
		LOG.trace("removeRenderListener (UUID={})", e.getId());
		this.removeRender(e.getId());
	};
	
	public RenderList() {
		
		super();
		setWidth(100, Unit.PERCENTAGE);
		setSpacing(false);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@PostConstruct
	public void init() {
		
		presentation.addListener(AddToRenderListEvent.class, (RenderListEventListener) addRenderListener);
		presentation.addListener(UpdateRenderListEvent.class, (RenderListEventListener) updateRenderListener);
		presentation.addListener(RemoveFromRenderListEvent.class, (RenderListEventListener) removeRenderListener);
		
		presentation.getRootIDs().forEach(this::addRender);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@PreDestroy
	public void onDestroy() {
		
		presentation.removeListener((RenderListEventListener) removeRenderListener);
		presentation.removeListener((RenderListEventListener) updateRenderListener);
		presentation.removeListener((RenderListEventListener) addRenderListener);
	}
	
	public void addRender(String id) {
		
		LOG.trace("addRender(UUID={}) ...", id);
		
		if (presentation.getParentID(id) == null) {
			
			LOG.trace("addRender(UUID={}): is top-level ...", id);
			
			if (items.containsKey(id)) {
				LOG.trace("addRender(UUID={}): is top-level: already exists, removing first ...", id);
				removeRender(id);
			}
			
			final var item = new Item(bus, messages, presentation, id);
			items.put(id, item);
			bus.post(new RunInUIThread(() -> addComponentAsFirst(item)));
			
		} else {
			
			LOG.trace("addRender(UUID={}): child of UUID={}: descending to find immediate parent ...", id,
					presentation.getParentID(id));
			items.values().forEach(i -> i.addChild(id));
			
		}
	}
	
	public void updateRender(String id, Map<RenderListPresentation.Field, String> fields) {
		
		LOG.trace("updateRender(UUID={}) ...", id);
		
		items.values().forEach(i -> i.update(id, fields));
	}
	
	public void removeRender(String id) {
		
		LOG.trace("removeRender(UUID={}) ...", id);
		
		if (items.containsKey(id)) {
			
			final var c = items.get(id);
			bus.post(new RunInUIThread(() -> removeComponent(c)));
			items.remove(id);
			
		} else {
			
			items.values().forEach(i -> i.removeChild(id));
			
		}
	}
	
	public static class Item extends VerticalLayout {
		
		private static final long serialVersionUID = 6491829192231759715L;
		
		private final EventBus bus;
		private final MessageSource messages;
		private final RenderListPresentation presentation;
		private final String renderID;
		
		private final HorizontalLayout fieldLayout = new HorizontalLayout(), buttonsLayout = new HorizontalLayout(),
				datesLayout = new HorizontalLayout();
		
		private final Label id = new Label(), created = new Label(), submitted = new Label(), completed = new Label();
		private final ProgressBar progress = new ProgressBar();
		private final Button openCloseButton = new Button(), decomposeButton = new Button(),
				submitButton = new Button(), viewResultButton = new Button(), removeButton = new Button();
		
		private final Panel childrenPanel = new Panel();
		private final VerticalLayout childrenLayout = new VerticalLayout();
		private final Map<String, RenderList.Item> children = new HashMap<>();
		
		private final Map<Field, Function<String, Runnable>> updaters = new HashMap<>();
		{
			updaters.put(Field.ID, (value) -> () -> id.setValue(value));
			updaters.put(Field.CREATED, (value) -> () -> created.setValue(value));
			updaters.put(Field.SUBMITTED, (value) -> () -> submitted.setValue(value));
			updaters.put(Field.COMPLETED, (value) -> () -> completed.setValue(value));
			updaters.put(Field.PROGRESS, (value) -> () -> progress.setValue(Float.parseFloat(value)));
			updaters.put(Field.IS_OPEN, (value) -> () -> {
				final var isOpen = Boolean.parseBoolean(value);
				openCloseButton.setIcon(isOpen ? VaadinIcons.ANGLE_DOWN : VaadinIcons.ANGLE_RIGHT);
				childrenPanel.setVisible(isOpen);
			});
			updaters.put(Field.IS_OPENABLE, (value) -> () -> {
				final var isOpenable = Boolean.parseBoolean(value);
				openCloseButton.setEnabled(isOpenable);
				openCloseButton.setVisible(isOpenable);
			});
			updaters.put(Field.IS_DECOMPOSABLE,
					(value) -> () -> decomposeButton.setEnabled(Boolean.parseBoolean(value)));
			updaters.put(Field.IS_SUBMITTABLE, (value) -> () -> submitButton.setEnabled(Boolean.parseBoolean(value)));
			updaters.put(Field.IS_VIEWABLE, (value) -> () -> viewResultButton.setEnabled(Boolean.parseBoolean(value)));
			updaters.put(Field.IS_REMOVABLE, (value) -> () -> removeButton.setEnabled(Boolean.parseBoolean(value)));
		}
		
		public Item(EventBus bus, MessageSource messages, RenderListPresentation presentation, String renderId) {
			
			this.bus = bus;
			this.messages = messages;
			this.presentation = presentation;
			this.renderID = renderId;
			
			setSpacing(false);
			
			openCloseButton.addClickListener((ce) -> presentation.doOpenClose(renderId));
			openCloseButton.setDescription(messages.getMessage("render.list.buttons.openclose", null, getLocale()));
			openCloseButton.setStyleName(ValoTheme.BUTTON_BORDERLESS);
			openCloseButton.setStyleName(ValoTheme.BUTTON_SMALL);
			openCloseButton.setWidthUndefined();
			
			id.setStyleName(ValoTheme.LABEL_TINY);
			id.setDescription(messages.getMessage("render.list.fields.id", null, getLocale()));
			id.setWidthUndefined();
			
			progress.setWidth(100, Unit.PERCENTAGE);
			
			created.setStyleName(ValoTheme.LABEL_TINY);
			created.setDescription(messages.getMessage("render.list.fields.created", null, getLocale()));
			
			submitted.setStyleName(ValoTheme.LABEL_TINY);
			submitted.setDescription(messages.getMessage("render.list.fields.submitted", null, getLocale()));
			
			completed.setStyleName(ValoTheme.LABEL_TINY);
			completed.setDescription(messages.getMessage("render.list.fields.completed", null, getLocale()));
			
			decomposeButton.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
			decomposeButton.setStyleName(ValoTheme.BUTTON_SMALL);
			decomposeButton.setIcon(VaadinIcons.GRID_SMALL);
			decomposeButton.addClickListener((ce) -> presentation.doDecomposeRender(renderId));
			decomposeButton.setDescription(messages.getMessage("render.list.buttons.decompose", null, getLocale()));
			
			submitButton.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
			submitButton.setStyleName(ValoTheme.BUTTON_SMALL);
			submitButton.setIcon(VaadinIcons.COMPILE);
			submitButton.addClickListener((ce) -> presentation.doSubmitRender(renderId));
			submitButton.setDescription(messages.getMessage("render.list.buttons.submit", null, getLocale()));
			
			viewResultButton.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
			viewResultButton.setStyleName(ValoTheme.BUTTON_SMALL);
			viewResultButton.setIcon(VaadinIcons.GRID);
			viewResultButton.addClickListener((ce) -> presentation.doViewResult(renderId));
			viewResultButton.setDescription(messages.getMessage("render.list.buttons.viewresult", null, getLocale()));
			
			removeButton.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
			removeButton.setStyleName(ValoTheme.BUTTON_SMALL);
			removeButton.setIcon(VaadinIcons.CLOSE);
			removeButton.addClickListener((ce) -> presentation.doDelete(renderId));
			removeButton.setDescription(messages.getMessage("render.list.buttons.remove", null, getLocale()));
			
			buttonsLayout.setWidthUndefined();
			buttonsLayout.setMargin(false);
			buttonsLayout.setSpacing(false);
			buttonsLayout.addComponents(decomposeButton, submitButton, viewResultButton, removeButton);
			buttonsLayout.setComponentAlignment(decomposeButton, Alignment.MIDDLE_CENTER);
			buttonsLayout.setComponentAlignment(submitButton, Alignment.MIDDLE_CENTER);
			buttonsLayout.setComponentAlignment(viewResultButton, Alignment.MIDDLE_CENTER);
			buttonsLayout.setComponentAlignment(removeButton, Alignment.MIDDLE_CENTER);
			
			fieldLayout.setWidth(100, Unit.PERCENTAGE);
			fieldLayout.setMargin(false);
			fieldLayout.setSpacing(true);
			fieldLayout.addComponents(openCloseButton, id, progress, buttonsLayout);
			
			fieldLayout.setComponentAlignment(openCloseButton, Alignment.MIDDLE_CENTER);
			fieldLayout.setComponentAlignment(id, Alignment.MIDDLE_LEFT);
			fieldLayout.setComponentAlignment(progress, Alignment.MIDDLE_CENTER);
			fieldLayout.setComponentAlignment(buttonsLayout, Alignment.MIDDLE_RIGHT);
			
			fieldLayout.setExpandRatio(openCloseButton, 0);
			fieldLayout.setExpandRatio(id, 0);
			fieldLayout.setExpandRatio(progress, 1);
			fieldLayout.setExpandRatio(buttonsLayout, 0);
			
			datesLayout.setMargin(false);
			datesLayout.setSpacing(true);
			datesLayout.addComponents(created, submitted, completed);
			datesLayout.setComponentAlignment(created, Alignment.MIDDLE_LEFT);
			datesLayout.setComponentAlignment(submitted, Alignment.MIDDLE_LEFT);
			datesLayout.setComponentAlignment(completed, Alignment.MIDDLE_LEFT);
			
			presentation.getChildIDs(renderId).forEach(this::addChild);
			
			childrenPanel.setContent(childrenLayout);
			childrenPanel.setCaption(messages.getMessage("render.list.childlist", null, getLocale()));
			childrenPanel.setStyleName(ValoTheme.PANEL_SCROLL_INDICATOR);
			childrenPanel.setStyleName(ValoTheme.PANEL_WELL);
			childrenPanel.setVisible(false);
			
			childrenLayout.setMargin(false);
			childrenLayout.setSpacing(false);
			
			addComponent(fieldLayout);
			addComponent(datesLayout);
			addComponent(childrenPanel);
			
			update(presentation.getFieldsFor(renderId));
		}
		
		public void update(String id, Map<Field, String> fields) {
			
			if (this.renderID.equals(id))
				this.update(fields);
			
			else
				children.forEach((childID, item) -> item.update(id, fields));
		}
		
		private void update(Map<Field, String> fields) {
			
			final var updates = new LinkedList<Runnable>();
			
			for (var f : fields.keySet())
				if (updaters.containsKey(f))
					updates.add(updaters.get(f).apply(fields.get(f)));
				
			if (!updates.isEmpty())
				bus.post(new RunInUIThread(() -> updates.forEach(r -> r.run())));
			
		}
		
		public void addChild(String childID) {
			
			final var parentID = presentation.getParentID(childID);
			
			if (!renderID.equals(parentID)) {
				LOG.trace(
						"addChild(UUID={}) to UUID={} -- parent-IDs do not match. Descending to find a match on parent-ID ...",
						childID, renderID);
				children.forEach((id, i) -> i.addChild(childID));
				return;
			}
			
			LOG.trace("addChild(UUID={}) to UUID={} ...", childID, renderID);
			
			if (children.containsKey(childID)) {
				LOG.trace("addChild(UUID={}) to UUID={}: child already exists. Removing first ...", childID, renderID);
				bus.post(new RunInUIThread(() -> childrenLayout.removeComponent(children.get(childID))));
				children.remove(childID);
			}
			
			final var childItem = new Item(bus, messages, presentation, childID);
			children.put(childID, childItem);
			bus.post(new RunInUIThread(() -> childrenLayout.addComponentAsFirst(childItem)));
		}
		
		public void removeChild(String childID) {
			
			if (!children.containsKey(childID)) {
				LOG.trace(
						"removeChild(UUID={}) from UUID={} -- no children match given ID. Descending to find a match ...",
						childID, renderID);
				children.forEach((id, i) -> i.removeChild(childID));
				return;
			}
			
			LOG.trace("removeChild(UUID={}) from UUID={} ...", childID, renderID);
			
			final var c = children.get(childID);
			if (c != null) {
				children.remove(childID);
				bus.post(new RunInUIThread(() -> {
					childrenLayout.removeComponent(c);
				}));
			}
			
		}
	}
	
}
