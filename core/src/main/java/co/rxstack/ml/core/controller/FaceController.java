package co.rxstack.ml.core.controller;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import co.rxstack.ml.aws.rekognition.service.IRekognitionService;
import co.rxstack.ml.cognitiveservices.service.ICognitiveService;
import co.rxstack.ml.common.model.Ticket;
import co.rxstack.ml.core.jobs.IndexingQueue;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
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

/**
 * @author mhachem on 10/8/2017.
 */
@RestController
public class FaceController {

	private static final Logger log = LoggerFactory.getLogger(FaceController.class);

	private IndexingQueue indexingQueue;
	
	private final ICognitiveService cognitiveService;
	private final IRekognitionService rekognitionService;

	@Autowired
	public FaceController(IRekognitionService rekognitionService, ICognitiveService cognitiveService,
		IndexingQueue indexingQueue) {
		this.rekognitionService = rekognitionService;
		this.cognitiveService = cognitiveService;
		this.indexingQueue = indexingQueue;
	}

	@PostMapping("/api/v1/faces/{personId}/index")
	public ResponseEntity indexFace(
		@PathVariable("personId")
			String personId,
		@RequestParam("personName")
			String personName,
		@RequestParam("faceImage")
			MultipartFile faceImage) {
		log.info("Intercepted: index face request");

		Preconditions.checkNotNull(personId);
		Preconditions.checkNotNull(faceImage);
		// todo use something like phash to compute the hash of the image (media hash)
		// log.info("image hash: {}", Hashing.sha256().hashBytes(bytes).toString());

		try {
			byte[] bytes = faceImage.getBytes();
			Ticket ticket = new Ticket(UUID.randomUUID().toString());
			ticket.setType(Ticket.Type.INDEXING);
			ticket.setPersonId(personId);
			ticket.setImageBytes(bytes);
			ticket.setPersonName(personName);
			
			indexingQueue.push(ticket);
			/*rekognitionService.indexFace(bytes, ImmutableMap.of("PERSON_ID", personId));*/
			return ResponseEntity.accepted().body(ImmutableMap.of("ticket", ticket.getId()));
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
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
