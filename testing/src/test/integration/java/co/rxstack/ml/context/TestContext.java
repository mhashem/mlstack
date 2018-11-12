package co.rxstack.ml.context;

import static org.bytedeco.javacpp.opencv_objdetect.CvHaarClassifierCascade;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import co.rxstack.ml.aggregator.config.FaceDBConfig;
import co.rxstack.ml.aggregator.service.impl.FaceExtractorService;
import co.rxstack.ml.aws.rekognition.service.ICloudStorageService;
import co.rxstack.ml.aws.rekognition.service.impl.CloudStorageService;
import co.rxstack.ml.client.aws.IMachineLearningClient;
import co.rxstack.ml.client.aws.IRekognitionClient;
import co.rxstack.ml.client.aws.impl.MachineLearningClient;
import co.rxstack.ml.client.aws.impl.RekognitionClient;
import co.rxstack.ml.client.cognitiveservices.ICognitiveServicesClient;
import co.rxstack.ml.client.cognitiveservices.impl.CognitiveServicesClient;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.google.common.base.Throwables;
import nu.pattern.OpenCV;
import org.apache.commons.io.IOUtils;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_objdetect;
import org.opencv.objdetect.CascadeClassifier;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

/**
 * @author mhachem on 9/27/2017.
 */
@Configuration
public class TestContext {

	static {
		// Preload the opencv_objdetect module to work around a known bug.
		Loader.load(opencv_objdetect.class);
		OpenCV.loadShared();
	}
	
	private static final Logger logger = getLogger(TestContext.class);
	
	public static final String AWS_REGION = "eu-west-1";

	@Bean("cognitiveServicesUri")
	public URI cognitiveServicesUri() {
		return URI.create("https://northeurope.api.cognitive.microsoft.com/face/v1.0");
	}

	@Bean
	public ICognitiveServicesClient cognitiveServicesClient(URI cognitiveServicesUri) {
		return new CognitiveServicesClient(cognitiveServicesUri, "8407dfc043ae486a8f36bff5034da21f");
	}

	@Bean
	public AWSStaticCredentialsProvider awsStaticCredentialsProvider() {
		return new AWSStaticCredentialsProvider(
			new BasicAWSCredentials("AKIAJGSF7P2RGBJPJ76A", "vgXyU62xmbAlQ6oB8qTPKvNBxW9lefVMUbVLwq3o"));
	}

	@Bean
	public IRekognitionClient rekognitionClient(AWSStaticCredentialsProvider awsStaticCredentialsProvider) {
		return new RekognitionClient(AWS_REGION, awsStaticCredentialsProvider);
	}
	
	@Bean
	public IMachineLearningClient machineLearningClient(AWSStaticCredentialsProvider awsStaticCredentialsProvider) {
		return new MachineLearningClient(AWS_REGION, awsStaticCredentialsProvider);
	}

	@Bean
	public ICloudStorageService cloudStorageService(AWSStaticCredentialsProvider awsStaticCredentialsProvider) {
		return new CloudStorageService(AWS_REGION, "mlstack", "index", awsStaticCredentialsProvider);
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
	public FaceDBConfig faceDBConfig() {
		FaceDBConfig config = new FaceDBConfig();
		config.setStandardHeight(120);
		config.setStandardWidth(120);
		return config;
	}

	@Bean
	public FaceExtractorService faceExtractorService(CvHaarClassifierCascade cvHaarClassifierCascade, CascadeClassifier cascadeClassifier, FaceDBConfig faceDBConfig) {
		return new FaceExtractorService(cvHaarClassifierCascade, cascadeClassifier, faceDBConfig);
	}

}
