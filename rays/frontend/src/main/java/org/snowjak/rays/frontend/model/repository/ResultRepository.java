package org.snowjak.rays.frontend.model.repository;

import org.snowjak.rays.frontend.model.entity.Result;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResultRepository extends CrudRepository<Result, Long> {
	
}
