package co.rxstack.ml.cognitiveservices.services;

import java.io.InputStream;
import java.util.List;

import co.rxstack.ml.common.FaceDetectionResult;

/**
 * @author mhachem on 9/27/2017.
 */
public interface IFaceDetectionService {
	
	List<FaceDetectionResult> detect(InputStream inputStream);
	
}
