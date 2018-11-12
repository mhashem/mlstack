package co.rxstack.ml.client.cognitiveservices.impl;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import co.rxstack.ml.client.cognitiveservices.ICognitiveServicesClient;
import co.rxstack.ml.common.model.FaceDetectionResult;
import co.rxstack.ml.common.model.FaceIdentificationRequest;
import co.rxstack.ml.common.model.FaceIdentificationResult;
import co.rxstack.ml.common.model.FaceRectangle;
import co.rxstack.ml.common.model.Person;
import co.rxstack.ml.common.model.PersonGroup;
import co.rxstack.ml.common.model.TrainingStatus;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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
public class CognitiveServicesClient implements ICognitiveServicesClient {
	
	private static final String SUBSCRIPTION_KEY_HEADER = "Ocp-Apim-Subscription-Key";
	private static final String ACCEPT_HEADER = "accept";
	private static final String CONTENT_TYPE = "Content-Type";
	private static final String APPLICATION_JSON = "application/json";
	private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

	private static final Logger log = LoggerFactory.getLogger(CognitiveServicesClient.class);
	
	private URI serviceUri;
	private String subscriptionKey;
	private ObjectMapper objectMapper;
	

	public CognitiveServicesClient(URI uri, String subscriptionKey) {
		Preconditions.checkNotNull(uri);
		Preconditions.checkNotNull(subscriptionKey);
		
		serviceUri = UriComponentsBuilder.fromUri(uri).build().toUri();
		this.subscriptionKey = subscriptionKey;
		this.objectMapper = new ObjectMapper();
	}

	@Override
	public boolean createPersonGroup(String personGroupId, String name) {

		HttpResponse<JsonNode> response = null;

		JSONObject jsonBody = new JSONObject();
		jsonBody.put("name", name);
		
		URI uri = UriComponentsBuilder.fromUri(serviceUri).path("/persongroups")
			.pathSegment(personGroupId).build().toUri();

		try {
			response = Unirest.put(uri.toString())
				.header(ACCEPT_HEADER, APPLICATION_JSON)
				.header(SUBSCRIPTION_KEY_HEADER, subscriptionKey)
				.header(CONTENT_TYPE, APPLICATION_JSON)
				.body(jsonBody)
				.asJson();
			if (response.getStatus() == HttpStatus.SC_OK) {
				return true;
			}

			if (response.getStatus() == HttpStatus.SC_CONFLICT) {
				log.warn("Person group {} conflicted with already existing {}", personGroupId, response.getBody());
				return true;
			}
		} catch (UnirestException e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}

	@Override
	public boolean deletePersonGroup(String personGroupId) {
		URI uri = UriComponentsBuilder.fromUri(serviceUri).path("/persongroups/{personGroupId}")
			.buildAndExpand(personGroupId).toUri();
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
	public List<PersonGroup> getPersonGroups() {
		URI uri = UriComponentsBuilder.fromUri(serviceUri).path("/persongroups").build().toUri();
			//.pathSegment(personGroupId).build().toUri();
		try {
			HttpResponse<JsonNode> response =
				Unirest.get(uri.toString())
					.header(SUBSCRIPTION_KEY_HEADER, subscriptionKey)
					.asJson();
			
			if (response.getStatus() == HttpStatus.SC_OK) {
				return objectMapper.readValue(response.getRawBody(), new TypeReference<List<PersonGroup>>() {
				});
			}
		} catch (UnirestException | IOException e) {
			log.error(e.getMessage(), e);
		}
		return ImmutableList.of();
	}

	@Override
	public Optional<PersonGroup> getPersonGroup(String personGroupId) {
		return this.getPersonGroups().stream()
			.filter(pGroup -> pGroup.getPersonGroupId().equals(personGroupId)).findFirst();
	}

	@Override
	public Optional<TrainingStatus> getPersonGroupTrainingStatus(String personGroupId) {
		log.info("getting person group training status {}", personGroupId);
		URI uri = UriComponentsBuilder.fromUri(serviceUri).path("/persongroups/{personGroupId}/training")
			.buildAndExpand(personGroupId).toUri();

		try {
			HttpResponse<JsonNode> response =
				Unirest.get(uri.toString()).header(SUBSCRIPTION_KEY_HEADER, subscriptionKey).asJson();
			
			if (response.getStatus() == HttpStatus.SC_OK) {
				TrainingStatus trainingStatus = objectMapper.readValue(response.getRawBody(), TrainingStatus.class);
				return Optional.ofNullable(trainingStatus);
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
	public Optional<Person> createPerson(String personGroupId, String personName, @Nullable String userData) {
		log.info("creating person in group {}", personGroupId);

		Preconditions.checkNotNull(personGroupId);
		Preconditions.checkNotNull(personName);

		URI uri= UriComponentsBuilder.fromUri(serviceUri).path("/persongroups/{personGroupId}/persons")
			.buildAndExpand(personGroupId).toUri();

		try {
			ImmutableMap<String, String> dataMap =
				ImmutableMap.of("name", personName, "userData", userData == null ? "" : userData);

			HttpResponse<JsonNode> response =
				Unirest.post(uri.toString())
					.header(SUBSCRIPTION_KEY_HEADER, subscriptionKey)
					.header(CONTENT_TYPE, APPLICATION_JSON)
					.body(objectMapper.writeValueAsString(dataMap))
					.asJson();

			if (response.getStatus() == HttpStatus.SC_OK) {
				return Optional.ofNullable(objectMapper.readValue(response.getRawBody(), Person.class));
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return Optional.empty();
	}

	@Override
	public Optional<Person> getPerson(String personGroupId, String personId) {
		log.info("getting person with id {} at group", personId, personGroupId);
		URI uri = UriComponentsBuilder.fromUri(serviceUri).path("/persongroups/{personGroupId}/persons/{personId}")
			.buildAndExpand(personGroupId, personId).toUri();
		try {
			HttpResponse<JsonNode> response =
				Unirest.get(uri.toString()).header(SUBSCRIPTION_KEY_HEADER, subscriptionKey)
					.asJson();
			if (response.getStatus() == HttpStatus.SC_OK) {
				Person person = objectMapper.readValue(response.getRawBody(), Person.class);
				return Optional.ofNullable(person);
			}
		} catch (UnirestException | IOException e) {
			log.error(e.getMessage(), e);
		}
		return Optional.empty();
	}

	@Override
	public Optional<String> addPersonFace(String personGroupId, String personId,
		@Nullable
			FaceRectangle faceRectangle, byte[] imageBytes) {

		log.info("adding person face, person group {}, personId {}", personGroupId, personId);

		Preconditions.checkNotNull(personGroupId);
		Preconditions.checkNotNull(personId);

		URI uri = UriComponentsBuilder.fromUri(serviceUri)
			.path("/persongroups/{personGroupId}/persons/{personId}/persistedFaces")
			.buildAndExpand(personGroupId, personId).toUri();

		try {
			if (imageBytes.length > 0) {

				if (faceRectangle != null) {
					uri =
						UriComponentsBuilder.fromUri(uri).queryParam("targetFace", faceRectangle.encodeAsQueryParam()).build().toUri();
				}

				HttpResponse<JsonNode> response =
					Unirest.post(uri.toString()).header(SUBSCRIPTION_KEY_HEADER, subscriptionKey)
						.header(CONTENT_TYPE, APPLICATION_OCTET_STREAM).body(imageBytes).asJson();

				if (response.getStatus() == HttpStatus.SC_OK) {
					return Optional.ofNullable(response.getBody().getObject().get("persistedFaceId").toString());
				} else {
					log.error("Error adding person face: {}", response.getBody());
				}
			}

		} catch (UnirestException e) {
			log.error(e.getMessage(), e);
		}
		return Optional.empty();
	}

	@Override
	public List<FaceDetectionResult> detect(byte[] imageBytes) {

		URI uri = UriComponentsBuilder.fromUri(serviceUri).path("/detect")
			.queryParam("returnFaceId", true)
			.queryParam("returnFaceLandmarks", false)
			.queryParam("returnFaceAttributes", "age,gender")
			.build().toUri();

		try {
			if (imageBytes.length > 0) {
				HttpResponse<JsonNode> response =
					Unirest.post(uri.toString())
						.header(CONTENT_TYPE, APPLICATION_OCTET_STREAM)
						.header(SUBSCRIPTION_KEY_HEADER, subscriptionKey)
						.body(imageBytes)
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

	@Override
	public List<FaceIdentificationResult> identify(String personGroupId, List<String> faceIds, int maxCandidates,
		double confidenceThreshold) {

		Preconditions.checkNotNull(personGroupId);
		Preconditions.checkNotNull(faceIds);
		Preconditions.checkArgument(!faceIds.isEmpty() && faceIds.size() <= 10);
		Preconditions.checkArgument(maxCandidates <= 5);
		Preconditions.checkArgument(confidenceThreshold > 0 && confidenceThreshold <= 1);
			
		URI uri = UriComponentsBuilder.fromUri(serviceUri).path("/identify").build().toUri();
		FaceIdentificationRequest faceIdentificationRequest =
			new FaceIdentificationRequest(personGroupId, faceIds, maxCandidates, confidenceThreshold);

		try {
			HttpResponse<JsonNode> response =
				Unirest.post(uri.toString())
					.header(CONTENT_TYPE, APPLICATION_JSON)
					.header(SUBSCRIPTION_KEY_HEADER, subscriptionKey)
					.body(objectMapper.writeValueAsString(faceIdentificationRequest))
					.asJson();

			if (response.getStatus() == HttpStatus.SC_OK) {
				return  objectMapper.readValue(response.getRawBody(),
					new TypeReference<List<FaceIdentificationResult>>(){});
			}

		} catch (UnirestException | IOException e) {
			log.error(e.getMessage(), e);
		}
		return ImmutableList.of();
	}
}
