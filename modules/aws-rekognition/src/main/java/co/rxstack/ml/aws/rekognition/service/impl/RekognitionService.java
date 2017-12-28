package co.rxstack.ml.aws.rekognition.service.impl;

import java.util.List;
import java.util.Optional;

import co.rxstack.ml.aws.rekognition.service.IRekognitionService;
import co.rxstack.ml.client.aws.IRekognitionClient;
import co.rxstack.ml.common.model.Candidate;
import co.rxstack.ml.common.model.ComparisonResult;
import co.rxstack.ml.common.model.FaceDetectionResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author mhachem on 9/28/2017.
 */
@Service
public class RekognitionService implements IRekognitionService {

	private static final Logger log = LoggerFactory.getLogger(RekognitionService.class);

	private IRekognitionClient rekognitionClient;

	@Autowired
	public RekognitionService(IRekognitionClient rekognitionClient) {
		this.rekognitionClient = rekognitionClient;
	}

	@Override
	public Optional<ComparisonResult> compareFaces(byte[] faceOneImageBytes, byte[] faceTwoImageBytes) {
		return rekognitionClient.compareFaces(faceOneImageBytes, faceTwoImageBytes);
	}

	@Override
	public List<FaceDetectionResult> detect(byte[] imageBytes) {
		log.info("Detecting faces in image");
		return rekognitionClient.detect(imageBytes);
	}

	@Override
	public Optional<String> indexFace(byte[] imageBytes) {
		return Optional.empty();
	}

	@Override
	public List<Candidate> searchFacesByImage(String collectionId, byte[] imageBytes, int maxFaces) {
		log.info("Searching faces {} by image [maxFaces: {}]", collectionId, maxFaces);
		return rekognitionClient.searchFacesByImage(collectionId, imageBytes, maxFaces);
	}

}
