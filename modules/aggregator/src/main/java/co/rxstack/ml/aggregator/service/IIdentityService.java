package co.rxstack.ml.aggregator.service;

import java.util.List;
import java.util.Optional;

import co.rxstack.ml.aggregator.model.db.Face;
import co.rxstack.ml.aggregator.model.db.Identity;

public interface IIdentityService {

	List<Identity> findAll();

	Optional<Identity> findIdentityByAwsFaceId(String awsFaceId);

	Optional<Identity> findIdentityByCognitivePersonId(String cognitivePersonId);

	Optional<Identity> findIdentityByFaceId(int faceId);

	Optional<Identity> findById(int id);

	List<Face> findFaceListByIdentityId(int identityId);

	Identity save(Identity identity);

}
