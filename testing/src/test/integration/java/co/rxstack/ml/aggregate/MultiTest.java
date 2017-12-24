package co.rxstack.ml.aggregate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import co.rxstack.ml.client.aws.IRekognitionClient;
import co.rxstack.ml.client.cognitiveservices.ICognitiveServicesClient;
import co.rxstack.ml.common.model.FaceDetectionResult;
import co.rxstack.ml.context.TestContext;
import co.rxstack.ml.utils.ResourceHelper;

import com.google.common.base.Stopwatch;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestContext.class)
public class MultiTest {

	private static final String MULTI_FACES = "multi-faces";

	private Map<String, byte[]> imageBytesMap;

	@Autowired
	private IRekognitionClient rekognitionClient;
	@Autowired
	private ICognitiveServicesClient cognitiveServicesClient;

	@Before
	public void setup() throws IOException {
		imageBytesMap = new HashMap<>();
		imageBytesMap.put(MULTI_FACES, ResourceHelper.loadResourceAsByteArray(
			MultiTest.class,
			"multi/multi-faces-4f-1.jpg"));
	}

	@Test
	public void testDetectFaces() throws ExecutionException, InterruptedException {

		Stopwatch stopwatch = Stopwatch.createStarted();

		byte[] imageBytes = imageBytesMap.get(MULTI_FACES);

		CompletableFuture<List<FaceDetectionResult>> awsCompletableFuture =
			CompletableFuture.supplyAsync(() -> rekognitionClient.detect(imageBytes));
		CompletableFuture<List<FaceDetectionResult>> csCompletableFuture =
			CompletableFuture.supplyAsync(() -> cognitiveServicesClient.detect(imageBytes));

		CompletableFuture<Void> combinedCompletableFuture =
			CompletableFuture.allOf(awsCompletableFuture, csCompletableFuture);

		combinedCompletableFuture.get();

		if (awsCompletableFuture.isDone() && csCompletableFuture.isDone()) {
			System.out.println("AWS: " + awsCompletableFuture.get().size());
			System.out.println("CS: " + csCompletableFuture.get().size());
		}

		List<FaceDetectionResult> awsFaceDetectionResults = awsCompletableFuture.get();
		List<FaceDetectionResult> csFaceDetectionResults = csCompletableFuture.get();

		awsFaceDetectionResults.forEach(System.out::println);

		System.out.println("--------------------------------------------------");

		csFaceDetectionResults.forEach(System.out::println);

		System.out.println("Detection completed in " + stopwatch.elapsed().getSeconds() + " seconds");
	}

}
