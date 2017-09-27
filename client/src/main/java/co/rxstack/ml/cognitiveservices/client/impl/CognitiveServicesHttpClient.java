package co.rxstack.ml.cognitiveservices.client.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import co.rxstack.ml.cognitiveservices.client.ICognitiveServicesHttpClient;
import co.rxstack.ml.common.FaceDetectionResult;
import co.rxstack.ml.common.PersonGroup;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author mhachem on 9/27/2017.
 */
public class CognitiveServicesHttpClient implements ICognitiveServicesHttpClient {
	
	private static final String SUBSCRIPTION_KEY_HEADER = "Ocp-Apim-Subscription-Key";
	private static final String ACCEPT_HEADER = "accept";
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String APPLICATION_JSON = "application/json";
	private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

	private static final Logger log = LoggerFactory.getLogger(CognitiveServicesHttpClient.class);
	
	private URI serviceUri;
	private String subscriptionKey;
	private ObjectMapper objectMapper;
	

	public CognitiveServicesHttpClient(URI uri, String subscriptionKey) {
		Preconditions.checkNotNull(uri);
		Preconditions.checkNotNull(subscriptionKey);
		
		serviceUri = UriComponentsBuilder.fromUri(uri).build().toUri();
		this.subscriptionKey = subscriptionKey;
		this.objectMapper = new ObjectMapper();
	}

	@Override
	public boolean createPersonGroup(String personGroupId, String name) {

		JSONObject jsonBody = new JSONObject();
		jsonBody.put("name", name);
		
		URI uri = UriComponentsBuilder.fromUri(serviceUri).path("/persongroups")
			.pathSegment(personGroupId).build().toUri();

		try {
			HttpResponse<JsonNode> response =
				Unirest.put(uri.toString())
					.header(ACCEPT_HEADER, APPLICATION_JSON)
					.header(SUBSCRIPTION_KEY_HEADER, subscriptionKey)
					.header(CONTENT_TYPE, APPLICATION_JSON)
					.body(jsonBody)
					.asJson();
			if (response.getStatus() == HttpStatus.SC_OK) {
				return true;
			}
		} catch (UnirestException e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}

	@Override
	public boolean deletePersonGroup(String personGroupId) {
		URI uri = UriComponentsBuilder.fromUri(serviceUri).path("/persongroups")
			.pathSegment(personGroupId).build().toUri();
		try {
			HttpResponse<JsonNode> response =
				Unirest.delete(uri.toString())
					.header(SUBSCRIPTION_KEY_HEADER, subscriptionKey)
					.asJson();
			if (response.getStatus() == HttpStatus.SC_OK) {
				return true;
			}
		} catch (UnirestException e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}

	@Override
	public Optional<PersonGroup> getPersonGroup(String personGroupId) {
		
		URI uri = UriComponentsBuilder.fromUri(serviceUri).path("/persongroups")
			.pathSegment(personGroupId).build().toUri();

		try {
			HttpResponse<JsonNode> response =
				Unirest.get(uri.toString())
					.header(SUBSCRIPTION_KEY_HEADER, subscriptionKey)
					.asJson();
			
			if (response.getStatus() == HttpStatus.SC_OK) {
				PersonGroup personGroup = objectMapper.readValue(response.getRawBody(), PersonGroup.class);
				return Optional.ofNullable(personGroup);
			}
		} catch (UnirestException | IOException e) {
			log.error(e.getMessage(), e);
		}
		return Optional.empty();
	}

	@Override
	public boolean trainPersonGroup(String personGroupId) {

		URI uri = UriComponentsBuilder.fromUri(serviceUri).path("/persongroups/{personGroupId}/train")
			.pathSegment(personGroupId).build().toUri();

		try {
			HttpResponse<JsonNode> response =
				Unirest.post(uri.toString())
					.header(SUBSCRIPTION_KEY_HEADER, subscriptionKey)
					.asJson();
			
			if (response.getStatus() == HttpStatus.SC_ACCEPTED) {
				return true;
			}
		} catch (UnirestException e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}

	@Override
	public Optional<String> createPerson(String personGroupId) {
		return Optional.empty();
	}

	@Override
	public List<FaceDetectionResult> detect(InputStream stream) {

		URI uri = UriComponentsBuilder.fromUri(serviceUri).path("/detect")
			.queryParam("returnFaceId", true)
			.queryParam("returnFaceLandmarks", false)
			.queryParam("returnFaceAttributes", "age,gender")
			.build().toUri();

		try {

			byte[] bytes = new byte[stream.available()];
			stream.read(bytes);

			HttpResponse<JsonNode> response =
				Unirest.post(uri.toString())
					.header(CONTENT_TYPE, APPLICATION_OCTET_STREAM)
					.header(SUBSCRIPTION_KEY_HEADER, subscriptionKey)
					.body(bytes)
					.asJson();
			if (response.getStatus() == HttpStatus.SC_OK) {
				return objectMapper.readValue(response.getRawBody(), new TypeReference<List<FaceDetectionResult>>(){});
			}
		} catch (UnirestException | IOException e) {
			log.error(e.getMessage(), e);
		}

		return ImmutableList.of();
	}
}