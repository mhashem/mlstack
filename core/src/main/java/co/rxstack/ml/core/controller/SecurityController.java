package co.rxstack.ml.core.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import co.rxstack.ml.aws.rekognition.service.IRekognitionService;
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
public class SecurityController {

	private static final Logger log = LoggerFactory.getLogger(SecurityController.class);

	private final IRekognitionService rekognitionService;

	@Autowired
	public SecurityController(IRekognitionService rekognitionService) {
		this.rekognitionService = rekognitionService;
	}

	@PostMapping("/api/v1/security/recognition")
	public ResponseEntity<?> searchSimilar(
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
