package co.rxstack.ml.faces.service;

import java.util.List;
import java.util.Map;

import co.rxstack.ml.faces.model.Face;

import com.google.common.collect.BiMap;
import com.google.common.collect.Multimap;
import io.reactivex.Observable;

public interface IFaceService {

	Observable<Integer> getRefreshingFacesObservable();

	List<Face> findAll();

	List<Face> findByIdentityId(int identityId);

	Map<Integer, double[]> findAllFaceIdEmbeddingsMap();

	BiMap<Integer, Integer> findFaceIdentityBiMap();

	Multimap<Integer, double[]> findAllEmbeddingsForIdentity();

}
