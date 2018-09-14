package org.snowjak.rays.frontend.ui.components;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.frontend.messages.frontend.AddWindowRequest;
import org.snowjak.rays.frontend.messages.frontend.RunInUIThread;
import org.snowjak.rays.frontend.security.AuthorizedView;
import org.snowjak.rays.frontend.ui.presentation.renderlist.AddRenderEventListener;
import org.snowjak.rays.frontend.ui.presentation.renderlist.AddToRenderListEvent;
import org.snowjak.rays.frontend.ui.presentation.renderlist.RemoveFromRenderListEvent;
import org.snowjak.rays.frontend.ui.presentation.renderlist.RemoveRenderEventListener;
import org.snowjak.rays.frontend.ui.presentation.renderlist.RenderListItemBean;
import org.snowjak.rays.frontend.ui.presentation.renderlist.RenderListPresentation;
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
	private ModalRenderResultWindow resultWindow;
	
	@Autowired
	private EventBus bus;
	
	@Autowired
	private MessageSource messages;
	
	@Autowired
	private RenderListPresentation presentation;
	
	private final Map<String, Item> items = new HashMap<>();
	
	private final AddRenderEventListener addRenderListener = (e) -> {
		LOG.trace("addRenderListener (UUID={})", e.getRender().getId());
		this.addRender(e.getRender());
	};
	private final UpdateRenderEventListener updateRenderListener = (e) -> {
		LOG.trace("updateRenderListener (UUID={})", e.getRender().getId());
		this.updateRender(e.getRender());
	};
	private final RemoveRenderEventListener removeRenderListener = (e) -> {
		LOG.trace("removeRenderListener (UUID={})", e.getRender().getId());
		this.removeRender(e.getRender());
	};
	
	public RenderList() {
		
		super();
		setWidth(100, Unit.PERCENTAGE);
		setSpacing(false);
	}
	
	@PostConstruct
	public void init() {
		
		presentation.addListener(AddToRenderListEvent.class, addRenderListener);
		presentation.addListener(UpdateRenderListEvent.class, updateRenderListener);
		presentation.addListener(RemoveFromRenderListEvent.class, removeRenderListener);
		
		presentation.getRenders().forEach(r -> addRender(r));
	}
	
	@PreDestroy
	public void onDestroy() {
		
		presentation.removeListener(removeRenderListener);
		presentation.removeListener(updateRenderListener);
		presentation.removeListener(addRenderListener);
	}
	
	public void addRender(RenderListItemBean bean) {
		
		LOG.trace("addRender(UUID={}) ...", bean.getId());
		
		if (bean.getParent() == null) {
			
			LOG.trace("addRender(UUID={}): is top-level ...", bean.getId());
			
			if (items.containsKey(bean.getId())) {
				LOG.trace("addRender(UUID={}): is top-level: already exists, removing first ...", bean.getId());
				removeRender(items.get(bean.getId()).bean);
			}
			
			final var item = new Item(bus, messages, presentation, resultWindow, bean);
			items.put(bean.getId(), item);
			bus.post(new RunInUIThread(() -> addComponentAsFirst(item)));
			
		} else {
			
			if (!items.containsKey(bean.getTopLevelParent().getId())) {
				LOG.debug("addRender(UUID={}): child of UUID={}: top-level parent is unrecognized!", bean.getId(),
						bean.getTopLevelParent());
				return;
			}
			
			LOG.trace("addRender(UUID={}): child of UUID={}: descending to find immediate parent ...", bean.getId(),
					bean.getTopLevelParent());
			items.get(bean.getTopLevelParent().getId()).addChild(bean);
			
		}
	}
	
	public void updateRender(RenderListItemBean bean) {
		
		LOG.trace("updateRender(UUID={}) ...", bean.getId());
		
		if (!items.containsKey(bean.getTopLevelParent().getId())) {
			LOG.debug("updateRender(UUID={}): given top-level parent is not recognized!", bean.getId());
			return;
		}
		
		items.get(bean.getTopLevelParent().getId()).update(bean);
	}
	
	public void removeRender(RenderListItemBean bean) {
		
		LOG.trace("removeRender(UUID={}) ...", bean.getId());
		
		if (!items.containsKey(bean.getId())) {
			LOG.debug("removeRender(UUID={}): given ID is not recognized!", bean.getId());
			return;
		}
		
		final var c = items.get(bean.getId());
		bus.post(new RunInUIThread(() -> removeComponent(c)));
		items.remove(bean.getId());
	}
	
	public static class Item extends VerticalLayout {
		
		private static final long serialVersionUID = 6491829192231759715L;
		
		private final EventBus bus;
		private final MessageSource messages;
		private final RenderListPresentation presentation;
		private final ModalRenderResultWindow resultWindow;
		private final RenderListItemBean bean;
		private final HorizontalLayout fieldLayout = new HorizontalLayout(), buttonsLayout = new HorizontalLayout(),
				datesLayout = new HorizontalLayout();
		
		private final Label id = new Label(), created = new Label(), submitted = new Label(), completed = new Label();
		private final ProgressBar progress = new ProgressBar();
		private final Button openCloseButton = new Button(), decomposeButton = new Button(),
				submitButton = new Button(), viewResultButton = new Button(), removeButton = new Button();
		
		private final AtomicBoolean resetButtonIcons = new AtomicBoolean(false);
		
		private final Panel childrenPanel = new Panel();
		private final VerticalLayout childrenLayout = new VerticalLayout();
		private final Map<String, RenderList.Item> children = new HashMap<>();
		
		public Item(EventBus bus, MessageSource messages, RenderListPresentation presentation,
				ModalRenderResultWindow resultWindow, RenderListItemBean bean) {
			
			this.bus = bus;
			this.messages = messages;
			this.presentation = presentation;
			this.resultWindow = resultWindow;
			this.bean = new RenderListItemBean(bean);
			
			setSpacing(false);
			
			openCloseButton.setIcon(bean.isOpen() ? VaadinIcons.ANGLE_DOWN : VaadinIcons.ANGLE_RIGHT);
			openCloseButton.addClickListener((ce) -> presentation.doOpenClose(bean.getId()));
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
			decomposeButton.addClickListener((ce) -> {
				bus.post(new RunInUIThread(() -> {
					decomposeButton.setEnabled(false);
					submitButton.setEnabled(false);
					removeButton.setEnabled(false);
					decomposeButton.setIcon(VaadinIcons.ELLIPSIS_DOTS_H);
				}));
				resetButtonIcons.set(true);
				presentation.doDecomposeRender(bean.getId());
			});
			decomposeButton.setDescription(messages.getMessage("render.list.buttons.decompose", null, getLocale()));
			
			submitButton.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
			submitButton.setStyleName(ValoTheme.BUTTON_SMALL);
			submitButton.setIcon(VaadinIcons.COMPILE);
			submitButton.addClickListener((ce) -> {
				bus.post(new RunInUIThread(() -> {
					decomposeButton.setEnabled(false);
					submitButton.setEnabled(false);
					removeButton.setEnabled(false);
					submitButton.setIcon(VaadinIcons.ELLIPSIS_DOTS_H);
				}));
				resetButtonIcons.set(true);
				presentation.doSubmitRender(bean.getId());
			});
			submitButton.setDescription(messages.getMessage("render.list.buttons.submit", null, getLocale()));
			
			viewResultButton.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
			viewResultButton.setStyleName(ValoTheme.BUTTON_SMALL);
			viewResultButton.setIcon(VaadinIcons.GRID);
			viewResultButton.addClickListener((ce) -> {
				resultWindow.setImage(bean.getId(), bean.getImage());
				bus.post(new AddWindowRequest(resultWindow));
			});
			viewResultButton.setDescription(messages.getMessage("render.list.buttons.viewresult", null, getLocale()));
			
			removeButton.setStyleName(ValoTheme.BUTTON_ICON_ONLY);
			removeButton.setStyleName(ValoTheme.BUTTON_SMALL);
			removeButton.setIcon(VaadinIcons.CLOSE);
			removeButton.addClickListener((ce) -> presentation.doDelete(bean.getId()));
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
			
			bean.getChildren().stream()
					.forEach(r -> children.put(r.getId(), new Item(bus, messages, presentation, resultWindow, r)));
			
			childrenPanel.setContent(childrenLayout);
			childrenPanel.setCaption(messages.getMessage("render.list.childlist", null, getLocale()));
			childrenPanel.setStyleName(ValoTheme.PANEL_SCROLL_INDICATOR);
			childrenPanel.setStyleName(ValoTheme.PANEL_WELL);
			childrenPanel.setVisible(bean.isOpen());
			
			childrenLayout.setMargin(false);
			childrenLayout.setSpacing(false);
			
			addComponent(fieldLayout);
			addComponent(datesLayout);
			addComponent(childrenPanel);
			
			update(bean, true, true);
		}
		
		public void addChild(RenderListItemBean toAdd) {
			
			if (!toAdd.getParent().getId().equals(bean.getId())) {
				LOG.trace(
						"addChild(UUID={}) to UUID={} -- parent-IDs do not match. Descending to find a match on parent-ID ...",
						toAdd.getId(), bean.getId());
				children.forEach((id, i) -> i.addChild(toAdd));
				return;
			}
			
			LOG.trace("addChild(UUID={}) to UUID={} ...", toAdd.getId(), bean.getId());
			
			if (bean.getChildren().stream().anyMatch(r -> r.getId().equals(toAdd.getId()))) {
				LOG.trace("addChild(UUID={}) to UUID={}: child already exists. Removing first ...", toAdd.getId(),
						bean.getId());
				removeChild(toAdd);
			}
			
			bean.getChildren().add(toAdd);
			children.put(toAdd.getId(), new Item(bus, messages, presentation, resultWindow, toAdd));
			bus.post(new RunInUIThread(() -> childrenLayout.addComponentAsFirst(children.get(toAdd.getId()))));
		}
		
		public void removeChild(RenderListItemBean toRemove) {
			
			if (!toRemove.getChildren().stream().anyMatch(r -> r.getId().equals(toRemove.getId()))) {
				LOG.trace(
						"removeChild(UUID={}) from UUID={} -- no children match given ID. Descending to find a match ...",
						toRemove.getId(), bean.getId());
				children.forEach((id, i) -> i.removeChild(toRemove));
				return;
			}
			
			LOG.trace("removeChild(UUID={}) from UUID={} ...", toRemove.getId(), bean.getId());
			
			toRemove.getChildren().removeIf(r -> r.getId().equals(toRemove.getId()));
			bus.post(new RunInUIThread(() -> childrenLayout.removeComponent(children.get(toRemove.getId()))));
			children.remove(toRemove.getId());
			
		}
		
		public void update(RenderListItemBean newBean) {
			
			update(newBean, false, false);
		}
		
		private void update(RenderListItemBean updatedBean, boolean forceUpdate, boolean runUpdatesInCurrentThread) {
			
			if (!updatedBean.getId().equals(bean.getId())) {
				LOG.trace("update(UUID={}) -- doesn't match this bean (UUID={}). Descending to find a match ...",
						updatedBean.getId(), bean.getId());
				children.forEach((id, i) -> i.update(updatedBean, forceUpdate, runUpdatesInCurrentThread));
				return;
			}
			
			LOG.trace("update(UUID={}) ...", updatedBean.getId());
			
			final Collection<Runnable> uiUpdates = new LinkedList<>();
			
			if (forceUpdate || bean.isOpen() != updatedBean.isOpen()) {
				LOG.trace("update(UUID={}): updating isOpen ...", updatedBean.getId());
				bean.setOpen(updatedBean.isOpen());
				uiUpdates.add(() -> {
					openCloseButton.setIcon(bean.isOpen() ? VaadinIcons.ANGLE_DOWN : VaadinIcons.ANGLE_RIGHT);
					childrenPanel.setVisible(bean.isOpen());
				});
			}
			
			if (forceUpdate || bean.isOpenable() != updatedBean.isOpenable()) {
				LOG.trace("update(UUID={}): updating isOpenable ...", updatedBean.getId());
				bean.setOpenable(updatedBean.isOpenable());
				uiUpdates.add(() -> {
					openCloseButton.setEnabled(bean.isOpenable());
					openCloseButton.setVisible(bean.isOpenable());
				});
			}
			
			if (forceUpdate || !(bean.getId().equals(updatedBean.getId()))) {
				LOG.trace("update(UUID={}): updating id ...", updatedBean.getId());
				bean.setId(updatedBean.getId());
				uiUpdates.add(() -> id.setValue(bean.getId()));
			}
			
			if (forceUpdate || !(bean.getCreated().equals(updatedBean.getCreated()))) {
				LOG.trace("update(UUID={}): updating created ...", updatedBean.getId());
				bean.setCreated(updatedBean.getCreated());
				uiUpdates.add(() -> created.setValue(bean.getCreated()));
			}
			
			if (forceUpdate || !(bean.getSubmitted().equals(updatedBean.getSubmitted()))) {
				LOG.trace("update(UUID={}): updating submitted ...", updatedBean.getId());
				bean.setSubmitted(updatedBean.getSubmitted());
				uiUpdates.add(() -> submitted.setValue(bean.getSubmitted()));
			}
			
			if (forceUpdate || !(bean.getCompleted().equals(updatedBean.getCompleted()))) {
				LOG.trace("update(UUID={}): updating completed ...", updatedBean.getId());
				bean.setCompleted(updatedBean.getCompleted());
				uiUpdates.add(() -> completed.setValue(bean.getCompleted()));
			}
			
			if (forceUpdate || bean.getPercentComplete() != updatedBean.getPercentComplete()) {
				LOG.trace("update(UUID={}): updating percentComplete ...", updatedBean.getId());
				bean.setPercentComplete(updatedBean.getPercentComplete());
				uiUpdates.add(() -> progress.setValue(bean.getPercentComplete()));
			}
			
			if (forceUpdate || resetButtonIcons.get()) {
				LOG.trace("update(UUID={}): updating decompose button icon ...\", updatedBean.getId()");
				uiUpdates.add(() -> decomposeButton.setIcon(VaadinIcons.GRID_SMALL));
				
				LOG.trace("update(UUID={}): updating submit button icon ...\", updatedBean.getId()");
				uiUpdates.add(() -> submitButton.setIcon(VaadinIcons.COMPILE));
			}
			
			if (forceUpdate || bean.isDecomposable() != updatedBean.isDecomposable() || resetButtonIcons.get()) {
				LOG.trace("update(UUID={}): updating isDecomposable ...", updatedBean.getId());
				bean.setDecomposable(updatedBean.isDecomposable());
				uiUpdates.add(() -> decomposeButton.setEnabled(bean.isDecomposable()));
			}
			
			if (forceUpdate || bean.isSubmittable() != updatedBean.isSubmittable() || resetButtonIcons.get()) {
				LOG.trace("update(UUID={}): updating isSubmittable ...", updatedBean.getId());
				bean.setSubmittable(updatedBean.isSubmittable());
				uiUpdates.add(() -> submitButton.setEnabled(bean.isSubmittable()));
			}
			
			if (forceUpdate || bean.isRemovable() != updatedBean.isRemovable() || resetButtonIcons.get()) {
				LOG.trace("update(UUID={}): updating isRemovable ...", updatedBean.getId());
				bean.setRemovable(updatedBean.isRemovable());
				uiUpdates.add(() -> {
					removeButton.setEnabled(bean.isRemovable());
					removeButton.setVisible(bean.isRemovable());
				});
			}
			
			if (forceUpdate || bean.isHasResult() != updatedBean.isHasResult()) {
				LOG.trace("update(UUID={}): updating isHasResult ...", updatedBean.getId());
				bean.setHasResult(updatedBean.isHasResult());
				uiUpdates.add(() -> viewResultButton.setEnabled(updatedBean.isHasResult()));
			}
			
			if (forceUpdate || bean.getImage() != updatedBean.getImage()) {
				LOG.trace("update(UUID={}): updating image ...", updatedBean.getId());
				bean.setImage(updatedBean.getImage());
			}
			
			resetButtonIcons.set(false);
			
			LOG.trace("update(UUID={}): executing updates ...", updatedBean.getId());
			if (runUpdatesInCurrentThread)
				uiUpdates.forEach(r -> r.run());
			else
				bus.post(new RunInUIThread(() -> uiUpdates.forEach(r -> r.run())));
		}
		
		/**
		 * Return the {@link Item} (whether this or a child-instance) corresponding to
		 * the given render-ID, or <code>null</code> if no such Item could be
		 * identified.
		 * 
		 * @param renderId
		 * @return
		 */
		public Item getItemFor(String renderId) {
			
			if (bean.getId().equals(renderId))
				return this;
			
			if (children.containsKey(renderId))
				return children.get(renderId);
			
			return children.values().stream().map(c -> c.getItemFor(renderId)).filter(i -> i != null).findFirst()
					.orElse(null);
		}
	}
	
}
