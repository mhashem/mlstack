package co.rxstack.ml.cognitiveservices.client;

import java.net.URI;

import co.rxstack.ml.cognitiveservices.client.impl.CognitiveServicesHttpClient;
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
	public ICognitiveServicesHttpClient cognitiveServicesHttpClient(URI serviceUri) {
		return new CognitiveServicesHttpClient(serviceUri, "8407dfc043ae486a8f36bff5034da21f");
	}
	
}
