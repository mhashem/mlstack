package co.rxstack.ml.core.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.servlet.http.HttpServletRequest;

import co.rxstack.ml.aws.rekognition.service.IRekognitionService;
import co.rxstack.ml.cognitiveservices.service.ICognitiveService;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hashing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import sun.security.krb5.internal.Ticket;

/**
 * @author mhachem on 10/8/2017.
 */
@RestController
public class FaceController {

	private static final Logger log = LoggerFactory.getLogger(FaceController.class);

	private final ICognitiveService cognitiveService;
	private final IRekognitionService rekognitionService;

	@Autowired
	public FaceController(IRekognitionService rekognitionService, ICognitiveService cognitiveService) {
		this.rekognitionService = rekognitionService;
		this.cognitiveService = cognitiveService;
	}

	@PostMapping("/api/v1/faces/{personId}/index")
	public ResponseEntity indexFace(
		@PathVariable("personId")
			String personId,
		@RequestParam("faceImage")
			MultipartFile faceImage) {
		log.info("Intercepted: index face request");

		Preconditions.checkNotNull(personId);
		Preconditions.checkNotNull(faceImage);
		
		CompletableFuture.runAsync(() -> {
			try {
				byte[] bytes = faceImage.getBytes();
				log.info("image hash: {}", Hashing.sha256().hashBytes(bytes).toString());
				rekognitionService.indexFace(bytes, ImmutableMap.of("PERSON_ID", personId));
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		});
		
		return ResponseEntity.accepted().body(ImmutableMap.of("ticket", UUID.randomUUID().toString()));
	}

	@PostMapping("/api/v1/faces/recognition")
	public ResponseEntity searchSimilar(
		@RequestParam("targetImage")
			MultipartFile targetImage, HttpServletRequest request) {
		log.info("Intercepted request for image search from [{}]", request.getRemoteAddr());
		Preconditions.checkNotNull(targetImage);
		try {
			return ResponseEntity
				.ok(ImmutableMap.of("candidates", rekognitionService.searchFacesByImage(targetImage.getBytes())));
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(ImmutableMap.of("message", "no matching face(s) found"));
	}

}
