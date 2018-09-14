package org.snowjak.rays.frontend.ui.presentation.renderlist;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.frontend.messages.backend.commands.RequestRenderDecomposition;
import org.snowjak.rays.frontend.messages.backend.commands.RequestRenderDeletion;
import org.snowjak.rays.frontend.messages.backend.commands.RequestSingleRenderTaskSubmission;
import org.snowjak.rays.frontend.messages.frontend.ReceivedRenderCreation;
import org.snowjak.rays.frontend.messages.frontend.ReceivedRenderUpdate;
import org.snowjak.rays.frontend.model.entity.QRender;
import org.snowjak.rays.frontend.model.entity.Render;
import org.snowjak.rays.frontend.model.repository.RenderRepository;
import org.snowjak.rays.frontend.ui.presentation.AbstractPresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Streams;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.spring.annotation.VaadinSessionScope;

/**
 * Presents a list of {@link Render}s, which can be used by any UI component.
 * <p>
 * </p>
 * 
 * @author snowjak88
 *
 */
@Component
@VaadinSessionScope
public class RenderListPresentation extends AbstractPresentation<RenderListEventListener, AbstractRenderListEvent> {
	
	private static final Logger LOG = LoggerFactory.getLogger(RenderListPresentation.class);
	private static final Comparator<Render> RENDER_COMPARATOR = (r1, r2) -> r1.getCreated().compareTo(r2.getCreated());
	private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME
			.withLocale(LocaleContextHolder.getLocale()).withZone(ZoneId.systemDefault());
	
	private LinkedList<RenderListItemBean> renders = new LinkedList<>();
	
	@Autowired
	private RenderRepository renderRepository;
	
	@Autowired
	private EventBus bus;
	
	/**
	 * Construct a new {@link RenderListPresentation} of all {@link Render}s that
	 * are direct children of the Render denoted by the given UUID.
	 * 
	 * @param uuid
	 */
	public RenderListPresentation() {
		
		super();
	}
	
	@PostConstruct
	@Transactional(readOnly = true)
	public void onConstruct() {
		
		renders.addAll(Streams
				.stream(renderRepository.findAll(QRender.render.parent.isNull(), Sort.by(Direction.DESC, "created")))
				.map(this::convertToBean).collect(Collectors.toList()));
		
		bus.register(this);
	}
	
	@PreDestroy
	public void onDestroy() {
		
		bus.unregister(this);
	}
	
	public List<RenderListItemBean> getRenders() {
		
		return Collections.unmodifiableList(renders);
	}
	
	@Subscribe
	public void receiveRenderCreation(ReceivedRenderCreation creation) {
		
		final var newBean = convertToBean(creation.getRender());
		fireEvent(new AddToRenderListEvent(newBean));
		
		if (newBean.getParent() != null)
			fireEvent(new UpdateRenderListEvent(newBean.getParent()));
	}
	
	@Subscribe
	public void receiveRenderUpdate(ReceivedRenderUpdate update) {
		
		final var updatedBean = convertToBean(update.getRender(), false, false, false);
		fireEvent(new UpdateRenderListEvent(updatedBean));
	}
	
	public void doDelete(String id) {
		
		synchronized (this) {
			
			LOG.debug("doDelete(UUID={})", id);
			
			final var bean = getBeanById(id);
			if (bean == null) {
				LOG.warn("doDelete(UUID={}) -- UUID not recognized!", id);
				return;
			}
			
			if (bean.getParent() != null)
				bean.getParent().getChildren().removeIf(r -> r.getId().equals(bean.getId()));
			renders.removeIf(r -> r.getId().equals(bean.getId()));
			
			fireEvent(new RemoveFromRenderListEvent(bean));
			bus.post(new RequestRenderDeletion(UUID.fromString(id)));
		}
	}
	
	public void doOpenClose(String id) {
		
		synchronized (this) {
			
			LOG.debug("doOpenClose(UUID={}) ...", id);
			
			final var bean = getBeanById(id);
			if (bean == null) {
				LOG.warn("doOpenClose(UUID={}) -- UUID not recognized!", id);
				return;
			}
			
			bean.setOpen(!bean.isOpen());
			fireEvent(new UpdateRenderListEvent(bean));
		}
		
	}
	
	public void doDecomposeRender(String id) {
		
		LOG.debug("doDecomposeRender(UUID={}) ...", id);
		bus.post(new RequestRenderDecomposition(UUID.fromString(id), 128));
	}
	
	public void doSubmitRender(String id) {
		
		LOG.debug("doSubmitRender(UUID={}) ...", id);
		
		final var bean = getBeanById(id);
		if (bean == null) {
			LOG.warn("doSubmitRender(UUID={}) -- UUID not recognized!", id);
			return;
		}
		
		doSubmitRender(bean);
	}
	
	private void doSubmitRender(RenderListItemBean bean) {
		
		if (bean.getChildren().isEmpty()) {
			
			LOG.debug("doSubmitRender(UUID={}): has no children: submitting as a single-render ...", bean.getId());
			bus.post(new RequestSingleRenderTaskSubmission(UUID.fromString(bean.getId())));
			
		} else {
			
			LOG.debug("doSubmitRender(UUID={}): has {} children: submitting each child ...", bean.getId(),
					bean.getChildren().size());
			bean.getChildren().forEach(this::doSubmitRender);
			
		}
	}
	
	/**
	 * Convert the given {@link Render} to a {@link RenderListItemBean}. As a
	 * side-effect, adds this bean (and all its parents and children, if they don't
	 * exist yet) to this Presentation instance.
	 * 
	 * @param entity
	 * @return
	 */
	public RenderListItemBean convertToBean(Render entity) {
		
		return convertToBean(entity, true, true, true);
	}
	
	private RenderListItemBean convertToBean(Render entity, boolean allowConvertParent, boolean allowConvertChildren,
			boolean addToPresentation) {
		
		final var existingBean = Optional.ofNullable(getBeanById(entity.getUuid()));
		final var bean = existingBean.orElse(new RenderListItemBean());
		
		//
		// Set simple properties
		//
		
		bean.setId(entity.getUuid());
		
		bean.setCreated((entity.getCreated() == null) ? "" : DATE_FMT.format(entity.getCreated()));
		bean.setSubmitted((entity.getSubmitted() == null) ? "" : DATE_FMT.format(entity.getSubmitted()));
		bean.setCompleted((entity.getCompleted() == null) ? "" : DATE_FMT.format(entity.getCompleted()));
		
		bean.setPercentComplete(entity.getPercentCompleteFloat());
		
		bean.setSize(entity.getWidth() + "x" + entity.getHeight() + " (" + entity.getSpp() + "spp)");
		
		bean.setOpen((existingBean.isPresent()) ? existingBean.get().isOpen() : false);
		
		//
		// Set rules-based properties
		//
		bean.setRemovable(!entity.isChild());
		
		bean.setOpenable(entity.isParent() && !entity.getChildren().isEmpty());
		
		bean.setDecomposable(!entity.isDecomposed());
		
		bean.setSubmittable(
				(entity.getSubmitted() == null && entity.getCompleted() == null) || (entity.getSubmitted() != null
						&& entity.getCompleted() != null && entity.getSubmitted().isBefore(entity.getCompleted())));
		
		bean.setHasResult(entity.getPngBase64() != null && !entity.getPngBase64().trim().isEmpty());
		bean.setImage(entity.inflateResultAsResource());
		
		//
		// Set bean parent (if it exists and if that's allowed)
		//
		
		//
		// (if we're updating an existing bean, we'll need to unhook this bean from its
		// parent)
		if (bean.getParent() != null) {
			bean.getParent().getChildren().remove(bean);
			bean.setParent(null);
		}
		
		final RenderListItemBean parentBean;
		if (entity.getParent() == null)
			parentBean = null;
		
		else
			parentBean = Optional.ofNullable(getBeanById(entity.getParent().getUuid()))
					.orElse(allowConvertParent
							? convertToBean(entity.getParent(), allowConvertParent, false, addToPresentation)
							: null);
		
		bean.setParent(parentBean);
		if (parentBean != null)
			bean.getParent().getChildren().add(bean);
		
		bean.setTopLevelParent(getTopLevelBean(bean));
		
		//
		// Set bean children (if that's allowed)
		//
		
		//
		// (if we're updating an existing bean, we'll need to unhook this bean from its
		// old children)
		//
		if (!bean.getChildren().isEmpty()) {
			bean.getChildren().forEach(r -> r.setParent(null));
			bean.getChildren().clear();
		}
		
		final List<RenderListItemBean> childBeans = new LinkedList<>();
		if (!entity.getChildren().isEmpty())
			entity.getChildren().stream().sorted(RENDER_COMPARATOR)
					.map(r -> Optional.ofNullable(getBeanById(r.getUuid()))
							.orElse(allowConvertChildren
									? convertToBean(r, false, allowConvertChildren, addToPresentation)
									: null))
					.filter(r -> r != null).forEach(childBeans::add);
		
		bean.setChildren(childBeans);
		childBeans.forEach(cb -> cb.setParent(bean));
		
		//
		//
		//
		
		if (addToPresentation && !existingBean.isPresent())
			synchronized (this) {
				renders.add(bean);
			}
		
		return bean;
	}
	
	/**
	 * Get the {@link RenderListItemBean} currently held in this
	 * {@link RenderListPresentation} that matches the given Render-ID.
	 * 
	 * @param id
	 * @return <code>null</code> if no such RenderListItemBean exists in this
	 *         Presentation
	 */
	public RenderListItemBean getBeanById(String id) {
		
		synchronized (this) {
			return renders.stream().map(r -> getBeanById(id, r)).filter(r -> r != null).findFirst().orElse(null);
		}
	}
	
	private RenderListItemBean getBeanById(String id, RenderListItemBean current) {
		
		synchronized (this) {
			if (current.getId().equals(id))
				return current;
			
			return current.getChildren().stream().map(r -> getBeanById(id, r)).filter(r -> r != null).findFirst()
					.orElse(null);
		}
	}
	
	/**
	 * Given a {@link RenderListItemBean}, identify its top-most parent (which may
	 * be itself)
	 * 
	 * @param bean
	 * @return <code>null</code> if bean is <code>null</code>
	 */
	public RenderListItemBean getTopLevelBean(RenderListItemBean bean) {
		
		if (bean == null)
			return null;
		
		return (bean.getParent() == null) ? bean : getTopLevelBean(bean.getParent());
	}
	
}
