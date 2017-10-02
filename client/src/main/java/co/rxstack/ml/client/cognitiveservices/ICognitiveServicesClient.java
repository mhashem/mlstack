package co.rxstack.ml.client.cognitiveservices;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import co.rxstack.ml.common.model.FaceDetectionResult;
import co.rxstack.ml.common.model.FaceRectangle;
import co.rxstack.ml.common.model.PersonGroup;

/**
 * @author mhachem on 9/27/2017.
 */
public interface ICognitiveServicesClient {

	boolean createPersonGroup(String personGroupId, String name);

	boolean deletePersonGroup(String personGroupId);
	
	Optional<PersonGroup> getPersonGroup(String personGroupId);
	
	boolean trainPersonGroup(String personGroupId);

	Optional<String> createPerson(String personGroupId, String personName, String userData);

	Optional<String> addPersonFace(String personGroupId, String personId,
		FaceRectangle faceRectangle, InputStream inputStream);

	List<FaceDetectionResult> detect(byte[] imageBytes);
	
}
