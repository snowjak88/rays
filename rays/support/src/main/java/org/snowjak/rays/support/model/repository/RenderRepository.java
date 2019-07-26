package org.snowjak.rays.support.model.repository;

import java.util.stream.Stream;

import org.snowjak.rays.support.model.entity.Render;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RenderRepository extends PagingAndSortingRepository<Render, String>, QuerydslPredicateExecutor<Render> {
	
	@Query("select r from Render r")
	public Stream<Render> streamAll();
	
}
