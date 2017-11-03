package co.rxstack.ml.client;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class StackClient implements IStackClient {

	private static final Logger log = LoggerFactory.getLogger(StackClient.class);

	private String serviceName;
	private RestTemplate restTemplate;
	private DiscoveryClient discoveryClient;

	public StackClient(RestTemplate restTemplate, DiscoveryClient discoveryClient, String serviceName) {
		Preconditions.checkNotNull(restTemplate);
		Preconditions.checkNotNull(discoveryClient);
		Preconditions.checkNotNull(serviceName);
		this.restTemplate = restTemplate;
		this.discoveryClient = discoveryClient;
		this.serviceName = serviceName;
	}

	@Override
	public boolean pushIndexedFacesIds(Map<String, String> indexedFacesIds) {
		Optional<ServiceInstance> mlServiceOpt = discoveryClient.getInstances(serviceName).stream().findFirst();
		if (mlServiceOpt.isPresent()) {
			URI postUri =
				UriComponentsBuilder.fromUri(mlServiceOpt.get().getUri()).path("/api/v1/person/faceIds").build()
					.toUri();
			log.info("Pushing indexed faces-ids to stack client at {}", postUri.toString());
			restTemplate.postForEntity(postUri, indexedFacesIds, null);
			log.info("Ids pushed successfully");
			return true;
		} else {
			log.warn("No instances of {} found up at registry", serviceName);
		}
		return false;
	}
}
