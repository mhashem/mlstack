package co.rxstack.ml.aws.rekognition.service.impl;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import co.rxstack.ml.api.FaceIndexer;
import co.rxstack.ml.aws.rekognition.config.AwsConfig;
import co.rxstack.ml.aws.rekognition.mapper.FaceIndexingResultMapper;
import co.rxstack.ml.aws.rekognition.model.FaceIndexingResult;
import co.rxstack.ml.aws.rekognition.service.IRekognitionService;
import co.rxstack.ml.client.aws.IRekognitionClient;
import co.rxstack.ml.common.model.Candidate;
import co.rxstack.ml.common.model.ComparisonResult;
import co.rxstack.ml.common.model.FaceDetectionResult;

import com.amazonaws.services.rekognition.model.IndexFacesResult;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author mhachem on 9/28/2017.
 */
@Service
public class RekognitionService implements IRekognitionService, FaceIndexer<FaceIndexingResult> {

	private static final Logger log = LoggerFactory.getLogger(RekognitionService.class);

	private AwsConfig awsConfig;
	private IRekognitionClient rekognitionClient;

	@Autowired
	public RekognitionService(IRekognitionClient rekognitionClient, AwsConfig awsConfig) {
		this.rekognitionClient = rekognitionClient;
		this.awsConfig = awsConfig;
	}

	@Override
	public Optional<ComparisonResult> compareFaces(byte[] faceOneImageBytes, byte[] faceTwoImageBytes) {
		return rekognitionClient.compareFaces(faceOneImageBytes, faceTwoImageBytes);
	}

	@Override
	public List<FaceIndexingResult> indexFaces(byte[] imageBytes, Map<String, String> bundleMap) {
		log.info("Indexing Face for bundle {}, image with {} bytes => Collection {}", bundleMap, imageBytes.length,
			awsConfig.getCollectionId());
		return indexFaces(awsConfig.getCollectionId(), imageBytes);
	}

	@Override
	public Optional<FaceIndexingResult> indexFace(byte[] imageBytes, Map<String, String> bundleMap) {
		log.info("Indexing Face for bundle {}, image with {} bytes => Collection {}", bundleMap, imageBytes.length,
			awsConfig.getCollectionId());
		return indexFace(awsConfig.getCollectionId(), imageBytes);
	}
	
	@Override
	public List<FaceDetectionResult> detect(byte[] imageBytes) {
		log.info("Detecting faces in image with {} bytes", imageBytes.length);
		return rekognitionClient.detect(imageBytes);
	}

	@Override
	public Optional<Candidate> searchFaceByImage(byte[] imageBytes) {
		return searchFacesByImage(imageBytes).stream().findAny();
	}

	@Override
	public List<Candidate> searchFacesByImage(byte[] imageBytes) {
		log.info("Searching faces [collection: {}] by image with {} bytes [maxFaces: {}]", awsConfig.getCollectionId(),
			imageBytes.length, awsConfig.getMaxFaces());
		Stopwatch stopwatch = Stopwatch.createStarted();
		List<Candidate> candidates =
			rekognitionClient.searchFacesByImage(awsConfig.getCollectionId(), imageBytes, awsConfig.getMaxFaces());
		log.info("AWS searchFacesByImage found {} , in {}ms", candidates.size(), stopwatch.elapsed(MILLISECONDS));
		return candidates;
	}

	private Optional<FaceIndexingResult> indexFace(String collectionId, byte[] imageBytes) {
		return indexFaces(collectionId, imageBytes).stream().findFirst();
	}

	private List<FaceIndexingResult> indexFaces(String collectionId, byte[] imageBytes) {
		log.info("AWS indexing face(s) for image with {} bytes => Collection {}", imageBytes.length, collectionId);
		Stopwatch stopwatch = Stopwatch.createStarted();
		FaceIndexingResultMapper faceIndexingResultMapper = new FaceIndexingResultMapper();
		Optional<IndexFacesResult> indexFacesResultOptional = rekognitionClient.indexFace(collectionId, imageBytes);
		if (indexFacesResultOptional.isPresent()) {
			IndexFacesResult indexFacesResult = indexFacesResultOptional.get();
			log.info("Successfully indexed {} face(s) in {} ms", indexFacesResult.getFaceRecords().size(),
				stopwatch.elapsed(MILLISECONDS));
			return indexFacesResult.getFaceRecords().stream().map(faceIndexingResultMapper)
				.collect(Collectors.toList());
		}
		log.info("No Faces indexed returning an empty list!");
		return ImmutableList.of();
	}


}
