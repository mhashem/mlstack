package co.rxstack.ml.core.context;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import co.rxstack.ml.aggregator.config.ClassifierConfig;
import co.rxstack.ml.aggregator.config.FaceDBConfig;
import co.rxstack.ml.aggregator.dao.FaceDao;
import co.rxstack.ml.aggregator.dao.IdentityDao;
import co.rxstack.ml.aggregator.service.IFaceExtractorService;
import co.rxstack.ml.aggregator.service.IFaceRecognitionService;
import co.rxstack.ml.aggregator.service.IIdentityService;
import co.rxstack.ml.aggregator.service.IStorageService;
import co.rxstack.ml.aggregator.service.impl.AggregatorService;
import co.rxstack.ml.aggregator.service.impl.FaceExtractorService;
import co.rxstack.ml.aggregator.service.impl.FaceRecognitionService;
import co.rxstack.ml.aggregator.service.impl.IdentityService;
import co.rxstack.ml.aggregator.service.impl.StorageService;
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
import co.rxstack.ml.client.preprocessor.PreprocessorClient;
import co.rxstack.ml.cognitiveservices.config.CognitiveServicesConfig;
import co.rxstack.ml.cognitiveservices.service.ICognitiveService;
import co.rxstack.ml.cognitiveservices.service.impl.CognitiveService;
import co.rxstack.ml.core.config.AwsProperties;
import co.rxstack.ml.core.config.CognitiveServicesProperties;
import co.rxstack.ml.core.factory.AuthRequestInterceptor;
import co.rxstack.ml.tensorflow.config.FaceNetConfig;
import co.rxstack.ml.tensorflow.config.InceptionConfig;
import co.rxstack.ml.tensorflow.service.IFaceNetService;
import co.rxstack.ml.tensorflow.service.impl.FaceNetService;
import co.rxstack.ml.tensorflow.service.impl.InceptionService;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import nu.pattern.OpenCV;
import org.apache.commons.io.IOUtils;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacpp.opencv_objdetect.CvHaarClassifierCascade;
import org.opencv.objdetect.CascadeClassifier;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.client.RestTemplate;

/**
 * @author mhachem on 9/27/2017.
 */
@Configuration
public class AppContext {

	private static final Logger logger = getLogger(AppContext.class);
	
	static {
		// Preload the opencv_objdetect module to work around a known bug.
		Loader.load(opencv_objdetect.class);
		OpenCV.loadShared();
	}

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

	@Value("${tensor.graph.inception.path}")
	private String graphPath;
	@Value("${tensor.labels.path}")
	private String labelsPath;
	@Value("${tensor.graph.facenet.path}")
	private String faceNetGraphPath;

	@Value("${preprocesser.host}")
	private String preprocessorHost;

	@Value("${classifier.path}")
	private String classifierPath;
	@Value("${classifier.name.prefix}")
	private String classifierNamePrefix;

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
	public IIdentityService identityService(IdentityDao identityDao, FaceDao faceDao) {
		return new IdentityService(identityDao, faceDao);
	}

	@Bean
	public AggregatorService resultAggregatorService(IRekognitionService rekognitionService,
		ICognitiveService cognitiveService, IFaceExtractorService openCVService,
		IFaceRecognitionService faceRecognitionService, IIdentityService identityService,
		InceptionService inceptionService, PreprocessorClient preprocessorClient, IFaceNetService faceNetService) {
		return new AggregatorService(identityService, openCVService, faceRecognitionService, rekognitionService,
			cognitiveService, inceptionService, preprocessorClient, faceNetService);
	}

	@Qualifier("haarCascadeFile")
	@Bean
	public File haarCascadeFile() {
		try {
			Path tempFile = Files.createTempFile("haarcascade_frontalface_alt", "xml");
			InputStream inputStream = new ClassPathResource("opencv/haarcascade_frontalface_alt.xml")
				.getInputStream();
			Files.copy(inputStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
			IOUtils.closeQuietly(inputStream);
			return tempFile.toFile();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}

	@Qualifier("cascadeClassifier")
	@Bean
	public CascadeClassifier cascadeClassifier(File haarCascadeFile) {
		return new CascadeClassifier(haarCascadeFile.getAbsolutePath());
	}

	@Qualifier("cvHaarClassifierCascade")
	@Bean
	public CvHaarClassifierCascade cvHaarClassifierCascade(File haarCascadeFile) {
		try {
			return new opencv_objdetect.CvHaarClassifierCascade(opencv_core.cvLoad(haarCascadeFile.getAbsolutePath()));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}

	@Bean
	public IFaceExtractorService faceExtractorService(CvHaarClassifierCascade cvHaarClassifierCascade, CascadeClassifier cascadeClassifier,
		FaceDBConfig faceDBConfig) {
		return new FaceExtractorService(cvHaarClassifierCascade, cascadeClassifier, faceDBConfig);
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
	public IFaceRecognitionService faceRecognitionService(FaceDBConfig faceDBConfig,
		IFaceExtractorService faceExtractorService) {
		return new FaceRecognitionService(faceDBConfig, faceExtractorService);
	}

	@Bean
	public IStorageService storageService(FaceDBConfig faceDBConfig, ICloudStorageService cloudStorageService) {
		return new StorageService(faceDBConfig, cloudStorageService);
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

	@Bean
	public InceptionConfig inceptionConfig() {
		InceptionConfig inceptionConfig = new InceptionConfig();
		inceptionConfig.setGraphPath(graphPath);
		inceptionConfig.setLabelsPath(labelsPath);
		return inceptionConfig;
	}

	@Bean
	public FaceNetConfig faceNetConfig() {
		FaceNetConfig faceNetConfig = new FaceNetConfig();
		faceNetConfig.setFaceNetGraphPath(faceNetGraphPath);
		return faceNetConfig;
	}

	@Bean
	public InceptionService inceptionService(InceptionConfig inceptionConfig) {
		try {
			return new InceptionService(inceptionConfig);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Bean
	public IFaceNetService faceNetService(FaceNetConfig faceNetConfig) {
		try {
			return new FaceNetService(faceNetConfig);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e);
		}
	}

	@Bean
	public ClassifierConfig classifierConfig() {
		ClassifierConfig classifierConfig = new ClassifierConfig();
		classifierConfig.setClassifierPath(classifierPath);
		classifierConfig.setClassifierNamePrefix(classifierNamePrefix);
		return classifierConfig;
	}

	/*@Bean
	public IClassifierService classifierService(ClassifierConfig classifierConfig) {
		return new ClassifierService(classifierConfig);
	}*/

	@Bean
	public PreprocessorClient preprocessorClient() {
		return new PreprocessorClient(preprocessorHost);
	}

}
