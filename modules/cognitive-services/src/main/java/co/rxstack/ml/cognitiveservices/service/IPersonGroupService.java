package co.rxstack.ml.cognitiveservices.service;

import java.util.Optional;

import co.rxstack.ml.common.model.PersonGroup;

/**
 * @author mhachem on 9/27/2017.
 */
public interface IPersonGroupService {
	
	boolean createPersonGroup(String personGroupId, String name);
	
	boolean deletePersonGroup(String personGroupId);
	
	Optional<PersonGroup> getPersonGroup(String personGroupId);

	boolean trainPersonGroup(String personGroupId);
}
