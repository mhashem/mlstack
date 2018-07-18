package co.rxstack.ml.faces.dao;

import java.util.Optional;

import co.rxstack.ml.faces.model.Identity;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

@Component
public interface IdentityDao extends CrudRepository<Identity, Integer> {

	Optional<Identity> findById(int id);

}
