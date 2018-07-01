package co.rxstack.ml.core.controller;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import co.rxstack.ml.aggregator.service.IIdentityService;
import co.rxstack.ml.aggregator.service.impl.AggregatorService;
import co.rxstack.ml.common.model.AggregateFaceIdentification;
import co.rxstack.ml.common.model.Constants;
import co.rxstack.ml.common.model.Ticket;
import co.rxstack.ml.core.jobs.IndexingQueue;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
	private AggregatorService aggregatorService;
	private IIdentityService identityService;

	@Autowired
	public FaceController(AggregatorService aggregatorService, IndexingQueue indexingQueue,
		IIdentityService identityService) {
		this.aggregatorService = aggregatorService;
		this.indexingQueue = indexingQueue;
		this.identityService = identityService;
	}

	@GetMapping("/api/v1/faces/{identityId}")
	public ResponseEntity getFacesForIdentity(@PathVariable("identityId") int identityId) {
		return ResponseEntity.ok(ImmutableMap.of("faces", identityService.findFaceListByIdentityId(identityId)));
	}

	@PostMapping("/api/v1/faces/{identityId}/index")
	public ResponseEntity indexFace(
		@PathVariable("identityId")
			String identityId,
		@RequestParam("personName")
			String personName,
		@RequestParam("faceImage")
			MultipartFile faceImage) {
		log.info("Intercepted: index face request");

		Preconditions.checkNotNull(identityId);
		Preconditions.checkNotNull(faceImage);

		try {
			byte[] bytes = faceImage.getBytes();
			Ticket ticket = new Ticket(UUID.randomUUID().toString());
			ticket.setType(Ticket.Type.INDEXING);
			ticket.setPersonId(identityId);
			ticket.setImageBytes(bytes);
			ticket.setPersonName(personName);

			indexingQueue.push(ticket);
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
			AggregateFaceIdentification faceIdentification =
				aggregatorService.identify(targetImage.getBytes(),
					ImmutableMap.of(Constants.CONTENT_TYPE, targetImage.getContentType()));
			return ResponseEntity.ok(faceIdentification);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
			.body(ImmutableMap.of("message", "no matching face(s) found"));
	}

	@PostMapping("/api/v2/faces/recognition")
	public ResponseEntity recognize(
		@RequestParam("image")
			MultipartFile image, HttpServletRequest request) {
		log.info("Intercepted request for image recognition from {}", request.getRemoteAddr());

		Preconditions.checkNotNull(image);
		try {
			return ResponseEntity.ok(aggregatorService.recognize(image.getBytes()));
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return ResponseEntity.badRequest().build();
	}

	@PostMapping("/api/v3/faces/recognition")
	public ResponseEntity recognize3(
		@RequestParam("image")
			MultipartFile image, HttpServletRequest request) throws IOException {
		log.info("Intercepted request for image recognition (v3) from {}", request.getRemoteAddr());

		Preconditions.checkNotNull(image);
		try {
			return ResponseEntity.ok(aggregatorService.faceNetRecognize(image.getBytes()));
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return ResponseEntity.badRequest().build();
	}

}
