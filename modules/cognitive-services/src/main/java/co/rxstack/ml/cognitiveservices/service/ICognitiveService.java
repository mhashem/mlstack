package co.rxstack.ml.cognitiveservices.service;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import co.rxstack.ml.common.model.FaceDetectionResult;
import co.rxstack.ml.common.model.FaceIdentificationResult;
import co.rxstack.ml.common.model.FaceRectangle;
import co.rxstack.ml.common.model.PersonGroup;

/**
 * @author mhachem on 10/8/2017.
 */
public interface ICognitiveService {

	List<FaceDetectionResult> detect(byte[] imageBytes);

	boolean createPersonGroup(String personGroupId, String name);

	boolean deletePersonGroup(String personGroupId);

	Optional<PersonGroup> getPersonGroup(String personGroupId);

	boolean trainPersonGroup(String personGroupId);

	Optional<String> createPerson(String personGroupId, String personName, String userData);

	Optional<String> addPersonFace(String personGroupId, String personId,
		@Nullable
			FaceRectangle faceRectangle, byte[] imageBytes);

	List<FaceIdentificationResult> identify(String personGroupId, List<String> faceIds, int maxCandidates,
		double confidenceThreshold);
}
