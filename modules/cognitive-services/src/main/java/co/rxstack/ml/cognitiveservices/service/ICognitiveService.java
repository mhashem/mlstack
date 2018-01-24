package co.rxstack.ml.cognitiveservices.service;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import co.rxstack.ml.api.FaceIndexer;
import co.rxstack.ml.cognitiveservices.model.CognitiveIndexingResult;
import co.rxstack.ml.common.model.FaceDetectionResult;
import co.rxstack.ml.common.model.FaceIdentificationResult;
import co.rxstack.ml.common.model.FaceRectangle;
import co.rxstack.ml.common.model.Person;
import co.rxstack.ml.common.model.PersonGroup;
import co.rxstack.ml.common.model.TrainingStatus;

/**
 * @author mhachem on 10/8/2017.
 */
public interface ICognitiveService extends FaceIndexer<CognitiveIndexingResult> {

	List<FaceDetectionResult> detect(byte[] imageBytes);

	boolean createPersonGroup(String personGroupId, String name);

	boolean deletePersonGroup(String personGroupId);

	Optional<PersonGroup> getPersonGroup(String personGroupId);

	boolean trainPersonGroup();

	Optional<TrainingStatus> getTrainingStatus();

	Optional<Person> createPerson(String personGroupId, String personName, String userData);

	Optional<Person> getPerson(String personGroupId, String personId);

	Optional<String> addPersonFace(String personGroupId, String personId,
		@Nullable
			FaceRectangle faceRectangle, byte[] imageBytes);

	Optional<FaceIdentificationResult> identifyFace(byte[] imageBytes);

	List<FaceIdentificationResult> identifyFaces(byte[] imageBytes);

	/*List<FaceIdentificationResult> identify(String personGroupId, List<String> faceIds, int maxCandidates,
		double confidenceThreshold);*/
}
