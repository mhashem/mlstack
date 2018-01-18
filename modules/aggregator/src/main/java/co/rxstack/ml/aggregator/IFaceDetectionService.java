package co.rxstack.ml.aggregator;

import java.io.IOException;
import java.util.List;

public interface IFaceDetectionService {
	List<byte[]> detectFaces(byte[] imageBytes) throws IOException;
}
