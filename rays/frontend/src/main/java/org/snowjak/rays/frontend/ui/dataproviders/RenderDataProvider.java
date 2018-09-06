package org.snowjak.rays.frontend.ui.dataproviders;

import java.util.List;
import java.util.stream.Stream;

import org.snowjak.rays.frontend.model.entity.QRender;
import org.snowjak.rays.frontend.model.entity.Render;
import org.snowjak.rays.frontend.model.repository.RenderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.vaadin.artur.spring.dataprovider.FilterablePageableDataProvider;

import com.google.common.collect.Streams;
import com.vaadin.data.provider.HierarchicalDataProvider;
import com.vaadin.data.provider.HierarchicalQuery;
import com.vaadin.data.provider.Query;
import com.vaadin.data.provider.QuerySortOrder;
import com.vaadin.data.provider.Sort;

@Component
public class RenderDataProvider extends FilterablePageableDataProvider<Render, String>
		implements HierarchicalDataProvider<Render, String> {
	
	private static final long serialVersionUID = -4837480007561814276L;
	
	@Autowired
	private RenderRepository renderRepository;
	
	@Override
	protected Page<Render> fetchFromBackEnd(Query<Render, String> query, Pageable page) {
		
		return renderRepository.findAll(QRender.render.uuid.containsIgnoreCase(getOptionalFilter().orElse("")), page);
	}
	
	@Override
	protected List<QuerySortOrder> getDefaultSortOrders() {
		
		return Sort.desc("created").build();
	}
	
	@Override
	protected int sizeInBackEnd(Query<Render, String> query) {
		
		return (int) renderRepository.count(QRender.render.uuid.containsIgnoreCase(getOptionalFilter().orElse("")));
	}
	
	@Override
	public boolean isInMemory() {
		
		return false;
	}
	
	@Override
	public int getChildCount(HierarchicalQuery<Render, String> query) {
		
		return (int) renderRepository.count(QRender.render.uuid.containsIgnoreCase(query.getFilter().orElse(""))
				.and((query.getParent() == null) ? QRender.render.parent.isNull()
						: QRender.render.parent.uuid.eq(query.getParent().getUuid())));
	}
	
	@Override
	public Stream<Render> fetchChildren(HierarchicalQuery<Render, String> query) {
		
		return Streams
				.stream(renderRepository.findAll(QRender.render.uuid.containsIgnoreCase(query.getFilter().orElse(""))
						.and((query.getParent() == null) ? QRender.render.parent.isNull()
								: QRender.render.parent.uuid.eq(query.getParent().getUuid()))));
	}
	
	@Override
	public boolean hasChildren(Render item) {
		
		return item.isParent();
	}
	
	@Override
	public Object getId(Render item) {
		
		return ((Render) super.getId(item)).getUuid();
	}
	
}
