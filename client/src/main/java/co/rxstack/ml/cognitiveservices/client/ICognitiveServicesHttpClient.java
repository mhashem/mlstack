package co.rxstack.ml.cognitiveservices.client;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import co.rxstack.ml.common.FaceDetectionResult;
import co.rxstack.ml.common.PersonGroup;

/**
 * @author mhachem on 9/27/2017.
 */
public interface ICognitiveServicesHttpClient {

	
	boolean createPersonGroup(String personGroupId, String name);

	boolean deletePersonGroup(String personGroupId);
	
	Optional<PersonGroup> getPersonGroup(String personGroupId);
	
	boolean trainPersonGroup(String personGroupId);
	
	/*
	 * https://[location].api.cognitive.microsoft.com/face/v1.0/persongroups/{personGroupId}/persons
	 */

	/**
	 * 
	 * Request Body:
	 * 		name	String	Display name of the target person. The maximum length is 128.
	 * 		userData (optional)	String	Optional fields for user-provided data attached to a person. Size limit is 16KB.
	 * 
	 * @param personGroupId {@link String} Specifying the target person group to create the person.
	 * @return {@link Optional<String>} created personId if successfully created
	 */
	Optional<String> createPerson(String personGroupId);
	
	List<FaceDetectionResult> detect(InputStream inputStream);
	
}
