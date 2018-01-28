package co.rxstack.ml.aggregator.service;

import java.util.Optional;

import co.rxstack.ml.aggregator.model.db.Face;

public interface IFaceService {
	Optional<Face> getFaceByAwsFaceId(String awsFaceId);

	Optional<Face> getFaceByCognitivePersonId(String cognitivePersonId);

	Optional<Face> getFaceByPersonId(String personId);
}
