package org.snowjak.rays.frontend.model.repository;

import org.snowjak.rays.frontend.model.entity.Render;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RenderRepository extends PagingAndSortingRepository<Render, String>, QuerydslPredicateExecutor<Render> {
	
}
