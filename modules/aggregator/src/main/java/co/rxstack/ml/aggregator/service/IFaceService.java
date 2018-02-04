package co.rxstack.ml.aggregator.service;

import java.util.List;

import co.rxstack.ml.aggregator.model.db.Face;

public interface IFaceService {

	List<Face> findByIdentityId(int identityId);

}
