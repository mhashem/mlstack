package co.rxstack.ml.core.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import co.rxstack.ml.aggregator.impl.AggregatorService;
import co.rxstack.ml.aws.rekognition.model.FaceIndexingResult;
import co.rxstack.ml.aws.rekognition.service.IRekognitionService;
import co.rxstack.ml.cognitiveservices.service.ICognitiveService;
import co.rxstack.ml.common.model.Candidate;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author mhachem on 10/8/2017.
 */
@RestController
public class FaceController {

	private static final Logger log = LoggerFactory.getLogger(FaceController.class);

	private final IRekognitionService rekognitionService;
	private final ICognitiveService cognitiveService;

	@Autowired
	public FaceController(IRekognitionService rekognitionService, ICognitiveService cognitiveService) {
		this.rekognitionService = rekognitionService;
		this.cognitiveService = cognitiveService;
	}

	@PostMapping("/api/v1/faces/indexing")
	public ResponseEntity indexFace(
		@RequestParam("collectionId")
			String collectionId,
		@RequestParam("faceImage")
			MultipartFile faceImage, HttpServletResponse response) {
		log.info("Intercepted: index face request");

		Preconditions.checkNotNull(collectionId);
		Preconditions.checkNotNull(faceImage);
		try {
			List<FaceIndexingResult> faceIndexingResults =
				rekognitionService.indexFaces(collectionId, faceImage.getBytes());
			// todo 
			// Use aggregation service here so it calls both AWS and Cognitive Services and 
			// returns back generated face ids
			
			return ResponseEntity.ok(ImmutableMap.of("indexing_results", faceIndexingResults));
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			// todo check what is best practice in such case!
			//return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("");
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(ImmutableMap.of("message", "no face(s) found to index!"));
	}

	@PostMapping("/api/v1/faces/recognition")
	public ResponseEntity searchSimilar(
		@RequestParam("collectionId")
			String collectionId,
		@RequestParam("targetImage")
			MultipartFile targetImage, HttpServletRequest request) {

		log.info("Intercepted request for image search in collection [{}] from [{}]", collectionId,
			request.getRemoteAddr());

		Preconditions.checkNotNull(collectionId);
		Preconditions.checkNotNull(targetImage);

		try {
			List<Candidate> candidateList =
				rekognitionService.searchFacesByImage(collectionId, targetImage.getBytes(), 1);
			return ResponseEntity.ok(ImmutableMap.of("candidates", candidateList));
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(ImmutableMap.of("message", "no matching face(s) found"));
	}

}
