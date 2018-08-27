package org.snowjak.rays.frontend.model.repository;

import org.snowjak.rays.frontend.model.entity.Scene;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SceneRepository extends CrudRepository<Scene, Long> {
	
}
