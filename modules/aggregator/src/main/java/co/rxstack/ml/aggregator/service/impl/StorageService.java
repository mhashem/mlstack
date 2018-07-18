package co.rxstack.ml.aggregator.service.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import co.rxstack.ml.aggregator.config.FaceDBConfig;
import co.rxstack.ml.aggregator.service.IStorageService;
import co.rxstack.ml.aggregator.service.StorageStrategy;
import co.rxstack.ml.aws.rekognition.service.ICloudStorageService;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class StorageService implements IStorageService {

	public static final String TEMPORARY_FOLDER = "preprocessed";

	private static final Logger log = LoggerFactory.getLogger(StorageService.class);

	private FaceDBConfig faceDBConfig;
	private ICloudStorageService cloudStorageService;

	public StorageService(FaceDBConfig faceDBConfig, ICloudStorageService cloudStorageService) {
		this.faceDBConfig = faceDBConfig;
		this.cloudStorageService = cloudStorageService;
	}

	@Override
	public boolean saveTemporary(String filename, byte[] fileBytes, StorageStrategy.Strategy strategy) {
		return saveFile(filename, TEMPORARY_FOLDER, fileBytes, strategy);
	}

	@Override
	public boolean saveFile(String filename, String folder, byte[] fileBytes, StorageStrategy.Strategy strategy) {
		switch (strategy) {
		case DISK:
			return this.saveToDisk(filename, folder, fileBytes);
		case S3_BUCKET:
			cloudStorageService.uploadImage(filename, new ByteArrayInputStream(fileBytes), ImmutableMap.of());
			return true;
		}
		throw new IllegalArgumentException("No persisting strategy specified!");
	}

	@Override
	public byte[] readBytes(String fileName, String folder, StorageStrategy.Strategy strategy) throws IOException {
		switch (strategy) {
		case DISK:
			return IOUtils.toByteArray(java.nio.file.Files.newInputStream(
				Paths.get(faceDBConfig.getFaceDbPath() + File.separator + folder + File.separator + fileName)));
		}
		return new byte[]{};
	}

	private boolean saveToDisk(String fileName, String folder, byte[] fileBytes) {
		log.info("Saving file {} to disk path [{}] with bytes [{}]", fileName, folder,
			fileBytes.length);
		try {
			String storagePath = faceDBConfig.getFaceDbPath() + File.separator + folder;
			File directory = new File(storagePath);
			if (!directory.exists()) {
				if (!directory.mkdir()) {
					log.error("Couldn't create saving directory! {}", directory.getAbsolutePath());
					return false;
				}
			}
			String builder = directory.getAbsolutePath() + File.separator + fileName;
			Files.write(fileBytes, new File(builder));
			return true;
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		return false;
	}

}
