package co.rxstack.ml.core.controller;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import co.rxstack.ml.aggregator.service.IStorageService;
import co.rxstack.ml.aggregator.service.StorageStrategy;
import co.rxstack.ml.aggregator.service.impl.AggregatorService;
import co.rxstack.ml.common.model.Ticket;
import co.rxstack.ml.core.jobs.IndexingQueue;
import co.rxstack.ml.faces.service.IIdentityService;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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

	private IStorageService storageService;

	@Autowired
	public FaceController(AggregatorService aggregatorService, IndexingQueue indexingQueue,
		IIdentityService identityService, IStorageService storageService) {
		this.aggregatorService = aggregatorService;
		this.indexingQueue = indexingQueue;
		this.identityService = identityService;
		this.storageService = storageService;
	}

	/*@Cacheable(value = "faces.byId", key = "#identityId")*/
	@CrossOrigin(origins = "http://localhost:9000")
	@GetMapping("/api/v1/faces/{identityId}")
	public ResponseEntity getFacesForIdentity(@PathVariable("identityId") int identityId) {
		return ResponseEntity.ok(identityService.findFaceListByIdentityId(identityId));
	}

	@GetMapping("/api/v2/faces/{identityId}")
	public ResponseEntity getFacesForIdentityV2(@PathVariable("identityId") int identityId) {
		return ResponseEntity.ok(identityService.findFaceListByIdentityId(identityId));
	}

	@CrossOrigin(origins = "http://localhost:9000")
	@PostMapping("/api/v1/faces/{personId}/index")
	public ResponseEntity indexFace(
		@PathVariable("personId")
			String personId,
		@RequestParam(value = "personName", required = false)
			String personName,
		@RequestParam("faceImage") MultipartFile faceImage, HttpServletRequest request) {
		log.info("Intercepted: index face request");

		log.info("request {}", request.getHeaderNames().toString());

		Preconditions.checkNotNull(faceImage);

		try {
			String imageName = UUID.randomUUID().toString() + "." + faceImage.getContentType()
				.substring(faceImage.getContentType().indexOf('/') + 1);

			log.info("Image Content type: {}", faceImage.getContentType());

			boolean isSaved = storageService.saveTemporary(imageName,
				faceImage.getBytes(), StorageStrategy.Strategy.DISK);

			if (isSaved) {
				Ticket ticket = new Ticket(UUID.randomUUID().toString());
				ticket.setType(Ticket.Type.INDEXING);
				ticket.setPersonId(personId);
				ticket.setPersonName(personName);
				ticket.setImageName(imageName);
				indexingQueue.push(ticket);
				return ResponseEntity.accepted().body(ImmutableMap.of("ticket", ticket.getId()));
			} else {
				return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("failed to save");
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}

	@CrossOrigin(origins = "http://localhost:9000")
	@PostMapping("/api/v1/faces/recognition")
	public ResponseEntity searchSimilar(
		@RequestParam("image")
			MultipartFile image, HttpServletRequest request) {
		log.info("Intercepted request for image recognition from [{}]", request.getRemoteAddr());
		Preconditions.checkNotNull(image);
		try {
			aggregatorService.identify(image.getBytes());
			return ResponseEntity.accepted().build();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ImmutableMap.of("error", e.getMessage()));
		}
	}

	@PostMapping("/api/v0/faces/recognition")
	public ResponseEntity experimental(
		@RequestParam("image")
			MultipartFile image, HttpServletRequest request) {
		log.info("Intercepted request for image recognition from [{}]", request.getRemoteAddr());
		return searchSimilar(image, request);
	}

	@PostMapping("/api/v2/faces/recognition")
	public ResponseEntity recognize(
		@RequestParam("image")
			MultipartFile image, HttpServletRequest request) {
		log.info("Intercepted request for image recognition from {}", request.getRemoteAddr());

		Preconditions.checkNotNull(image);
		try {
			return ResponseEntity.ok(aggregatorService.inceptionRecognize(image.getBytes()));
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
