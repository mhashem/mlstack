package co.rxstack.ml.aggregator.service.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import co.rxstack.ml.aggregator.config.FaceDBConfig;
import co.rxstack.ml.aggregator.service.IStorageService;
import co.rxstack.ml.aggregator.service.StorageStrategy;
import co.rxstack.ml.aws.rekognition.service.ICloudStorageService;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StorageService implements IStorageService {

	private static final Logger log = LoggerFactory.getLogger(StorageService.class);

	private FaceDBConfig faceDBConfig;
	private ICloudStorageService cloudStorageService;

	public StorageService(FaceDBConfig faceDBConfig, ICloudStorageService cloudStorageService) {
		this.faceDBConfig = faceDBConfig;
		this.cloudStorageService = cloudStorageService;
	}

	@Override
	public boolean saveFile(String fileName, String extension, String folder, byte[] fileBytes,
		StorageStrategy.Strategy strategy) {
		switch (strategy) {
		case DISK:
			return this.saveToDisk(fileName, folder, fileBytes, extension);
		case S3_BUCKET:
			cloudStorageService.uploadImage(fileName+ "." + extension,
				new ByteArrayInputStream(fileBytes), ImmutableMap.of());
			return true;
		}
		throw new IllegalArgumentException("No persisting strategy specified!");
	}

	private boolean saveToDisk(String fileName, String folder, byte[] fileBytes, String extension) {
		log.info("Saving file {} to disk path [{}] with bytes [{}] and type [{}]", fileName, folder,
			fileBytes.length, extension);
		try {
			String storagePath = faceDBConfig.getFaceDbPath() + File.separator + folder;
			File directory = new File(storagePath);
			if (!directory.exists()) {
				if (!directory.mkdir()) {
					log.error("Couldn't create saving directory! {}", directory.getAbsolutePath());
					return false;
				}
			}
			String builder = directory.getAbsolutePath() + File.separator + fileName + "." + extension;
			Files.write(fileBytes, new File(builder));
			return true;
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}

}
