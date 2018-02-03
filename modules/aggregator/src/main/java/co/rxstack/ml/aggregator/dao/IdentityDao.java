package co.rxstack.ml.aggregator.dao;

import java.util.Optional;

import co.rxstack.ml.aggregator.model.db.Identity;

import org.springframework.data.repository.CrudRepository;

public interface IdentityDao extends CrudRepository<Identity, Integer> {

	Optional<Identity> findById(int id);

}
