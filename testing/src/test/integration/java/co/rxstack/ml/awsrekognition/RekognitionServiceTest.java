package co.rxstack.ml.awsrekognition;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import co.rxstack.ml.aws.rekognition.service.ICloudStorageService;
import co.rxstack.ml.client.aws.IRekognitionClient;
import co.rxstack.ml.common.model.Candidate;
import co.rxstack.ml.common.model.ComparisonResult;
import co.rxstack.ml.common.model.FaceDetectionResult;
import co.rxstack.ml.context.TestContext;
import co.rxstack.ml.utils.ImageHelper;

import org.junit.After;
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

	private static final String COLLECTION_ID = "employee_collection";

	@Autowired
	private IRekognitionClient rekognitionClient;
	@Autowired
	private ICloudStorageService cloudStorageService;

	private Map<String, byte[]> imageBytesMap;

	@Before
	public void setup() throws IOException {
		Class clazz = RekognitionServiceTest.class;
		imageBytesMap = new HashMap<>();
		imageBytesMap.put("lee-1", ImageHelper.loadResourceAsByteArray(clazz, "lee-1.jpg"));
		imageBytesMap.put("lee-2", ImageHelper.loadResourceAsByteArray(clazz, "lee-2.jpg"));
		imageBytesMap.put("lee-3", ImageHelper.loadResourceAsByteArray(clazz, "lee-3.jpg"));
		imageBytesMap.put("multiple-faces-1", ImageHelper.loadResourceAsByteArray(clazz, "multiple-faces-700x420.jpg"));

		cloudStorageService.uploadPersonFaceImage("lee-1", ImageHelper.bytes2InputStream(imageBytesMap.get("lee-1")));
		cloudStorageService.uploadPersonFaceImage("lee-2", ImageHelper.bytes2InputStream(imageBytesMap.get("lee-2")));
	}

	@Test
	public void testCompareFacesHighSimilarity() throws IOException {
		Optional<ComparisonResult> comparisonResult =
			rekognitionClient.compareFaces(imageBytesMap.get("lee-1"), imageBytesMap.get("lee-2"));
		Assert.assertTrue(comparisonResult.isPresent());
		Assert.assertEquals(99, comparisonResult.get().getConfidence(), 5);
	}

	@Test
	public void testDetectMultipleFaces() throws IOException {
		List<FaceDetectionResult> faceDetectionResults = rekognitionClient.detect(imageBytesMap.get("multiple-faces-1"));
		Assert.assertFalse(faceDetectionResults.isEmpty());
		Assert.assertEquals(5, faceDetectionResults.size(), 1);
	}

	@Test
	public void testSearchFacesByImage() {
		List<Candidate> candidateList = rekognitionClient.searchFacesByImage(COLLECTION_ID, imageBytesMap.get("lee-3"), 1);
		Assert.assertFalse(candidateList.isEmpty());
	}

	@After
	public void cleanup() {
		cloudStorageService.deleteObject("lee-1.jpg");
		cloudStorageService.deleteObject("lee-2.jpg");
	}

}
