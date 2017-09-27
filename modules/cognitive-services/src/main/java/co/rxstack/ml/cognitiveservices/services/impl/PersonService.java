package co.rxstack.ml.cognitiveservices.services.impl;

import java.util.Optional;

import co.rxstack.ml.cognitiveservices.client.ICognitiveServicesHttpClient;
import co.rxstack.ml.cognitiveservices.services.IPersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author mhachem on 9/27/2017.
 */
@Service
public class PersonService implements IPersonService {

	private ICognitiveServicesHttpClient cognitiveServicesHttpClient;

	@Autowired
	public PersonService(ICognitiveServicesHttpClient cognitiveServicesHttpClient) {
		this.cognitiveServicesHttpClient = cognitiveServicesHttpClient;
	}

	@Override
	public Optional<String> createPerson(String personGroupId, String personName, String userData) {
		return cognitiveServicesHttpClient.createPerson(personGroupId, personName, userData);
	}
}
