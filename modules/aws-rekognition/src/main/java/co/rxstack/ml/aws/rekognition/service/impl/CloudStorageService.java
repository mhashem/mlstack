package co.rxstack.ml.aws.rekognition.service.impl;

import java.io.File;
import java.util.Map;

import co.rxstack.ml.aws.rekognition.service.ICloudStorageService;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author mhachem on 9/30/2017.
 */
@Service
public class CloudStorageService implements ICloudStorageService {

	private AmazonS3 amazonS3;

	@Autowired
	public CloudStorageService(AWSStaticCredentialsProvider awsStaticCredentialsProvider) {
		amazonS3 = AmazonS3ClientBuilder.standard()
			.withRegion(Regions.EU_WEST_1)
			.withCredentials(awsStaticCredentialsProvider).build();
	}

	@Override
	public void uploadImage(String bucketName, String folderName, String uploadedFileName, File imageFile,
		Map<String, String> metaDataMap) {
		PutObjectRequest putRequest = new PutObjectRequest(bucketName, folderName + "/" + uploadedFileName, imageFile);
		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.setUserMetadata(metaDataMap);
		putRequest.withMetadata(objectMetadata);
		amazonS3.putObject(putRequest);
	}

}
