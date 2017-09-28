package co.rxstack.ml.cognitiveservices.client;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import co.rxstack.ml.common.FaceDetectionResult;
import co.rxstack.ml.common.FaceRectangle;
import co.rxstack.ml.common.PersonGroup;

/**
 * @author mhachem on 9/27/2017.
 */
public interface ICognitiveServicesHttpClient {

	boolean createPersonGroup(String personGroupId, String name);

	boolean deletePersonGroup(String personGroupId);
	
	Optional<PersonGroup> getPersonGroup(String personGroupId);
	
	boolean trainPersonGroup(String personGroupId);

	Optional<String> createPerson(String personGroupId, String personName, String userData);

	Optional<String> addPersonFace(String personGroupId, String personId,
		FaceRectangle faceRectangle, InputStream inputStream);

	List<FaceDetectionResult> detect(InputStream inputStream);
	
}
