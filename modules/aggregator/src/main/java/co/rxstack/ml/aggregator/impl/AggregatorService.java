package co.rxstack.ml.aggregator.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import co.rxstack.ml.aggregator.IFaceExtractorService;
import co.rxstack.ml.aws.rekognition.model.FaceIndexingResult;
import co.rxstack.ml.aws.rekognition.service.IRekognitionService;
import co.rxstack.ml.cognitiveservices.model.CognitiveIndexingResult;
import co.rxstack.ml.cognitiveservices.service.ICognitiveService;
import co.rxstack.ml.common.model.AggregateFaceIndexingResult;
import co.rxstack.ml.common.model.FaceDetectionResult;
import co.rxstack.ml.common.model.Person;

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
public class AggregatorService {

	private static final Logger log = LoggerFactory.getLogger(FaceExtractorService.class);

	private IFaceExtractorService faceExtractorService;
	private ICognitiveService cognitiveService;
	private IRekognitionService rekognitionService;

	@Autowired
	public AggregatorService(IFaceExtractorService faceExtractorService, IRekognitionService rekognitionService,
		ICognitiveService cognitiveService) {
		Preconditions.checkNotNull(faceExtractorService);
		Preconditions.checkNotNull(rekognitionService);
		Preconditions.checkNotNull(cognitiveService);
		this.faceExtractorService = faceExtractorService;
		this.rekognitionService = rekognitionService;
		this.cognitiveService = cognitiveService;
	}

	public List<AggregateFaceIndexingResult> indexFaces(byte[] imageBytes, Map<String, String> bundleMap) {

		Optional<FaceIndexingResult> faceIndexingResult = rekognitionService.indexFace(imageBytes, bundleMap);
		Optional<CognitiveIndexingResult> cognitiveIndexingResult = cognitiveService.indexFace(imageBytes, bundleMap);

		AggregateFaceIndexingResult result = new AggregateFaceIndexingResult();

		faceIndexingResult
			.ifPresent(faceIndexingResult1 -> result.awsFaceId = faceIndexingResult1.getFace().getFaceId());
		cognitiveIndexingResult
			.ifPresent(cognitiveIndexingResult1 -> result.cognitivePersonId = cognitiveIndexingResult1.getPersonId());
		
		return ImmutableList.of(result);
	}

	/*public Optional<FaceDetectionResult> detectFaceIdentity(byte[] imageBytes) {
		try {
			List<byte[]> detectedFaces = faceExtractorService.detectFaces(imageBytes);
			List<List<FaceDetectionResult>> lists =
				detectedFaces.stream().map(bytes -> aggregateResult(imageBytes)).collect(Collectors.toList());
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return Optional.empty();
	}*/

	// todo more tests!
	public Optional<String> saveAndIndexImages(String personName, byte[] imageBytes) {
		String personGroupId = "employee_collection";
		cognitiveService.createPersonGroup(personGroupId, "Employee Collection");
		Optional<Person> personOptional =
			cognitiveService.createPerson(personGroupId, personName, "Test Prediction Result");
		if (personOptional.isPresent()) {
			for (FaceDetectionResult faceDetectionResult : cognitiveService.detect(imageBytes)) {
				String personId = personOptional.get().getPersonId();
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

	/*private List<FaceDetectionResult> aggregateResult(byte[] imageBytes) {
		rekognitionService
			.searchFacesByImage("employee_collection", imageBytes, 1)
			.stream().filter(candidate -> candidate.getConfidence() > 75.0f);
		*//*cognitiveService.identify()*//*
		return ImmutableList.of();
	}*/
}
