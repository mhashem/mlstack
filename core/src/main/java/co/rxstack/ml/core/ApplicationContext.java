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
import co.rxstack.ml.cognitiveservices.service.ICognitiveService;
import co.rxstack.ml.cognitiveservices.service.impl.CognitiveService;
import co.rxstack.ml.core.properties.AwsProperties;
import co.rxstack.ml.core.properties.CognitiveServicesProperties;
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

	@Bean
	public AwsProperties awsProperties() {
		return new AwsProperties();
	}
	
	@Bean
	public CognitiveServicesProperties cognitiveServicesProperties() {
		return new CognitiveServicesProperties();
	}
	
	@Bean
	public URI cognitiveServicesUri(CognitiveServicesProperties cognitiveServicesProperties) {
		return URI.create(cognitiveServicesProperties.getServiceUrl());
	}

	@Bean
	public ICognitiveServicesClient cognitiveServicesClient(CognitiveServicesProperties cognitiveServicesProperties,
		URI cognitiveServicesUri) {
		return new CognitiveServicesClient(cognitiveServicesUri, cognitiveServicesProperties.getSubscriptionKey());
	}

	@Bean
	public AWSStaticCredentialsProvider awsStaticCredentialsProvider(AwsProperties awsProperties) {
		return new AWSStaticCredentialsProvider(
			new BasicAWSCredentials(awsProperties.getAccessKey(), awsProperties.getSecretKey()));
	}

	@Bean
	public IRekognitionClient rekognitionClient(AWSStaticCredentialsProvider awsStaticCredentialsProvider,
		AwsProperties awsProperties) {
		return new RekognitionClient(awsProperties.getRegion(), awsStaticCredentialsProvider);
	}

	@Bean
	public ICognitiveService cognitiveService(ICognitiveServicesClient cognitiveServicesClient) {
		return new CognitiveService(cognitiveServicesClient);
	}

	@Bean
	public IRekognitionService rekognitionService(IRekognitionClient rekognitionClient) {
		return new RekognitionService(rekognitionClient);
	}

	@Bean
	public ICloudStorageService cloudStorageService(AWSStaticCredentialsProvider awsStaticCredentialsProvider,
		AwsProperties awsProperties) {
		return new CloudStorageService(awsProperties.getRegion(), awsProperties.getS3().getBucket(),
			awsProperties.getS3().getBucketFolder(), awsStaticCredentialsProvider);
	}

	@Bean
	public ResultAggregatorService resultAggregatorService(IRekognitionService rekognitionService,
		ICognitiveService cognitiveService) {
		return new ResultAggregatorService(rekognitionService, cognitiveService);
	}
	
}
