package co.rxstack.ml.faces.dao;

import java.util.Optional;

import co.rxstack.ml.faces.model.Identity;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

@Component
public interface IdentityDao extends CrudRepository<Identity, Integer> {

	Optional<Identity> findById(int id);

	@Modifying
	@Query("delete from Identity where id = :id")
	int deleteById(@Param("id") int id);

}
