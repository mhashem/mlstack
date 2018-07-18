package co.rxstack.ml.faces.service;

import java.util.List;
import java.util.Map;

import co.rxstack.ml.faces.model.Face;

public interface IFaceService {

	List<Face> findByIdentityId(int identityId);

	Map<String, float[]> findAllEmbeddings();

}
