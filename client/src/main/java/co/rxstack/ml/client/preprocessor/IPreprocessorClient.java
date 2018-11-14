package co.rxstack.ml.client.preprocessor;

import java.util.List;
import java.util.Optional;

import co.rxstack.ml.common.model.FaceBox;

public interface IPreprocessorClient<T, R> {
	
	R align(T imageBytes);

	List<FaceBox> detectFaces(T imageBytes);
	
}
