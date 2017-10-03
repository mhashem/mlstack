package co.rxstack.ml.aws.rekognition.service;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import co.rxstack.ml.common.model.ComparisonResult;
import co.rxstack.ml.common.model.FaceDetectionResult;

/**
 * @author mhachem on 9/28/2017.
 */
public interface IRekognitionService {
	Optional<ComparisonResult> compareFaces(InputStream faceOneStream, InputStream faceTwoStream);

	List<FaceDetectionResult> detect(byte[] imageBytes);
}
