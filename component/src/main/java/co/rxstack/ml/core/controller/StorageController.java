package co.rxstack.ml.core.controller;

import java.io.FileNotFoundException;
import java.io.IOException;

import co.rxstack.ml.aws.rekognition.service.ICloudStorageService;
import co.rxstack.ml.aws.rekognition.service.impl.CloudStorageService;

import co.rxstack.ml.tensorflow.service.IFaceNetService;
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
public class StorageController {

	private static final Logger log = LoggerFactory.getLogger(CloudStorageService.class);

	private final ICloudStorageService cloudStorageService;
	private final IFaceNetService faceNetService;
	
	@Autowired
	public StorageController(ICloudStorageService cloudStorageService, IFaceNetService faceNetService) {
		this.cloudStorageService = cloudStorageService;
		this.faceNetService = faceNetService;
	}

	@PostMapping("/api/v1/storage/image")
	public ResponseEntity uploadImage(
		@RequestParam("cloudIndexIdentifier")
			String cloudIndexIdentifier,
		@RequestParam("imageFile")
			MultipartFile imageFile) {
		log.info("Intercepted upload image request");
		try {
			cloudStorageService.uploadImage(cloudIndexIdentifier, imageFile.getInputStream());
			return ResponseEntity.ok(ImmutableMap.of("message", "uploaded successfully"));
		} catch (IOException e) {
			log.error(e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ImmutableMap.of("message", "failed to process image upload request"));
		}
	}

	@PostMapping("/api/v1/storage/embeddings")
	public ResponseEntity saveEmbeddings() {
		try {
			faceNetService.saveEmbeddings();
			return ResponseEntity.ok(ImmutableMap.of("saving", true));
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(ImmutableMap.of("error", e.getMessage()));
		}
	}

}
