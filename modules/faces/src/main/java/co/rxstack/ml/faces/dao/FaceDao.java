package co.rxstack.ml.faces.dao;

import java.util.List;

import co.rxstack.ml.faces.model.Face;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public interface FaceDao extends CrudRepository<Face, Integer> {

	List<Face> findAll();

	List<Face> findAllByIdentityId(int identityId);

	@Modifying
	@Transactional
	@Query("delete from Face f where f.identity.id = :identityId")
	void deleteFaceByIdentityId(@Param("identityId") int identityId);

}
