package co.rxstack.ml.aws.rekognition.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.stream.Collectors;

import co.rxstack.ml.aws.rekognition.service.ICloudStorageService;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
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
	private AmazonDynamoDB amazonDynamoDB;

	@Autowired
	public CloudStorageService(String awsRegion, String bucketName, String folder,
		AWSStaticCredentialsProvider awsStaticCredentialsProvider) {
		this.bucketName = bucketName;
		this.folder = folder;
		amazonS3 = AmazonS3ClientBuilder.standard().withRegion(Regions.fromName(awsRegion))
			.withCredentials(awsStaticCredentialsProvider).build();
		amazonDynamoDB =
			AmazonDynamoDBClientBuilder.standard().withRegion(awsRegion).withCredentials(awsStaticCredentialsProvider)
				.build();
	}

	@Override
	public void uploadPersonFaceImage(String cloudIndexIdentifier, InputStream inputStream) {
		log.info("Uploading person face image {}", cloudIndexIdentifier);
		// todo change FullName in lambda
		uploadImage(cloudIndexIdentifier + ".jpg", inputStream, ImmutableMap.of("FullName", cloudIndexIdentifier));
	}

	@Override
	public void uploadImage(String uploadedFileName, InputStream inputStream,
		Map<String, String> metaDataMap) {
		log.info("Uploading image to S3 Bucket {}", bucketName);
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

	@Override
	public void deleteObject(String fileName) {
		log.warn("Deleting S3Object with name {}", fileName);
		amazonS3.deleteObject(bucketName, folder + "/" + fileName);
	}

	@Override
	public Map<String, String> getCloudIndexFaceIds(String tableName) {
		ScanRequest scanRequest = new ScanRequest().withTableName(tableName);
		ScanResult result = amazonDynamoDB.scan(scanRequest);
		return result.getItems().stream()
			.collect(Collectors.toMap(s -> s.get("RekognitionId").getS(), s -> s.get("FullName").getS()));
	}
}
