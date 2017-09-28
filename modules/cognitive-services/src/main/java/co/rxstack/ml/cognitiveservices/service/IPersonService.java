package co.rxstack.ml.cognitiveservices.service;

import java.util.Optional;

/**
 * @author mhachem on 9/27/2017.
 */
public interface IPersonService {

	Optional<String> createPerson(String personGroupId, String personName, String userData);

}
