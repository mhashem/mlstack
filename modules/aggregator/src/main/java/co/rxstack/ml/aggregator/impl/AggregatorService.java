package co.rxstack.ml.aggregator.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import co.rxstack.ml.aggregator.IFaceExtractorService;
import co.rxstack.ml.aws.rekognition.model.FaceIndexingResult;
import co.rxstack.ml.aws.rekognition.service.IRekognitionService;
import co.rxstack.ml.cognitiveservices.model.CognitiveIndexingResult;
import co.rxstack.ml.cognitiveservices.service.ICognitiveService;
import co.rxstack.ml.common.model.AggregateFaceIdentification;
import co.rxstack.ml.common.model.AggregateFaceIndexingResult;
import co.rxstack.ml.common.model.Candidate;
import co.rxstack.ml.common.model.FaceIdentificationResult;

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
		log.info("indexFaces called with image {} bytes and bundleMap {}", imageBytes.length, bundleMap);
		CompletableFuture<Optional<FaceIndexingResult>> awsCompletableFuture =
			CompletableFuture.supplyAsync(() -> rekognitionService.indexFace(imageBytes, bundleMap));
		CompletableFuture<Optional<CognitiveIndexingResult>> cognitiveCompletableFuture =
			CompletableFuture.supplyAsync(() -> cognitiveService.indexFace(imageBytes, bundleMap));

		try {
			CompletableFuture.allOf(awsCompletableFuture, cognitiveCompletableFuture).get();

			if (awsCompletableFuture.isDone() && cognitiveCompletableFuture.isDone()) {
				AggregateFaceIndexingResult result = new AggregateFaceIndexingResult();
				Optional<CognitiveIndexingResult> cognitiveIndexingResult = cognitiveCompletableFuture.get();
				Optional<FaceIndexingResult> faceIndexingResult = awsCompletableFuture.get();
				faceIndexingResult
					.ifPresent(faceIndexingResult1 -> result.awsFaceId = faceIndexingResult1.getFace().getFaceId());
				cognitiveIndexingResult.ifPresent(
					cognitiveIndexingResult1 -> result.cognitivePersonId = cognitiveIndexingResult1.getPersonId());
				return ImmutableList.of(result);
			}

		} catch (InterruptedException | ExecutionException e) {
			log.error(e.getMessage(), e);
		}
		return ImmutableList.of();
	}

	public Optional<AggregateFaceIdentification> identify(byte[] imageBytes) {
		log.info("identify called with image {} bytes", imageBytes.length);
		try {
			Optional<byte[]> extractFace = faceExtractorService.extractFace(imageBytes);
			if (!extractFace.isPresent()) {
				log.warn("No face detected in image!");
				return Optional.empty();
			}

			// todo add co.rxstack.ml.aggregator.impl.FaceRecognitionService OpenCV

			CompletableFuture<Optional<Candidate>> awsCompletableFuture = CompletableFuture.supplyAsync(() -> {
				log.info("AWS Rekognition ---> async searching faces by image");
				return rekognitionService.searchFaceByImage(imageBytes);
			});

			CompletableFuture<Optional<FaceIdentificationResult>> cognitiveCompletableFuture =
				CompletableFuture.supplyAsync(() -> {
					log.info("Cognitive ---> async searching faces by image");
					return cognitiveService.identifyFace(imageBytes);
				});

			CompletableFuture.allOf(awsCompletableFuture, cognitiveCompletableFuture).get();

			if (awsCompletableFuture.isDone() && cognitiveCompletableFuture.isDone()) {
				AggregateFaceIdentification faceIdentification = new AggregateFaceIdentification();

				Optional<Candidate> candidate = awsCompletableFuture.get();
				candidate.ifPresent((c) -> {
					log.info("AWS Candidate best match {}", c);
					faceIdentification.awsFaceId = c.getPersonId();
				});

				Optional<FaceIdentificationResult> faceIdentificationResult = cognitiveCompletableFuture.get();
				if (faceIdentificationResult.isPresent()) {
					Optional<Candidate> candidateBestMatch = faceIdentificationResult.get().getCandidateBestMatch();
					if (candidateBestMatch.isPresent()) {
						log.info("Candidate best match {}", candidateBestMatch.get());
						faceIdentification.cognitivePersonId = candidateBestMatch.get().getPersonId();
					}
				}
				return Optional.of(faceIdentification);
			}

		} catch (IOException | InterruptedException | ExecutionException e) {
			log.error(e.getMessage(), e);
		}
		return Optional.empty();
	}

}
