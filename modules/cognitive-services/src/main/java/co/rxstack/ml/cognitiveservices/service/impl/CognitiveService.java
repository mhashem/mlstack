package co.rxstack.ml.cognitiveservices.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import co.rxstack.ml.client.cognitiveservices.ICognitiveServicesClient;
import co.rxstack.ml.cognitiveservices.service.ICognitiveService;
import co.rxstack.ml.common.model.FaceDetectionResult;
import co.rxstack.ml.common.model.FaceIdentificationResult;
import co.rxstack.ml.common.model.FaceRectangle;
import co.rxstack.ml.common.model.PersonGroup;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author mhachem on 10/8/2017.
 */
@Service
public class CognitiveService implements ICognitiveService {

	private static final Logger log = LoggerFactory.getLogger(CognitiveService.class);

	private ICognitiveServicesClient cognitiveServicesClient;

	private Map<String, String> faceIdsCacheMap;

	@Autowired
	public CognitiveService(ICognitiveServicesClient cognitiveServicesClient) {
		Preconditions.checkNotNull(cognitiveServicesClient);
		this.cognitiveServicesClient = cognitiveServicesClient;
		this.faceIdsCacheMap = new ConcurrentHashMap<>();
	}

	@Override
	public List<FaceDetectionResult> detect(byte[] imageBytes) {
		return cognitiveServicesClient.detect(imageBytes);
	}

	@Override
	public boolean createPersonGroup(String personGroupId, String name) {
		log.info("Creating person group personGroupId {} , name {}", personGroupId, name);
		return cognitiveServicesClient.createPersonGroup(personGroupId, name);
	}

	@Override
	public boolean deletePersonGroup(String personGroupId) {
		log.info("Deleting person group {}", personGroupId);
		return cognitiveServicesClient.deletePersonGroup(personGroupId);
	}

	@Override
	public Optional<PersonGroup> getPersonGroup(String personGroupId) {
		log.info("Reading person group {}", personGroupId);
		return cognitiveServicesClient.getPersonGroup(personGroupId);
	}

	@Override
	public boolean trainPersonGroup(String personGroupId) {
		log.info("Training person group {}", personGroupId);
		return cognitiveServicesClient.trainPersonGroup(personGroupId);
	}

	@Override
	public Optional<String> createPerson(String personGroupId, String personName, String userData) {
		log.info("Creating person {},{} in group {}", personName, userData, personGroupId);
		return cognitiveServicesClient.createPerson(personGroupId, personName, userData);
	}

	@Override
	public Optional<String> addPersonFace(String personGroupId, String personId,
		@Nullable
			FaceRectangle faceRectangle, byte[] imageBytes) {
		log.info("Adding person face for person {} group {}", personId, personGroupId);
		return cognitiveServicesClient.addPersonFace(personGroupId, personId, faceRectangle, imageBytes);
	}

	@Override
	public List<FaceIdentificationResult> identify(String personGroupId, List<String> faceIds, int maxCandidates,
		double confidenceThreshold) {
		log.info("Identifying person in person group {} for each faceId in {}", personGroupId, faceIds);
		return cognitiveServicesClient.identify(personGroupId, faceIds, maxCandidates, confidenceThreshold);
	}

	public Map<String, String> getFaceIdsCacheMap() {
		return ImmutableMap.copyOf(faceIdsCacheMap);
	}
}
