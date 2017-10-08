package co.rxstack.ml.core;

import java.net.URI;

import co.rxstack.ml.aggregator.ResultAggregatorService;
import co.rxstack.ml.aws.rekognition.service.ICloudStorageService;
import co.rxstack.ml.aws.rekognition.service.IRekognitionService;
import co.rxstack.ml.aws.rekognition.service.impl.CloudStorageService;
import co.rxstack.ml.aws.rekognition.service.impl.RekognitionService;
import co.rxstack.ml.client.aws.IRekognitionClient;
import co.rxstack.ml.client.aws.impl.RekognitionClient;
import co.rxstack.ml.client.cognitiveservices.ICognitiveServicesClient;
import co.rxstack.ml.client.cognitiveservices.impl.CognitiveServicesClient;
import co.rxstack.ml.cognitiveservices.service.IFaceDetectionService;
import co.rxstack.ml.cognitiveservices.service.IPersonGroupService;
import co.rxstack.ml.cognitiveservices.service.IPersonService;
import co.rxstack.ml.cognitiveservices.service.impl.FaceDetectionService;
import co.rxstack.ml.cognitiveservices.service.impl.PersonGroupService;
import co.rxstack.ml.cognitiveservices.service.impl.PersonService;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author mhachem on 9/27/2017.
 */
@Configuration
public class ApplicationContext {

	@Value("${subscription-key}")
	private String subscriptionKey;
	@Value("${cognitive-service-url}")
	private String cognitiveServiceUrl;
	@Value("${aws-region}")
	private String awsRegion;
	@Value("${aws-access-key}")
	private String awsAccessKey;
	@Value("${aws-secret-key}")
	private String awsSecretKey;
	@Value("${aws-s3-bucket}")
	private String awsS3Bucket;
	@Value("${aws-s3-bucket-folder}")
	private String awsS3BucketFolder;

	@Bean
	public URI cognitiveServicesUri() {
		return URI.create(cognitiveServiceUrl);
	}

	@Bean
	public ICognitiveServicesClient cognitiveServicesClient(URI cognitiveServicesUri) {
		return new CognitiveServicesClient(cognitiveServicesUri, subscriptionKey);
	}

	@Bean
	public AWSStaticCredentialsProvider awsStaticCredentialsProvider() {
		return new AWSStaticCredentialsProvider(new BasicAWSCredentials(awsAccessKey, awsSecretKey));
	}

	@Bean
	public IRekognitionClient rekognitionClient(AWSStaticCredentialsProvider awsStaticCredentialsProvider) {
		return new RekognitionClient(awsRegion, awsStaticCredentialsProvider);
	}
	
	@Bean
	public IPersonGroupService personGroupService(ICognitiveServicesClient cognitiveServicesClient) {
		return new PersonGroupService(cognitiveServicesClient);
	}
	
	@Bean
	public IPersonService personService(ICognitiveServicesClient cognitiveServicesClient) {
		return new PersonService(cognitiveServicesClient);
	}
	
	@Bean
	public IFaceDetectionService faceDetectionService(ICognitiveServicesClient cognitiveServicesClient) {
		return new FaceDetectionService(cognitiveServicesClient);
	}

	@Bean
	public IRekognitionService rekognitionService(IRekognitionClient rekognitionClient) {
		return new RekognitionService(rekognitionClient);
	}

	@Bean
	public ICloudStorageService cloudStorageService(AWSStaticCredentialsProvider awsStaticCredentialsProvider) {
		return new CloudStorageService(awsRegion, awsS3Bucket, awsS3BucketFolder, awsStaticCredentialsProvider);
	}

	@Bean
	public ResultAggregatorService resultAggregatorService(IRekognitionService rekognitionService,
		IFaceDetectionService faceDetectionService, IPersonService personService) {
		return new ResultAggregatorService(rekognitionService, faceDetectionService, personService);
	}
	
}
