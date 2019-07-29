package org.snowjak.rays.support.model.repository;

import java.util.stream.Stream;

import org.snowjak.rays.support.model.entity.RenderSetup;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RenderSetupRepository
		extends PagingAndSortingRepository<RenderSetup, Long>, QuerydslPredicateExecutor<RenderSetup> {
	
	@Query("select r from RenderSetup r")
	public Stream<RenderSetup> streamAll();
	
}
