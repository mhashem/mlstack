package co.rxstack.ml.cognitiveservices.service.impl;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Optional;

import co.rxstack.ml.client.cognitiveservices.ICognitiveServicesClient;
import co.rxstack.ml.cognitiveservices.service.IPersonGroupService;
import co.rxstack.ml.common.model.PersonGroup;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author mhachem on 9/27/2017.
 */
@Service
public class PersonGroupService implements IPersonGroupService {

	private static final Logger logger = getLogger(PersonGroupService.class);
	
	private ICognitiveServicesClient cognitiveServicesClient;
	
	@Autowired
	public PersonGroupService(ICognitiveServicesClient cognitiveServicesClient) {
		this.cognitiveServicesClient = cognitiveServicesClient;
	}
	
	@Override
	public boolean createPersonGroup(String personGroupId, String name) {
		logger.info("creating person group personGroupId {} , name {}", personGroupId, name);
		return cognitiveServicesClient.createPersonGroup(personGroupId, name);
	}

	@Override
	public boolean deletePersonGroup(String personGroupId) {
		logger.info("deleting person group {}", personGroupId);
		return cognitiveServicesClient.deletePersonGroup(personGroupId);
	}

	@Override
	public Optional<PersonGroup> getPersonGroup(String personGroupId) {
		logger.info("reading person group {}", personGroupId);
		return cognitiveServicesClient.getPersonGroup(personGroupId);
	}

	@Override
	public boolean trainPersonGroup(String personGroupId) {
		logger.info("training person group {}", personGroupId);
		return cognitiveServicesClient.trainPersonGroup(personGroupId);
	}
}
