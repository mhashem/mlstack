package co.rxstack.ml.client.preprocessor;

import java.util.List;
import java.util.Optional;

import co.rxstack.ml.common.model.FaceBox;

public interface IPreprocessorClient {
	
	Optional<byte[]> align(byte[] imageBytes);

	List<FaceBox> detectFaces(byte[] imageBytes);
	
}
