package co.rxstack.ml.client.cognitiveservices;

import java.util.List;
import java.util.Optional;

import co.rxstack.ml.common.model.FaceDetectionResult;
import co.rxstack.ml.common.model.FaceIdentificationResult;
import co.rxstack.ml.common.model.FaceRectangle;
import co.rxstack.ml.common.model.Person;
import co.rxstack.ml.common.model.PersonGroup;
import co.rxstack.ml.common.model.TrainingStatus;

/**
 * @author mhachem on 9/27/2017.
 */
public interface ICognitiveServicesClient {

	boolean createPersonGroup(String personGroupId, String name);

	boolean deletePersonGroup(String personGroupId);
	
	List<PersonGroup> getPersonGroups();

	Optional<PersonGroup> getPersonGroup(String personGroupId);

	Optional<TrainingStatus> getPersonGroupTrainingStatus(String personGroupId);

	boolean trainPersonGroup(String personGroupId);

	Optional<Person> createPerson(String personGroupId, String personName, String userData);

	Optional<Person> getPerson(String personGroupId, String personId);

	Optional<String> addPersonFace(String personGroupId, String personId,
		FaceRectangle faceRectangle, byte[] imageBytes);

	List<FaceDetectionResult> detect(byte[] imageBytes);

	List<FaceIdentificationResult> identify(String personGroupId, List<String> faceIds, int maxCandidates,
		double confidenceThreshold);

}
