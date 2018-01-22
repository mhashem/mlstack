package co.rxstack.ml.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface FaceIndexer<T> {

	Optional<T> indexFace(byte[] imageBytes, Map<String, String> bundleMap);

	List<T> indexFaces(byte[] imageBytes, Map<String, String> bundleMap);

}
