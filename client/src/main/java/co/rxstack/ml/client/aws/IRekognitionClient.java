package co.rxstack.ml.client.aws;

import java.util.List;
import java.util.Optional;

import co.rxstack.ml.common.model.Candidate;
import co.rxstack.ml.common.model.ComparisonResult;
import co.rxstack.ml.common.model.FaceDetectionResult;
import com.amazonaws.services.rekognition.model.IndexFacesResult;

/**
 * @author mhachem on 9/28/2017.
 */
public interface IRekognitionClient {

	List<String> deleteFaces(String collectionId);

	int createCollection(String collectionId);

	int deleteCollection(String collectionId);

	Optional<ComparisonResult> compareFaces(byte[] faceOneBytes, byte[] faceTwoBytes);

	List<FaceDetectionResult> detect(byte[] imageBytes);

	List<Candidate> searchFacesByImage(String collectionId, byte[] imageBytes, int maxFaces);

	Optional<IndexFacesResult> indexFace(String collectionId, byte[] imageBytes);

}
