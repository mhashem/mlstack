package co.rxstack.ml.cognitiveservices.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import co.rxstack.ml.client.cognitiveservices.ICognitiveServicesClient;
import co.rxstack.ml.cognitiveservices.config.CognitiveServicesConfig;
import co.rxstack.ml.cognitiveservices.model.CognitiveIndexingResult;
import co.rxstack.ml.cognitiveservices.service.ICognitiveService;
import co.rxstack.ml.common.model.Constants;
import co.rxstack.ml.common.model.FaceDetectionResult;
import co.rxstack.ml.common.model.FaceIdentificationResult;
import co.rxstack.ml.common.model.FaceRectangle;
import co.rxstack.ml.common.model.Person;
import co.rxstack.ml.common.model.PersonGroup;
import co.rxstack.ml.common.model.TrainingStatus;

import com.google.common.base.Preconditions;
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

	private ICognitiveServicesClient client;
	private CognitiveServicesConfig config;

	@Autowired
	public CognitiveService(ICognitiveServicesClient cognitiveServicesClient,
		CognitiveServicesConfig cognitiveServicesConfig) {
		Preconditions.checkNotNull(cognitiveServicesClient);
		Preconditions.checkNotNull(cognitiveServicesConfig);
		this.client = cognitiveServicesClient;
		this.config = cognitiveServicesConfig;
	}

	@Override
	public List<FaceDetectionResult> detect(byte[] imageBytes) {
		return client.detect(imageBytes);
	}

	@Override
	public boolean createPersonGroup(String personGroupId, String name) {
		log.info("Creating person group personGroupId {} , name {}", personGroupId, name);
		return client.createPersonGroup(personGroupId, name);
	}

	@Override
	public boolean deletePersonGroup(String personGroupId) {
		log.info("Deleting person group {}", personGroupId);
		return client.deletePersonGroup(personGroupId);
	}

	@Override
	public Optional<PersonGroup> getPersonGroup(String personGroupId) {
		log.info("Reading person group {}", personGroupId);
		return client.getPersonGroup(personGroupId);
	}

	@Override
	public boolean trainPersonGroup(String personGroupId) {
		log.info("Training person group {}", personGroupId);
		return client.trainPersonGroup(personGroupId);
	}

	@Override
	public Optional<TrainingStatus> getTrainingStatus(String personGroupId) {
		log.info("Get training status for person group {}", personGroupId);
		return client.getPersonGroupTrainingStatus(personGroupId);
	}

	@Override
	public Optional<Person> createPerson(String personGroupId, String personName, String userData) {
		log.info("Creating person {},{} in group {}", personName, userData, personGroupId);
		return client.createPerson(personGroupId, personName, userData);
	}

	@Override
	public Optional<Person> getPerson(String personGroupId, String personId) {
		return client.getPerson(personGroupId, personId);
	}

	@Override
	public Optional<String> addPersonFace(String personGroupId, String personId,
		@Nullable
			FaceRectangle faceRectangle, byte[] imageBytes) {
		log.info("Adding person face for person {} group {}", personId, personGroupId);
		return client.addPersonFace(personGroupId, personId, faceRectangle, imageBytes);
	}

	@Override
	public List<FaceIdentificationResult> identify(String personGroupId, List<String> faceIds, int maxCandidates,
		double confidenceThreshold) {
		log.info("Identifying person in group {} for each faceId in {}", personGroupId, faceIds);
		return client.identify(personGroupId, faceIds, maxCandidates, confidenceThreshold);
	}

	@Override
	public Optional<CognitiveIndexingResult> indexFace(byte[] imageBytes, Map<String, String> bundleMap) {

		String personGroupId = config.getPersonGroupId();
		Optional<PersonGroup> groupOptional = client.getPersonGroup(personGroupId);
		if (!groupOptional.isPresent()) {
			boolean r = client.createPersonGroup(personGroupId, config.getPersonGroupName());
			if (!r) {
				log.warn("Failed to create person group [{}, {}]", personGroupId, config.getPersonGroupName());
				return Optional.empty();
			}
		}

		Person person = null;
		String personId = bundleMap.get(Constants.PERSON_ID);
		String personName = bundleMap.get(Constants.PERSON_NAME);
		String userData = bundleMap.get(Constants.USER_DATA);

		Optional<Person> personOptional = Optional.empty();
		if (personId != null) {
			// get person!
			personOptional = client.getPerson(personGroupId, personId);
		}
		if (!personOptional.isPresent()) {
			personOptional = client.createPerson(personGroupId, personName, userData);
			if (!personOptional.isPresent()) {
				log.warn("Failed to create person {} at group {}", personName, personGroupId);
				return Optional.empty();
			}
		}

		person = personOptional.get();
		List<FaceDetectionResult> faceDetectionResults = client.detect(imageBytes);
		if (!faceDetectionResults.isEmpty()) {
			FaceDetectionResult faceDetectionResult = faceDetectionResults.get(0);
			client
				.addPersonFace(personGroupId, person.getPersonId(), faceDetectionResult.getFaceRectangle(), imageBytes);

			CognitiveIndexingResult result = new CognitiveIndexingResult();
			result.setPersonId(person.getPersonId());
			return Optional.of(result);
		}

		return Optional.empty();
	}

	@Override
	public List<CognitiveIndexingResult> indexFaces(byte[] imageBytes, Map<String, String> bundleMap) {
		return null;
	}
}
