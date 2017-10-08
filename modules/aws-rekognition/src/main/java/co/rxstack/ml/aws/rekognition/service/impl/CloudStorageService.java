package co.rxstack.ml.aws.rekognition.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import co.rxstack.ml.aws.rekognition.service.ICloudStorageService;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author mhachem on 9/30/2017.
 */
@Service
public class CloudStorageService implements ICloudStorageService {

	private static final Logger log = LoggerFactory.getLogger(CloudStorageService.class);

	private String folder;
	private String bucketName;
	private AmazonS3 amazonS3;

	@Autowired
	public CloudStorageService(String awsRegion, String bucketName, String folder,
		AWSStaticCredentialsProvider awsStaticCredentialsProvider) {
		this.bucketName = bucketName;
		this.folder = folder;
		amazonS3 = AmazonS3ClientBuilder.standard().withRegion(Regions.fromName(awsRegion))
			.withCredentials(awsStaticCredentialsProvider).build();
	}

	@Override
	public void uploadPersonFaceImage(String personName, InputStream inputStream) {
		uploadImage(personName + ".jpg", inputStream, ImmutableMap.of("FullName", personName));
	}

	@Override
	public void uploadImage(String uploadedFileName, InputStream inputStream,
		Map<String, String> metaDataMap) {
		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setUserMetadata(metaDataMap);
		PutObjectRequest putRequest =
			new PutObjectRequest(bucketName, folder + "/" + uploadedFileName, inputStream, objectMetadata);
		amazonS3.putObject(putRequest);
	}

	@Override
	public byte[] getObjectAsByteArray(String fileName) throws IOException {
		log.info("Getting S3Object with name {}", fileName);
		try {
			S3Object s3Object = amazonS3.getObject(bucketName, folder + "/" + fileName);
			return IOUtils.toByteArray(s3Object.getObjectContent());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		}
	}

}
