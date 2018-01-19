package co.rxstack.ml.aggregator;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface IFaceExtractorService {

	Optional<byte[]> extractFace(byte[] imageBytes) throws IOException;

	List<byte[]> detectFaces(byte[] imageBytes) throws IOException;

}
