package co.rxstack.ml.cognitiveservices;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import co.rxstack.ml.client.cognitiveservices.ICognitiveServicesClient;
import co.rxstack.ml.common.model.Candidate;
import co.rxstack.ml.common.model.FaceDetectionResult;
import co.rxstack.ml.common.model.FaceIdentificationResult;
import co.rxstack.ml.common.model.FaceRectangle;
import co.rxstack.ml.common.model.PersonGroup;
import co.rxstack.ml.context.TestContext;
import co.rxstack.ml.utils.ResourceHelper;

import com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author mhachem on 9/27/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestContext.class)
public class CognitiveServicesClientTest {
	
	private final String validPersonGroupId = "12345678";
	
	@Autowired
	private ICognitiveServicesClient cognitiveServicesClient;
	
	@Before
	public void setup() {
	}
	
	@Test
	public void testCreatePersonGroup() {
		boolean result = cognitiveServicesClient.createPersonGroup(validPersonGroupId, "test-group");
		Assert.assertTrue(result);
	}
	
	@Test
	public void testCreatePersonGroupBadPersonGroupId() {
		boolean result = cognitiveServicesClient.createPersonGroup("ABC123@", "test-group");
		Assert.assertFalse(result);
	}

	@Test
	public void testGetPersonGroup() {
		boolean result = cognitiveServicesClient.createPersonGroup(validPersonGroupId, "test-group");
		Assert.assertTrue(result);
		
		Optional<PersonGroup> personGroupOptional = cognitiveServicesClient.getPersonGroup(validPersonGroupId);
		Assert.assertTrue(personGroupOptional.isPresent());
		Assert.assertEquals("test-group", personGroupOptional.get().getName());
		Assert.assertEquals(validPersonGroupId, personGroupOptional.get().getPersonGroupId());
	}
	
	@Test
	public void testDetectFace() throws URISyntaxException, FileNotFoundException {
		try {
			List<FaceDetectionResult> faceDetectionResults = cognitiveServicesClient
				.detect(ResourceHelper.loadResourceAsByteArray(CognitiveServicesClientTest.class, "bill-gates.jpg"));
			Assert.assertFalse(faceDetectionResults.isEmpty());
			Assert.assertNotNull(faceDetectionResults.get(0).getFaceId());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDetectMultipleFaces() {
		try {
			List<FaceDetectionResult> faceDetectionResults = cognitiveServicesClient.detect(
				ResourceHelper.loadResourceAsByteArray(CognitiveServicesClientTest.class, "multiple-faces-700x420.jpg"));
			Assert.assertFalse(faceDetectionResults.isEmpty());
			Assert.assertEquals(5, faceDetectionResults.size(), 1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDetectAndSaveImage() throws IOException {
		InputStream inputStream =
			ResourceHelper.loadImage(CognitiveServicesClientTest.class, "multiple-faces-700x420.jpg");
		List<FaceDetectionResult> faceDetectionResults = cognitiveServicesClient.detect(ResourceHelper.toByteArray(inputStream));

		BufferedImage bufferedImage =
			ImageIO.read(ResourceHelper.loadImage(CognitiveServicesClientTest.class, "multiple-faces-700x420.jpg"));
		Graphics g = bufferedImage.getGraphics();
		g.setColor(Color.GREEN);

		faceDetectionResults.forEach(faceDetectionResult -> {
			FaceRectangle fRect = faceDetectionResult.getFaceRectangle();
			g.drawRect((int) fRect.getLeft(), (int) fRect.getTop(), (int) fRect.getWidth(), (int) fRect.getHeight());
		});
		ImageIO.write(bufferedImage, "png", File.createTempFile("recognized-image", ".png"));
	}

	@Test
	public void testCreatePerson() {
		boolean result = cognitiveServicesClient.createPersonGroup(validPersonGroupId, "test-group");
		Assert.assertTrue(result);
		Optional<String> fooIdOptional =
			cognitiveServicesClient.createPerson(validPersonGroupId, "Foo", "29,10");
		Assert.assertTrue(fooIdOptional.isPresent());
	}

	@Test
	public void testAddPersonFace() throws IOException {
		boolean result = cognitiveServicesClient.createPersonGroup(validPersonGroupId, "test-group");
		Assert.assertTrue(result);
		Optional<String> fooIdOptional = cognitiveServicesClient.createPerson(validPersonGroupId, "Foo", "29,10");
		Assert.assertTrue(fooIdOptional.isPresent());

		byte[] imageBytes = ResourceHelper.loadResourceAsByteArray(CognitiveServicesClientTest.class, "bill-gates.jpg");

		List<FaceDetectionResult> faceDetectionResults = cognitiveServicesClient.detect(imageBytes);
		faceDetectionResults.forEach(faceDetectionResult -> {
			Optional<String> personFaceOptional = cognitiveServicesClient
				.addPersonFace(validPersonGroupId, fooIdOptional.get(), faceDetectionResult.getFaceRectangle(),
					imageBytes);
			Assert.assertTrue(personFaceOptional.isPresent());
		});
	}

	@Test
	public void testDoCompleteCycle() throws IOException, InterruptedException {
		// 1 create person group
		boolean result = cognitiveServicesClient.createPersonGroup(validPersonGroupId, "test-group");
		Assert.assertTrue(result);

		// load images
		byte[] imageBytes1 = ResourceHelper.loadResourceAsByteArray(CognitiveServicesClientTest.class, "bill-gates.jpg");
		byte[] imageBytes2 = ResourceHelper
			.loadResourceAsByteArray(CognitiveServicesClientTest.class, "bill-gates-2.jpg");
		byte[] testImageBytes =
			ResourceHelper.loadResourceAsByteArray(CognitiveServicesClientTest.class, "bill-gates-4.jpg");

		// 2 create person
		Optional<String> createPersonOptional =
			cognitiveServicesClient.createPerson(validPersonGroupId, "Bill Gates", "54");

		if (createPersonOptional.isPresent()) {
			System.out.println("Created person");

			// 3 detect faces
			List<FaceDetectionResult> faceDetectionResults1 = cognitiveServicesClient.detect(imageBytes1);
			List<FaceDetectionResult> faceDetectionResults2 = cognitiveServicesClient.detect(imageBytes2);

			// todo save persistedFaceId to metaData in Amazon Rekognition too
			Optional<String> persistedFaceIdOptional1 = cognitiveServicesClient
				.addPersonFace(validPersonGroupId, createPersonOptional.get(),
					faceDetectionResults1.get(0).getFaceRectangle(), imageBytes1);

			Optional<String> persistedFaceIdOptional2 = cognitiveServicesClient
				.addPersonFace(validPersonGroupId, createPersonOptional.get(),
					faceDetectionResults2.get(0).getFaceRectangle(), imageBytes2);

			if (persistedFaceIdOptional1.isPresent() && persistedFaceIdOptional2.isPresent()) {
				result = cognitiveServicesClient.trainPersonGroup(validPersonGroupId);
				Assert.assertTrue(result);

				System.out.println("Training person group please wait...");
				Thread.sleep(10000L);

				List<FaceDetectionResult> faceDetectionResults = cognitiveServicesClient.detect(testImageBytes);
				Assert.assertTrue(!faceDetectionResults.isEmpty());

				Optional<String> faceIdOptional =
					faceDetectionResults.stream().map(FaceDetectionResult::getFaceId).findAny();

				faceIdOptional.ifPresent(faceId -> {
					List<FaceIdentificationResult> faceIdentificationResults =
						cognitiveServicesClient.identify(validPersonGroupId, ImmutableList.of(faceId), 1, 75);
					faceIdentificationResults.forEach(faceIdentificationResult -> {
						Optional<Candidate> candidateBestMatch = faceIdentificationResult.getCandidateBestMatch();
						if (candidateBestMatch.isPresent()) {
							System.out.println("########################################");
							System.out.println(candidateBestMatch.get().getConfidence());
							System.out.println("########################################");
						}
					});
				});
			}
		}
	}

	@After
	public void cleanup() {
		cognitiveServicesClient.deletePersonGroup(validPersonGroupId);
	}

}
