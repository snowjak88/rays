package org.snowjak.rays.frontend.ui.presentation.renderlist;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snowjak.rays.frontend.messages.backend.commands.RequestRenderDecomposition;
import org.snowjak.rays.frontend.messages.backend.commands.RequestRenderDeletion;
import org.snowjak.rays.frontend.messages.backend.commands.RequestSingleRenderTaskSubmission;
import org.snowjak.rays.frontend.messages.frontend.AddWindowRequest;
import org.snowjak.rays.frontend.messages.frontend.ReceivedRenderChildrenUpdate;
import org.snowjak.rays.frontend.messages.frontend.ReceivedRenderCreation;
import org.snowjak.rays.frontend.messages.frontend.ReceivedRenderDeletion;
import org.snowjak.rays.frontend.messages.frontend.ReceivedRenderUpdate;
import org.snowjak.rays.frontend.model.entity.Render;
import org.snowjak.rays.frontend.model.repository.RenderRepository;
import org.snowjak.rays.frontend.ui.components.ModalRenderResultWindow;
import org.snowjak.rays.frontend.ui.presentation.AbstractPresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.vaadin.spring.annotation.ViewScope;

/**
 * Presents a list of {@link Render}s, which can be used by any UI component.
 * <p>
 * </p>
 * 
 * @author snowjak88
 *
 */
@Component
@ViewScope
public class RenderListPresentation
		extends AbstractPresentation<RenderListEventListener<AbstractRenderListEvent>, AbstractRenderListEvent> {
	
	private static final Logger LOG = LoggerFactory.getLogger(RenderListPresentation.class);
	private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME
			.withLocale(LocaleContextHolder.getLocale()).withZone(ZoneId.systemDefault());
	
	private final Map<String, Boolean> isOpen = new HashMap<>(), isOpenable = new HashMap<>(),
			isDecomposable = new HashMap<>(), isSubmittable = new HashMap<>(), isViewable = new HashMap<>();
	
	private final Map<String, Map<Field, String>> values = new HashMap<>();
	private final Map<String, String> parents = new HashMap<>();
	
	private final Comparator<String> UUID_COMPARATOR = (id1, id2) -> {
		final var createdComparison = values.computeIfAbsent(id1, (id) -> new HashMap<>())
				.computeIfAbsent(Field.CREATED, (f) -> "").compareTo(
						values.computeIfAbsent(id2, (id) -> new HashMap<>()).computeIfAbsent(Field.CREATED, (f) -> ""));
		if (createdComparison != 0)
			return createdComparison;
		
		return id1.compareTo(id2);
	};
	private final Map<String, SortedSet<String>> children = new HashMap<>();
	
	/**
	 * Identifies what fields this Presentation is capable of presenting.
	 * 
	 * @author snowjak88
	 *
	 */
	public enum Field {
		/**
		 */
		ID("id", (r, p) -> r.getUuid()),
		/**
		 */
		PROGRESS("progress", (r, p) -> Float.toString(r.getPercentCompleteFloat())),
		/**
		 */
		SIZE("size", (r, p) -> r.getSize()),
		/**
		 */
		CREATED("created", (r, p) -> (r.getCreated() == null) ? "" : DATE_FMT.format(r.getCreated())),
		/**
		 */
		SUBMITTED("submitted", (r, p) -> (r.getSubmitted() == null) ? "" : DATE_FMT.format(r.getSubmitted())),
		/**
		 */
		COMPLETED("completed", (r, p) -> (r.getCompleted() == null) ? "" : DATE_FMT.format(r.getCompleted())),
		/**
		 */
		IS_OPEN("isOpen", (r, p) -> Boolean.toString(p.isOpen.computeIfAbsent(r.getUuid(), (id) -> false))),
		/**
		 */
		IS_OPENABLE("isOpenable",
				(r, p) -> Boolean.toString(p.isOpenable.compute(r.getUuid(), (id, cv) -> r.isParent()))),
		/**
		 */
		IS_DECOMPOSABLE("isDecomposable", (r, p) -> Boolean
				.toString(p.isDecomposable.compute(r.getUuid(), (id, cv) -> !r.isDecomposed() && !r.isChild()))),
		/**
		 */
		IS_SUBMITTABLE("isSubmittable",
				(r, p) -> Boolean.toString(p.isSubmittable.compute(r.getUuid(),
						(id, cv) -> !(r.getSubmitted() != null && r.getCompleted() != null
								&& r.getSubmitted().isBefore(r.getCompleted()))
								&& !(r.getSubmitted() != null && r.getCompleted() == null)))),
		/**
		 */
		IS_VIEWABLE("isViewable",
				(r, p) -> Boolean.toString(p.isViewable.compute(r.getUuid(), (id, cv) -> r.getPngBase64() != null))),
		/**
		 */
		IS_REMOVABLE("isRemovable", (r, p) -> Boolean.toString(true));
		
		private String name;
		private BiFunction<Render, RenderListPresentation, String> supplier;
		
		Field(String name, BiFunction<Render, RenderListPresentation, String> supplier) {
			
			this.name = name;
			this.supplier = supplier;
		}
		
		public String getName() {
			
			return name;
		}
		
		public BiFunction<Render, RenderListPresentation, String> getSupplier() {
			
			return supplier;
		}
	}
	
	@Autowired
	private RenderRepository renderRepository;
	
	@Autowired
	private ModalRenderResultWindow resultWindow;
	
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
		
		final var renders = renderRepository.findAll();
		
		for (var r : renders) {
			parents.put(r.getUuid(), (r.getParent() == null) ? null : r.getParent().getUuid());
			
			r.getChildren().forEach(c -> children.computeIfAbsent(r.getUuid(), (id) -> new TreeSet<>(UUID_COMPARATOR))
					.add(c.getUuid()));
		}
		
		for (Render r : renders)
			for (Field f : Field.values())
				values.computeIfAbsent(r.getUuid(), (id) -> new HashMap<>()).put(f, f.getSupplier().apply(r, this));
			
		bus.register(this);
	}
	
	@PreDestroy
	public void onDestroy() {
		
		bus.unregister(this);
	}
	
	public List<String> getRootIDs() {
		
		return values.keySet().stream().filter(id -> parents.get(id) == null).sorted(UUID_COMPARATOR)
				.collect(Collectors.toList());
	}
	
	public String getParentID(String id) {
		
		return parents.get(id);
	}
	
	public List<String> getChildIDs(String id) {
		
		return new LinkedList<>(children.computeIfAbsent(id, (i) -> new TreeSet<>(UUID_COMPARATOR)));
	}
	
	public Map<Field, String> getFieldsFor(String id) {
		
		return values.computeIfAbsent(id, (i) -> new HashMap<>());
	}
	
	@Subscribe
	@AllowConcurrentEvents
	public void receiveRenderCreation(ReceivedRenderCreation creation) {
		
		synchronized (this) {
			final var render = creation.getRender();
			final var uuid = render.getUuid();
			final var parentUuid = (render.getParent() == null) ? null : render.getParent().getUuid();
			
			convertToFields(render, true);
			parents.put(uuid, parentUuid);
			
			fireEvent(new AddToRenderListEvent(uuid));
			
			if (parentUuid != null) {
				children.computeIfAbsent(parentUuid, (id) -> new TreeSet<>(UUID_COMPARATOR)).add(uuid);
				fireEvent(new UpdateChildrenListRenderListEvent(parentUuid));
			}
		}
	}
	
	@Subscribe
	@AllowConcurrentEvents
	public void receiveRenderUpdate(ReceivedRenderUpdate update) {
		
		synchronized (this) {
			final var fields = getUpdatedFields(update.getRender(), false);
			values.get(update.getRender().getUuid()).putAll(fields);
			
			fireEvent(new UpdateRenderListEvent(update.getRender().getUuid(), fields));
		}
	}
	
	@Subscribe
	@AllowConcurrentEvents
	public void receiveRenderChildrenUpdate(ReceivedRenderChildrenUpdate update) {
		
		synchronized (this) {
			children.get(update.getRender().getUuid()).clear();
			update.getRender().getChildren().stream().map(Render::getUuid)
					.forEach(id -> children.get(update.getRender().getUuid()).add(id));
			
			fireEvent(new UpdateChildrenListRenderListEvent(update.getRender().getUuid()));
		}
	}
	
	@Subscribe
	@AllowConcurrentEvents
	public void receiveRenderDeletion(ReceivedRenderDeletion deletion) {
		
		synchronized (this) {
			
			LOG.debug("receiveRenderDeletion(UUID={}): deleting children ...", deletion.getId());
			
			children.get(deletion.getId())
					.forEach(childID -> receiveRenderDeletion(new ReceivedRenderDeletion(childID)));
			
			parents.remove(deletion.getId());
			children.remove(deletion.getId());
			values.remove(deletion.getId());
			
			fireEvent(new RemoveFromRenderListEvent(deletion.getId()));
			fireEvent(new UpdateChildrenListRenderListEvent(deletion.getId()));
			
		}
	}
	
	public void doDelete(String id) {
		
		synchronized (this) {
			
			LOG.debug("doDelete(UUID={})", id);
			bus.post(new RequestRenderDeletion(UUID.fromString(id)));
			
		}
	}
	
	public void doOpenClose(String id) {
		
		synchronized (this) {
			
			LOG.debug("doOpenClose(UUID={}) ...", id);
			
			if (!values.containsKey(id)) {
				LOG.warn("doOpenClose(UUID={}) -- UUID not recognized!", id);
				return;
			}
			
			final var isOpen = Boolean.parseBoolean(values.get(id).get(Field.IS_OPEN));
			final var newIsOpen = !isOpen;
			final var newIsOpenString = Boolean.toString(newIsOpen);
			
			LOG.trace("doOpenClose(UUID={}): {} --> {} ...", id, isOpen, newIsOpenString);
			values.get(id).put(Field.IS_OPEN, newIsOpenString);
			this.isOpen.put(id, newIsOpen);
			
			fireEvent(new UpdateRenderListEvent(id, Map.of(Field.IS_OPEN, newIsOpenString)));
		}
		
	}
	
	public void doDecomposeRender(String id) {
		
		LOG.debug("doDecomposeRender(UUID={}) ...", id);
		
		disableButtons(id);
		
		bus.post(new RequestRenderDecomposition(UUID.fromString(id), 128));
	}
	
	public void doSubmitRender(String id) {
		
		LOG.debug("doSubmitRender(UUID={}) ...", id);
		
		disableButtons(id);
		
		if (children.get(id).isEmpty()) {
			
			LOG.debug("doSubmitRender(UUID={}): has no children: submitting as a single-render ...", id);
			bus.post(new RequestSingleRenderTaskSubmission(UUID.fromString(id)));
			
		} else {
			
			LOG.debug("doSubmitRender(UUID={}): has {} children: submitting each child ...", id,
					children.get(id).size());
			children.get(id).forEach(this::doSubmitRender);
			
		}
		
	}
	
	public void doViewResult(String id) {
		
		LOG.debug("doViewResult(UUID={}) ...", id);
		
		if (!Boolean.parseBoolean(values.get(id).get(Field.IS_VIEWABLE))) {
			LOG.warn("doViewResult(UUID={}): is not viewable!", id);
			return;
		}
		
		final var entity = renderRepository.findById(id).orElse(null);
		if (entity == null) {
			LOG.warn("doViewResult(UUID={}): UUID is not recognized!", id);
			return;
		}
		
		resultWindow.setImage(id, entity.inflateResultAsResource());
		bus.post(new AddWindowRequest(resultWindow));
		
	}
	
	private void disableButtons(String id) {
		
		//@formatter:off
		final var buttonUpdates = Map.of(
				Field.IS_DECOMPOSABLE, Boolean.toString(false),
				Field.IS_SUBMITTABLE, Boolean.toString(false),
				Field.IS_VIEWABLE, Boolean.toString(false),
				Field.IS_REMOVABLE, Boolean.toString(false)
		);
		//@formatter:on
		
		values.get(id).putAll(buttonUpdates);
		fireEvent(new UpdateRenderListEvent(id, buttonUpdates));
	}
	
	/**
	 * Given a {@link Render}, convert it into a Map of all defined {@link Field}s
	 * onto String values. Compare each Field's values with the values currently
	 * defined in {@code values}. Return the subset of the converted Map that has
	 * updated values.
	 * <p>
	 * Optionally, {@code put} those updates into {@code values}.
	 * </p>
	 * 
	 * @param render
	 * @param putUpdates
	 * @return
	 */
	private Map<Field, String> getUpdatedFields(Render render, boolean putUpdates) {
		
		final Map<Field, String> result = new HashMap<>();
		final Map<Field, String> newValues = convertToFields(render, putUpdates);
		
		if (!values.containsKey(render.getUuid()))
			return values.get(render.getUuid());
		
		for (Field f : newValues.keySet())
			if (!newValues.get(f).equals(values.get(render.getUuid()).get(f))) {
				final var newValue = newValues.get(f);
				if (putUpdates)
					values.get(render.getUuid()).put(f, newValue);
				result.put(f, newValue);
			}
		
		return result;
	}
	
	/**
	 * Given a {@link Render}, convert it into a Map of all defined {@link Field}s
	 * onto String values.
	 * <p>
	 * Optionally, {@code put} those values into {@code values}.
	 * </p>
	 * 
	 * @param render
	 * @param putValues
	 * @return
	 */
	private Map<Field, String> convertToFields(Render render, boolean putValues) {
		
		final Map<Field, String> result = new HashMap<>();
		
		for (Field f : Field.values()) {
			final var newValue = f.getSupplier().apply(render, this);
			if (putValues)
				values.computeIfAbsent(render.getUuid(), (id) -> new HashMap<>()).put(f, newValue);
			result.put(f, newValue);
		}
		
		return result;
	}
}
