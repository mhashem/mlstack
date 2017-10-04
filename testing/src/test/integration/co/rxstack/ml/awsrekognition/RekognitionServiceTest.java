package co.rxstack.ml.awsrekognition;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import co.rxstack.ml.client.aws.IRekognitionClient;
import co.rxstack.ml.common.model.ComparisonResult;
import co.rxstack.ml.common.model.FaceDetectionResult;
import co.rxstack.ml.context.TestContext;
import co.rxstack.ml.utils.ImageHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author mhachem on 9/29/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestContext.class)
public class RekognitionServiceTest {

	@Autowired
	private IRekognitionClient rekognitionClient;

	@Before
	public void setup() {
		
	}

	@Test
	public void testCompareFacesHighSimilarity() throws IOException {
		byte[] imageOneBytes = ImageHelper.loadResourceAsByteArray("bill-gates.jpg");
		byte[] imageTwoBytes = ImageHelper.loadResourceAsByteArray("bill-gates-3.jpg");
		Optional<ComparisonResult> comparisonResult = rekognitionClient.compareFaces(imageOneBytes, imageTwoBytes);
		Assert.assertTrue(comparisonResult.isPresent());
		Assert.assertEquals(99, comparisonResult.get().getConfidence(), 5);
	}

	@Test
	public void testDetectMultipleFaces() throws IOException {
		InputStream inputStream = ImageHelper.loadImage("multiple-faces-700x420.jpg");
		List<FaceDetectionResult> faceDetectionResults = rekognitionClient.detect(ImageHelper.toByteArray(inputStream));
		Assert.assertFalse(faceDetectionResults.isEmpty());
		Assert.assertEquals(5, faceDetectionResults.size(), 1);
	}

}
