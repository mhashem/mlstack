package co.rxstack.ml.core;

import java.net.URI;

import co.rxstack.ml.cognitiveservices.client.ICognitiveServicesHttpClient;
import co.rxstack.ml.cognitiveservices.client.impl.CognitiveServicesHttpClient;
import co.rxstack.ml.cognitiveservices.services.IFaceDetectionService;
import co.rxstack.ml.cognitiveservices.services.IPersonGroupService;
import co.rxstack.ml.cognitiveservices.services.IPersonService;
import co.rxstack.ml.cognitiveservices.services.impl.FaceDetectionService;
import co.rxstack.ml.cognitiveservices.services.impl.PersonGroupService;
import co.rxstack.ml.cognitiveservices.services.impl.PersonService;
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
	public ICognitiveServicesHttpClient cognitiveServicesHttpClient(URI cognitiveServicesUri) {
		return new CognitiveServicesHttpClient(cognitiveServicesUri, subscriptionKey);
	}
	
	@Bean
	public IPersonGroupService personGroupService(ICognitiveServicesHttpClient cognitiveServicesHttpClient) {
		return new PersonGroupService(cognitiveServicesHttpClient);
	}
	
	@Bean
	public IPersonService personService(ICognitiveServicesHttpClient cognitiveServicesHttpClient) {
		return new PersonService(cognitiveServicesHttpClient);
	}
	
	@Bean
	public IFaceDetectionService faceDetectionService(ICognitiveServicesHttpClient cognitiveServicesHttpClient) {
		return new FaceDetectionService(cognitiveServicesHttpClient);
	}
	
}
