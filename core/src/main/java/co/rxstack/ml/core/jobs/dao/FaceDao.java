package co.rxstack.ml.core.jobs.dao;

import java.util.List;

import co.rxstack.ml.core.jobs.model.Face;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

@Component
public interface FaceDao extends CrudRepository<Face, String> {

	List<Face> findAll();
}
