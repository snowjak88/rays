package org.snowjak.rays.frontend.ui.components;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.frontend.messages.frontend.AddWindowRequest;
import org.snowjak.rays.frontend.messages.frontend.RunInUIThread;
import org.snowjak.rays.frontend.ui.presentation.renderlist.AddRenderEventListener;
import org.snowjak.rays.frontend.ui.presentation.renderlist.AddToRenderListEvent;
import org.snowjak.rays.frontend.ui.presentation.renderlist.RemoveFromRenderListEvent;
import org.snowjak.rays.frontend.ui.presentation.renderlist.RemoveRenderEventListener;
import org.snowjak.rays.frontend.ui.presentation.renderlist.RenderListItemBean;
import org.snowjak.rays.frontend.ui.presentation.renderlist.RenderListPresentation;
import org.snowjak.rays.frontend.ui.presentation.renderlist.UpdateRenderEventListener;
import org.snowjak.rays.frontend.ui.presentation.renderlist.UpdateRenderListEvent;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.eventbus.EventBus;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@SpringComponent
@UIScope
public class RenderList extends VerticalLayout {
	
	private static final long serialVersionUID = 7875490599102330170L;
	private static final Logger LOG = LoggerFactory.getLogger(RenderList.class);
	
	@Autowired
	private ModalRenderResultWindow resultWindow;
	
	@Autowired
	private EventBus bus;
	
	@Autowired
	private RenderListPresentation presentation;
	
	private final Map<String, Item> items = new HashMap<>();
	
	private final AddRenderEventListener addRenderListener = (e) -> {
		LOG.info("addRenderListener (UUID={})", e.getRender().getId());
		this.addRender(e.getRender());
	};
	private final UpdateRenderEventListener updateRenderListener = (e) -> {
		LOG.info("updateRenderListener (UUID={})", e.getRender().getId());
		this.updateRender(e.getRender());
	};
	private final RemoveRenderEventListener removeRenderListener = (e) -> {
		LOG.info("removeRenderListener (UUID={})", e.getRender().getId());
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
		
		synchronized (this) {
			
			LOG.debug("addRender(UUID={}) ...", bean.getId());
			
			if (bean.getParent() == null) {
				
				LOG.debug("addRender(UUID={}): is top-level ...", bean.getId());
				
				if (items.containsKey(bean.getId())) {
					LOG.debug("addRender(UUID={}): is top-level: already exists, removing first ...", bean.getId());
					removeRender(items.get(bean.getId()).bean);
				}
				
				final var item = new Item(bus, presentation, resultWindow, bean);
				items.put(bean.getId(), item);
				bus.post(new RunInUIThread(() -> addComponentAsFirst(item)));
				
			} else {
				
				if (!items.containsKey(bean.getTopLevelParent().getId())) {
					LOG.warn("addRender(UUID={}): child of UUID={}: top-level parent is unrecognized!", bean.getId(),
							bean.getTopLevelParent());
					return;
				}
				
				LOG.debug("addRender(UUID={}): child of UUID={}: descending to find immediate parent ...", bean.getId(),
						bean.getTopLevelParent());
				items.get(bean.getTopLevelParent().getId()).addChild(bean);
				
			}
		}
	}
	
	public void updateRender(RenderListItemBean bean) {
		
		synchronized (this) {
			
			LOG.debug("updateRender(UUID={}) ...", bean.getId());
			
			if (!items.containsKey(bean.getTopLevelParent().getId())) {
				LOG.warn("updateRender(UUID={}): given top-level parent is not recognized!", bean.getId());
				return;
			}
			
			items.get(bean.getTopLevelParent().getId()).update(bean);
			
		}
	}
	
	public void removeRender(RenderListItemBean bean) {
		
		synchronized (this) {
			
			LOG.debug("removeRender(UUID={}) ...", bean.getId());
			
			if (!items.containsKey(bean.getId())) {
				LOG.warn("removeRender(UUID={}): given ID is not recognized!", bean.getId());
				return;
			}
			
			final var c = items.get(bean.getId());
			bus.post(new RunInUIThread(() -> removeComponent(c)));
			items.remove(bean.getId());
			
		}
	}
	
	public static class Item extends VerticalLayout {
		
		private static final long serialVersionUID = 6491829192231759715L;
		
		private final EventBus bus;
		private final RenderListPresentation presentation;
		private final ModalRenderResultWindow resultWindow;
		private final RenderListItemBean bean;
		private final HorizontalLayout fieldLayout = new HorizontalLayout();
		
		private final TextField id = new TextField(), created = new TextField(), submitted = new TextField(),
				completed = new TextField();
		private final ProgressBar progress = new ProgressBar();
		private final Button openCloseButton = new Button(), decomposeButton = new Button(),
				submitButton = new Button(), viewResultButton = new Button();
		
		private final VerticalLayout childrenLayout = new VerticalLayout();
		private final Map<String, RenderList.Item> children = new HashMap<>();
		
		public Item(EventBus bus, RenderListPresentation presentation, ModalRenderResultWindow resultWindow,
				RenderListItemBean bean) {
			
			this.bus = bus;
			this.presentation = presentation;
			this.resultWindow = resultWindow;
			this.bean = new RenderListItemBean(bean);
			
			openCloseButton.setIcon(bean.isOpen() ? VaadinIcons.ANGLE_DOWN : VaadinIcons.ANGLE_RIGHT);
			openCloseButton.addClickListener((ce) -> presentation.doOpenClose(bean.getId()));
			
			decomposeButton.setIcon(VaadinIcons.GRID_SMALL);
			decomposeButton.addClickListener((ce) -> presentation.doDecomposeRender(bean.getId()));
			
			submitButton.setIcon(VaadinIcons.COMPILE);
			submitButton.addClickListener((ce) -> presentation.doSubmitRender(bean.getId()));
			
			viewResultButton.setIcon(VaadinIcons.GRID);
			viewResultButton.addClickListener((ce) -> {
				resultWindow.setImage(bean.getId(), bean.getImage());
				bus.post(new AddWindowRequest(resultWindow));
			});
			
			id.setReadOnly(true);
			created.setReadOnly(true);
			submitted.setReadOnly(true);
			completed.setReadOnly(true);
			
			fieldLayout.setMargin(false);
			fieldLayout.setSpacing(false);
			fieldLayout.addComponents(openCloseButton, id, created, submitted, completed, progress, decomposeButton,
					submitButton, viewResultButton);
			
			fieldLayout.setExpandRatio(openCloseButton, 0);
			fieldLayout.setExpandRatio(created, 1);
			fieldLayout.setExpandRatio(submitted, 1);
			fieldLayout.setExpandRatio(completed, 1);
			fieldLayout.setExpandRatio(progress, 3);
			fieldLayout.setExpandRatio(decomposeButton, 0);
			fieldLayout.setExpandRatio(submitButton, 0);
			fieldLayout.setExpandRatio(viewResultButton, 0);
			
			bean.getChildren().stream()
					.forEach(r -> children.put(r.getId(), new Item(bus, presentation, resultWindow, r)));
			
			childrenLayout.setMargin(false);
			childrenLayout.setSpacing(false);
			childrenLayout.setVisible(bean.isOpen());
			
			addComponent(fieldLayout);
			addComponent(childrenLayout);
			
			update(bean, true, true);
		}
		
		public void addChild(RenderListItemBean toAdd) {
			
			synchronized (this) {
				
				if (!toAdd.getParent().getId().equals(bean.getId())) {
					LOG.debug(
							"addChild(UUID={}) to UUID={} -- parent-IDs do not match. Descending to find a match on parent-ID ...",
							toAdd.getId(), bean.getId());
					children.forEach((id, i) -> i.addChild(toAdd));
					return;
				}
				
				LOG.debug("addChild(UUID={}) to UUID={} ...", toAdd.getId(), bean.getId());
				
				if (bean.getChildren().stream().anyMatch(r -> r.getId().equals(toAdd.getId()))) {
					LOG.debug("addChild(UUID={}) to UUID={}: child already exists. Removing first ...", toAdd.getId(),
							bean.getId());
					removeChild(toAdd);
				}
				
				bean.getChildren().add(toAdd);
				children.put(toAdd.getId(), new Item(bus, presentation, resultWindow, toAdd));
				bus.post(new RunInUIThread(() -> childrenLayout.addComponentAsFirst(children.get(toAdd.getId()))));
			}
		}
		
		public void removeChild(RenderListItemBean toRemove) {
			
			synchronized (this) {
				if (!toRemove.getChildren().stream().anyMatch(r -> r.getId().equals(toRemove.getId()))) {
					LOG.debug(
							"removeChild(UUID={}) from UUID={} -- no children match given ID. Descending to find a match ...",
							toRemove.getId(), bean.getId());
					children.forEach((id, i) -> i.removeChild(toRemove));
					return;
				}
				
				LOG.debug("removeChild(UUID={}) from UUID={} ...", toRemove.getId(), bean.getId());
				
				toRemove.getChildren().removeIf(r -> r.getId().equals(toRemove.getId()));
				bus.post(new RunInUIThread(() -> childrenLayout.removeComponent(children.get(toRemove.getId()))));
				children.remove(toRemove.getId());
				
			}
		}
		
		public void update(RenderListItemBean newBean) {
			
			update(newBean, false, false);
		}
		
		private void update(RenderListItemBean updatedBean, boolean forceUpdate, boolean runUpdatesInCurrentThread) {
			
			if (!updatedBean.getId().equals(bean.getId())) {
				LOG.debug("update(UUID={}) -- doesn't match this bean (UUID={}). Descending to find a match ...",
						updatedBean.getId(), bean.getId());
				children.forEach((id, i) -> i.update(updatedBean, forceUpdate, runUpdatesInCurrentThread));
				return;
			}
			
			LOG.debug("update(UUID={}) ...", updatedBean.getId());
			
			final Collection<Runnable> uiUpdates = new LinkedList<>();
			
			if (forceUpdate || bean.isOpen() != updatedBean.isOpen()) {
				LOG.trace("update(UUID={}): updating isOpen ...", updatedBean.getId());
				bean.setOpen(updatedBean.isOpen());
				uiUpdates.add(() -> {
					openCloseButton.setIcon(bean.isOpen() ? VaadinIcons.ANGLE_DOWN : VaadinIcons.ANGLE_RIGHT);
					childrenLayout.setVisible(bean.isOpen());
				});
			}
			
			if (forceUpdate || bean.isOpenable() != updatedBean.isOpenable()) {
				LOG.trace("update(UUID={}): updating isOpenable ...", updatedBean.getId());
				bean.setOpenable(updatedBean.isOpenable());
				uiUpdates.add(() -> openCloseButton.setEnabled(bean.isOpenable()));
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
			
			if (forceUpdate || bean.isDecomposable() != updatedBean.isDecomposable()) {
				LOG.trace("update(UUID={}): updating isDecomposable ...", updatedBean.getId());
				bean.setDecomposable(updatedBean.isDecomposable());
				uiUpdates.add(() -> decomposeButton.setEnabled(bean.isDecomposable()));
			}
			
			if (forceUpdate || bean.isSubmittable() != updatedBean.isSubmittable()) {
				LOG.trace("update(UUID={}): updating isSubmittable ...", updatedBean.getId());
				bean.setSubmittable(updatedBean.isSubmittable());
				uiUpdates.add(() -> submitButton.setEnabled(bean.isSubmittable()));
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
