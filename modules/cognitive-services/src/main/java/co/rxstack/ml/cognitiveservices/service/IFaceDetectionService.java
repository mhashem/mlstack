package co.rxstack.ml.cognitiveservices.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import co.rxstack.ml.common.model.FaceDetectionResult;

/**
 * @author mhachem on 9/27/2017.
 */
public interface IFaceDetectionService {
	
	List<FaceDetectionResult> detect(InputStream inputStream) throws IOException;
	
}
