package co.rxstack.ml.core;

import java.io.IOException;
import java.time.LocalDateTime;

import co.rxstack.ml.aws.rekognition.service.ICloudStorageService;
import co.rxstack.ml.aws.rekognition.service.impl.CloudStorageService;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author mhachem on 10/8/2017.
 */
@RestController
public class CloudStorageController {

	private static final Logger log = LoggerFactory.getLogger(CloudStorageService.class);

	private final ICloudStorageService cloudStorageService;

	@Autowired
	public CloudStorageController(ICloudStorageService cloudStorageService) {
		this.cloudStorageService = cloudStorageService;
	}

	@GetMapping("/api/v1/storage/ping")
	public ResponseEntity<?> ping() {
		return ResponseEntity.ok(ImmutableMap.of("status", "working", "time", LocalDateTime.now()));
	}

	@PostMapping("/api/v1/storage/image")
	public ResponseEntity<?> uploadImage(
		@RequestParam("personName")
			String personName,
		@RequestParam("imageFile")
			MultipartFile imageFile) {
		log.info("intercepted upload image request");
		try {
			cloudStorageService.uploadPersonFaceImage(personName, imageFile.getInputStream());
			return ResponseEntity.ok(ImmutableMap.of("message", "uploaded successfully"));
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ImmutableMap.of("message", "failed to process image upload request"));
		}
	}

}
