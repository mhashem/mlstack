package co.rxstack.ml.cognitiveservices.service.impl;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.InputStream;
import java.util.Optional;

import javax.annotation.Nullable;

import co.rxstack.ml.client.cognitiveservices.ICognitiveServicesClient;
import co.rxstack.ml.cognitiveservices.service.IPersonService;
import co.rxstack.ml.common.model.FaceRectangle;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author mhachem on 9/27/2017.
 */
@Service
public class PersonService implements IPersonService {

	private static final Logger log = getLogger(PersonService.class);

	private ICognitiveServicesClient cognitiveServicesClient;

	@Autowired
	public PersonService(ICognitiveServicesClient cognitiveServicesClient) {
		this.cognitiveServicesClient = cognitiveServicesClient;
	}

	@Override
	public Optional<String> createPerson(String personGroupId, String personName, String userData) {
		log.info("creating person {},{} in group {}", personName, userData, personGroupId);
		return cognitiveServicesClient.createPerson(personGroupId, personName, userData);
	}

	@Override
	public Optional<String> addPersonFace(String personGroupId, String personId,
		@Nullable
			FaceRectangle faceRectangle, InputStream stream) {
		log.info("adding person face for person {} group {}", personId, personGroupId);
		return cognitiveServicesClient.addPersonFace(personGroupId, personId, faceRectangle, stream);
	}
}
