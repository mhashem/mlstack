package co.rxstack.ml.cognitiveservices.services.impl;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Optional;

import co.rxstack.ml.cognitiveservices.client.ICognitiveServicesHttpClient;
import co.rxstack.ml.cognitiveservices.services.IPersonGroupService;
import co.rxstack.ml.common.PersonGroup;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author mhachem on 9/27/2017.
 */
@Service
public class PersonGroupService implements IPersonGroupService {

	private static final Logger logger = getLogger(PersonGroupService.class);
	
	private ICognitiveServicesHttpClient cognitiveServicesHttpClient;
	
	@Autowired
	public PersonGroupService(ICognitiveServicesHttpClient cognitiveServicesHttpClient) {
		this.cognitiveServicesHttpClient = cognitiveServicesHttpClient;
	}
	
	@Override
	public boolean createPersonGroup(String personGroupId, String name) {
		logger.info("creating person group personGroupId {} , name {}", personGroupId, name);
		return cognitiveServicesHttpClient.createPersonGroup(personGroupId, name);
	}

	@Override
	public boolean deletePersonGroup(String personGroupId) {
		logger.info("deleting person group {}", personGroupId);
		return cognitiveServicesHttpClient.deletePersonGroup(personGroupId);
	}

	@Override
	public Optional<PersonGroup> getPersonGroup(String personGroupId) {
		logger.info("reading person group {}", personGroupId);
		return cognitiveServicesHttpClient.getPersonGroup(personGroupId);
	}
}
