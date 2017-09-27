package co.rxstack.ml.cognitiveservices.services.impl;

import co.rxstack.ml.cognitiveservices.client.ICognitiveServicesHttpClient;
import co.rxstack.ml.cognitiveservices.services.IPersonService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author mhachem on 9/27/2017.
 */
public class PersonService implements IPersonService {

	private ICognitiveServicesHttpClient cognitiveServicesHttpClient;

	@Autowired
	public PersonService(ICognitiveServicesHttpClient cognitiveServicesHttpClient) {
		this.cognitiveServicesHttpClient = cognitiveServicesHttpClient;
	}
	
}
