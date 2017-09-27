package co.rxstack.ml.cognitiveservices.services;

import java.util.Optional;

import co.rxstack.ml.common.PersonGroup;

/**
 * @author mhachem on 9/27/2017.
 */
public interface IPersonGroupService {
	
	boolean createPersonGroup(String personGroupId, String name);
	
	boolean deletePersonGroup(String personGroupId);
	
	Optional<PersonGroup> getPersonGroup(String personGroupId);
	
	
	
}
