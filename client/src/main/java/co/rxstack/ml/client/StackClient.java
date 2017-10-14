package co.rxstack.ml.client;

import java.net.URI;
import java.util.Map;

import com.google.common.base.Preconditions;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class StackClient implements IStackClient {

	private URI serviceUri;
	private RestTemplate restTemplate;

	public StackClient(RestTemplate restTemplate, String serviceUri) {
		Preconditions.checkNotNull(restTemplate);
		Preconditions.checkNotNull(serviceUri);
		this.restTemplate = restTemplate;
		this.serviceUri = URI.create(serviceUri);
	}

	@Override
	public void pushIndexedFacesIds(Map<String, String> indexedFacesIds) {
		URI postUri = UriComponentsBuilder.fromUri(serviceUri).path("/api/v1/person/faceIds").build().toUri();
		restTemplate.postForEntity(postUri, indexedFacesIds, null);
	}
}
