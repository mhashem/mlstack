package co.rxstack.ml.cognitiveservices.service.impl;

import java.util.Optional;

import co.rxstack.ml.client.cognitiveservices.ICognitiveServicesClient;
import co.rxstack.ml.cognitiveservices.service.IPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author mhachem on 9/27/2017.
 */
@Service
public class PersonService implements IPersonService {

	private ICognitiveServicesClient cognitiveServicesHttpClient;

	@Autowired
	public PersonService(ICognitiveServicesClient cognitiveServicesHttpClient) {
		this.cognitiveServicesHttpClient = cognitiveServicesHttpClient;
	}

	@Override
	public Optional<String> createPerson(String personGroupId, String personName, String userData) {
		return cognitiveServicesHttpClient.createPerson(personGroupId, personName, userData);
	}
}
