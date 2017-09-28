package co.rxstack.ml.client;

import java.net.URI;

import co.rxstack.ml.client.cognitiveservices.impl.CognitiveServicesClient;
import co.rxstack.ml.client.cognitiveservices.ICognitiveServicesClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author mhachem on 9/27/2017.
 */
@Configuration
public class TestContext {

	@Bean
	public URI serviceUri() {
		return URI.create("https://northeurope.api.cognitive.microsoft.com/face/v1.0");
	}

	@Bean
	public ICognitiveServicesClient cognitiveServicesClient(URI serviceUri) {
		return new CognitiveServicesClient(serviceUri, "8407dfc043ae486a8f36bff5034da21f");
	}
	
}
