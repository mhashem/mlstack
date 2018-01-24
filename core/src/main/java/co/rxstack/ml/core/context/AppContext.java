package co.rxstack.ml.core.context;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import co.rxstack.ml.aggregator.IFaceExtractorService;
import co.rxstack.ml.aggregator.IFaceRecognitionService;
import co.rxstack.ml.aggregator.config.FaceDBConfig;
import co.rxstack.ml.aggregator.impl.AggregatorService;
import co.rxstack.ml.aggregator.impl.FaceExtractorService;
import co.rxstack.ml.aggregator.impl.FaceRecognitionService;
import co.rxstack.ml.aws.rekognition.config.AwsConfig;
import co.rxstack.ml.aws.rekognition.service.ICloudStorageService;
import co.rxstack.ml.aws.rekognition.service.IRekognitionService;
import co.rxstack.ml.aws.rekognition.service.impl.CloudStorageService;
import co.rxstack.ml.aws.rekognition.service.impl.RekognitionService;
import co.rxstack.ml.client.IStackClient;
import co.rxstack.ml.client.StackClient;
import co.rxstack.ml.client.aws.IRekognitionClient;
import co.rxstack.ml.client.aws.impl.RekognitionClient;
import co.rxstack.ml.client.cognitiveservices.ICognitiveServicesClient;
import co.rxstack.ml.client.cognitiveservices.impl.CognitiveServicesClient;
import co.rxstack.ml.cognitiveservices.config.CognitiveServicesConfig;
import co.rxstack.ml.cognitiveservices.service.ICognitiveService;
import co.rxstack.ml.cognitiveservices.service.impl.CognitiveService;
import co.rxstack.ml.core.config.AwsProperties;
import co.rxstack.ml.core.config.CognitiveServicesProperties;
import co.rxstack.ml.core.factory.AuthRequestInterceptor;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.google.common.collect.ImmutableList;
import nu.pattern.OpenCV;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * @author mhachem on 9/27/2017.
 */
@Configuration
public class AppContext {

	@Value("${client.service.name}")
	private String clientServiceName;

	@Value("${client.endpoint}")
	private String clientEndpoint;
	@Value("${client.user.username}")
	private String clientEndpointUsername;
	@Value("${client.user.password}")
	private String clientEndpointPassword;

	@Value("${face.db.path}")
	private String faceDBPath;
	@Value("${face.directory.name.delimiter}")
	private String faceDirectoryNameDelimiter;
	@Value("${model.storage.path}")
	private String modelStoragePath;

	@Value("${face.standard.width}")
	private int standardWidth;
	@Value("${face.standard.height}")
	private int standardHeight;


	@Qualifier("stackClientRestTemplate")
	@Bean
	public RestTemplate stackClientRestTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setInterceptors(ImmutableList
			.of(new AuthRequestInterceptor(clientEndpoint, clientEndpointUsername, clientEndpointPassword)));
		return restTemplate;
	}

	@Bean
	public IStackClient stackClient(RestTemplate stackClientRestTemplate, DiscoveryClient discoveryClient) {
		return new StackClient(stackClientRestTemplate, discoveryClient, clientServiceName);
	}

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
	public ICognitiveService cognitiveService(ICognitiveServicesClient cognitiveServicesClient,
		CognitiveServicesConfig cognitiveServicesConfig) {
		return new CognitiveService(cognitiveServicesClient, cognitiveServicesConfig);
	}

	@Bean
	public IRekognitionService rekognitionService(IRekognitionClient rekognitionClient, AwsConfig awsConfig) {
		return new RekognitionService(rekognitionClient, awsConfig);
	}

	@Bean
	public ICloudStorageService cloudStorageService(AWSStaticCredentialsProvider awsStaticCredentialsProvider,
		AwsProperties awsProperties) {
		return new CloudStorageService(awsProperties.getRegion(), awsProperties.getS3().getBucket(),
			awsProperties.getS3().getBucketFolder(), awsStaticCredentialsProvider);
	}

	@Bean
	public AggregatorService resultAggregatorService(IRekognitionService rekognitionService,
		ICognitiveService cognitiveService, IFaceExtractorService openCVService) {
		return new AggregatorService(openCVService, rekognitionService, cognitiveService);
	}

	@Bean
	public CascadeClassifier cascadeClassifier() throws URISyntaxException {
		OpenCV.loadShared();
		// lbpcascade_frontalface.xml
		URL url = AppContext.class.getClassLoader().getResource("opencv/haarcascade_frontalface_alt.xml");
		return new CascadeClassifier(Paths.get(url.toURI()).toFile().getAbsolutePath());
	}

	@Bean
	public IFaceExtractorService faceExtractorService(CascadeClassifier cascadeClassifier, FaceDBConfig faceDBConfig) {
		return new FaceExtractorService(cascadeClassifier, faceDBConfig);
	}

	@Bean
	public FaceDBConfig faceDBConfig() {
		FaceDBConfig faceDBConfig = new FaceDBConfig();
		faceDBConfig.setFaceDbPath(faceDBPath);
		faceDBConfig.setFaceDirectoryNameDelimiter(faceDirectoryNameDelimiter);
		faceDBConfig.setModelStoragePath(modelStoragePath);
		faceDBConfig.setStandardWidth(standardWidth);
		faceDBConfig.setStandardHeight(standardHeight);
		return faceDBConfig;
	}

	@Bean
	public IFaceRecognitionService faceRecognitionService(FaceDBConfig faceDBConfig) {
		return new FaceRecognitionService(faceDBConfig);
	}

	@Bean
	public AwsConfig awsConfig(AwsProperties awsProperties) {
		AwsConfig awsConfig = new AwsConfig();
		AwsProperties.Rekognition rekognition = awsProperties.getRekognition();
		awsConfig.setCollectionId(rekognition.getCollectionId());
		awsConfig.setMaxFaces(rekognition.getMaxFaces());
		return awsConfig;
	}

	@Bean
	public CognitiveServicesConfig cognitiveServicesConfig(CognitiveServicesProperties cognitiveServicesProperties) {
		CognitiveServicesConfig config = new CognitiveServicesConfig();
		config.setPersonGroupId(cognitiveServicesProperties.getPersonGroupId());
		config.setPersonGroupName(cognitiveServicesProperties.getPersonGroupName());
		config.setMaxCandidates(cognitiveServicesProperties.getMaxCandidates());
		config.setThreshold(cognitiveServicesProperties.getThreshold());
		return config;
	}
	
	
	
}
