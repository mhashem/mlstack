package co.rxstack.ml.aggregator;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import co.rxstack.ml.aws.rekognition.service.IRekognitionService;
import co.rxstack.ml.cognitiveservices.service.ICognitiveService;
import co.rxstack.ml.common.model.FaceDetectionResult;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author mhachem on 9/29/2017.
 */
@Service
public class ResultAggregatorService {

	private static final Logger log = LoggerFactory.getLogger(ResultAggregatorService.class);

	private ICognitiveService cognitiveService;
	private IRekognitionService rekognitionService;

	@Autowired
	public ResultAggregatorService(IRekognitionService rekognitionService, ICognitiveService cognitiveService) {

		Preconditions.checkNotNull(rekognitionService);
		Preconditions.checkNotNull(cognitiveService);

		this.rekognitionService = rekognitionService;
		this.cognitiveService = cognitiveService;
	}

	// todo more tests!
	public Optional<String> saveAndIndexImages(String personName, byte[] imageBytes) {
		String personGroupId = "employee_collection";
		cognitiveService.createPersonGroup(personGroupId, "Employee Collection");
		Optional<String> idOptional = cognitiveService.createPerson(personGroupId, personName, "");
		if (idOptional.isPresent()) {
			for (FaceDetectionResult faceDetectionResult : cognitiveService.detect(imageBytes)) {
				String personId = idOptional.get();
				Optional<String> persistedFaceIdOptional = cognitiveService
					.addPersonFace(personGroupId, personId, faceDetectionResult.getFaceRectangle(), imageBytes);
				if (persistedFaceIdOptional.isPresent()) {
					// todo save to db
					String persistedFaceId = persistedFaceIdOptional.get();
					log.info("persistedFaceId {} added to person {}", persistedFaceId, personId);
					return Optional.of(persistedFaceId);
				} else {
					log.warn("Failed to add person face {} - {}", personName, personId);
				}
			}
		} else {
			log.warn("Failed to create person {} - group {}", personName, personGroupId);
		}
		return Optional.empty();
	}

	public List<FaceDetectionResult> aggregateResult() throws IOException {
		return ImmutableList.of();
	}
}
