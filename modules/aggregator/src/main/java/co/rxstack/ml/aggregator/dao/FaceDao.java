package co.rxstack.ml.aggregator.dao;

import java.util.List;

import co.rxstack.ml.aggregator.model.db.Face;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

@Component
public interface FaceDao extends CrudRepository<Face, Integer> {

	List<Face> findAll();

	List<Face> findAllByIdentityId(int identityId);

}
