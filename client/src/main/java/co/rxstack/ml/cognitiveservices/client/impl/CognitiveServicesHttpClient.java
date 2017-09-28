package co.rxstack.ml.cognitiveservices.client.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import co.rxstack.ml.cognitiveservices.client.ICognitiveServicesHttpClient;
import co.rxstack.ml.common.FaceDetectionResult;
import co.rxstack.ml.common.FaceRectangle;
import co.rxstack.ml.common.PersonGroup;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;
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
		log.info("getting person group {}", personGroupId);
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
		log.info("attempting to train person group {}", personGroupId);
		URI uri = UriComponentsBuilder.fromUri(serviceUri).path("/persongroups/{personGroupId}/train")
			.buildAndExpand(personGroupId).toUri();

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
	public Optional<String> createPerson(String personGroupId, String personName, String userData) {
		log.info("creating person in group {}", personGroupId);

		Preconditions.checkNotNull(personGroupId);
		Preconditions.checkNotNull(personName);

		URI uri= UriComponentsBuilder.fromUri(serviceUri).path("/persongroups/{personGroupId}/persons")
			.buildAndExpand(personGroupId).toUri();

		try {
			ImmutableMap<String, String> dataMap =
				ImmutableMap.of("name", personName, "userDate", userData);

			HttpResponse<JsonNode> response =
				Unirest.post(uri.toString())
					.header(SUBSCRIPTION_KEY_HEADER, subscriptionKey)
					.header(CONTENT_TYPE, APPLICATION_JSON)
					.body(objectMapper.writeValueAsString(dataMap))
					.asJson();

			if (response.getStatus() == HttpStatus.SC_OK) {
				return Optional.ofNullable(response.getBody().getObject().get("personId").toString());
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return Optional.empty();
	}

	@Override
	public Optional<String> addPersonFace(String personGroupId, String personId,
		@Nullable FaceRectangle faceRectangle, InputStream stream) {

		log.info("adding person face, person group {}, personId {}", personGroupId, personId);

		Preconditions.checkNotNull(personGroupId);
		Preconditions.checkNotNull(personId);

		URI uri= UriComponentsBuilder.fromUri(serviceUri)
			.path("/persongroups/{personGroupId}/persons/{personId}/persistedFaces")
			.buildAndExpand(personGroupId, personId).toUri();

		try {
			byte[] bytes = new byte[stream.available()];
			int read = stream.read(bytes);

			if (read > 0) {
				HttpRequestWithBody httpRequest =
					Unirest.post(uri.toString())
						.header(SUBSCRIPTION_KEY_HEADER, subscriptionKey)
						.header(CONTENT_TYPE, APPLICATION_OCTET_STREAM);

				if (faceRectangle != null) {
					httpRequest = httpRequest.queryString("targetFace", faceRectangle.encodeAsQueryParam());
				}

				HttpResponse<JsonNode> response = httpRequest.body(bytes).asJson();

				if (response.getStatus() == HttpStatus.SC_OK) {
					return Optional.ofNullable(response.getBody().getObject()
						.get("persistedFaceId").toString());
				}
			}

		} catch (UnirestException | IOException e) {
			log.error(e.getMessage(), e);
		}
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
			int read = stream.read(bytes);

			if (read > 0) {
				HttpResponse<JsonNode> response =
					Unirest.post(uri.toString())
						.header(CONTENT_TYPE, APPLICATION_OCTET_STREAM)
						.header(SUBSCRIPTION_KEY_HEADER, subscriptionKey)
						.body(bytes)
						.asJson();
				if (response.getStatus() == HttpStatus.SC_OK) {
					return objectMapper.readValue(response.getRawBody(),
						new TypeReference<List<FaceDetectionResult>>(){});
				}
			}
		} catch (UnirestException | IOException e) {
			log.error(e.getMessage(), e);
		}

		return ImmutableList.of();
	}
}
