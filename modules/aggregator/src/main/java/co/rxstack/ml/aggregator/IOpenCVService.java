package co.rxstack.ml.aggregator;

import java.io.IOException;
import java.util.List;

public interface IOpenCVService {
	List<byte[]> detectFaces(byte[] imageBytes) throws IOException;
}
