package co.rxstack.ml.context;

import static org.bytedeco.javacpp.opencv_objdetect.CvHaarClassifierCascade;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import co.rxstack.ml.aggregator.config.FaceDBConfig;
import co.rxstack.ml.aggregator.service.impl.FaceExtractorService;
import co.rxstack.ml.aws.rekognition.service.ICloudStorageService;
import co.rxstack.ml.aws.rekognition.service.impl.CloudStorageService;
import co.rxstack.ml.client.aws.IRekognitionClient;
import co.rxstack.ml.client.aws.impl.RekognitionClient;
import co.rxstack.ml.client.cognitiveservices.ICognitiveServicesClient;
import co.rxstack.ml.client.cognitiveservices.impl.CognitiveServicesClient;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.google.common.base.Throwables;
import nu.pattern.OpenCV;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_objdetect;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author mhachem on 9/27/2017.
 */
@Configuration
public class TestContext {

	private static final String AWS_REGION = "eu-west-1";

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
	public ICloudStorageService cloudStorageService(AWSStaticCredentialsProvider awsStaticCredentialsProvider) {
		return new CloudStorageService(AWS_REGION, "mlstack", "index", awsStaticCredentialsProvider);
	}

	@Bean
	public CascadeClassifier cascadeClassifier() throws URISyntaxException {
		// lbpcascade_frontalface.xml
		File classifierFile = new File(
			TestContext.class.getClassLoader().getResource("opencv/haarcascade_frontalface_alt.xml").getFile());
		return new CascadeClassifier(classifierFile.getAbsolutePath());
	}

	@Bean
	public CvHaarClassifierCascade cvHaarClassifierCascade() throws URISyntaxException {
		OpenCV.loadShared();
		File classifierFile = new File(
			TestContext.class.getClassLoader().getResource("opencv/haarcascade_frontalface_alt.xml").getFile());
		try {
			return new opencv_objdetect.CvHaarClassifierCascade(opencv_core.cvLoad(classifierFile.getCanonicalPath()));
		} catch (IOException e) {
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
