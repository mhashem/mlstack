package co.rxstack.ml.awsrekognition;

import java.io.InputStream;
import java.util.List;

import co.rxstack.ml.client.aws.IRekognitionClient;
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
	public void testCompareFaces() {
		Assert.assertEquals(1,1);
	}

	@Test
	public void testDetectMultipleFaces() {
		InputStream inputStream = ImageHelper.loadImage("multiple-faces-700x420.jpg");
		List<FaceDetectionResult> faceDetectionResults = rekognitionClient.detect(inputStream);
		Assert.assertFalse(faceDetectionResults.isEmpty());
		Assert.assertEquals(5, faceDetectionResults.size(), 1);
	}

}
