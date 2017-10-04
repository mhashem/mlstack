package co.rxstack.ml.cognitiveservices.service;

import java.io.InputStream;
import java.util.Optional;

import javax.annotation.Nullable;

import co.rxstack.ml.common.model.FaceRectangle;

/**
 * @author mhachem on 9/27/2017.
 */
public interface IPersonService {

	Optional<String> createPerson(String personGroupId, String personName, String userData);

	Optional<String> addPersonFace(String personGroupId, String personId,
		@Nullable FaceRectangle faceRectangle, byte[] imageBytes);

}
