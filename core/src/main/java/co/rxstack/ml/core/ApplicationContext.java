package co.rxstack.ml.core;

import java.net.URI;

import co.rxstack.ml.client.cognitiveservices.ICognitiveServicesClient;
import co.rxstack.ml.client.cognitiveservices.impl.CognitiveServicesClient;
import co.rxstack.ml.cognitiveservices.service.IFaceDetectionService;
import co.rxstack.ml.cognitiveservices.service.IPersonGroupService;
import co.rxstack.ml.cognitiveservices.service.IPersonService;
import co.rxstack.ml.cognitiveservices.service.impl.FaceDetectionService;
import co.rxstack.ml.cognitiveservices.service.impl.PersonGroupService;
import co.rxstack.ml.cognitiveservices.service.impl.PersonService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author mhachem on 9/27/2017.
 */
@Configuration
public class ApplicationContext {

	@Value("${subscription-key}")
	private String subscriptionKey;
	
	@Value("${cognitive-service-url}")
	private String cognitiveServiceUrl;
	
	@Bean
	public URI cognitiveServicesUri() {
		return URI.create(cognitiveServiceUrl);
	}

	@Bean
	public ICognitiveServicesClient cognitiveServicesHttpClient(URI cognitiveServicesUri) {
		return new CognitiveServicesClient(cognitiveServicesUri, subscriptionKey);
	}
	
	@Bean
	public IPersonGroupService personGroupService(ICognitiveServicesClient cognitiveServicesHttpClient) {
		return new PersonGroupService(cognitiveServicesHttpClient);
	}
	
	@Bean
	public IPersonService personService(ICognitiveServicesClient cognitiveServicesHttpClient) {
		return new PersonService(cognitiveServicesHttpClient);
	}
	
	@Bean
	public IFaceDetectionService faceDetectionService(ICognitiveServicesClient cognitiveServicesHttpClient) {
		return new FaceDetectionService(cognitiveServicesHttpClient);
	}
	
}
