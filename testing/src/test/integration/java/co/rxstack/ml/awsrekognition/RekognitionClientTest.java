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
import co.rxstack.ml.utils.ResourceHelper;

import com.amazonaws.services.rekognition.model.FaceRecord;
import com.amazonaws.services.rekognition.model.IndexFacesResult;
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
public class RekognitionClientTest {

	private static final String COLLECTION_ID = "employee_collection";

	@Autowired
	private IRekognitionClient rekognitionClient;
	@Autowired
	private ICloudStorageService cloudStorageService;

	private Map<String, byte[]> imageBytesMap;

	@Before
	public void setup() throws IOException {
		Class clazz = RekognitionClientTest.class;
		imageBytesMap = new HashMap<>();
		imageBytesMap.put("lee-1", ResourceHelper.loadResourceAsByteArray(clazz, "lee-1.jpg"));
		imageBytesMap.put("lee-2", ResourceHelper.loadResourceAsByteArray(clazz, "lee-2.jpg"));
		imageBytesMap.put("lee-3", ResourceHelper.loadResourceAsByteArray(clazz, "lee-3.jpg"));
		imageBytesMap.put("multiple-faces-1", ResourceHelper.loadResourceAsByteArray(clazz, "multiple-faces-700x420.jpg"));
		imageBytesMap.put("mhmd", ResourceHelper.loadResourceAsByteArray(clazz, "mhmd.jpg"));
		imageBytesMap.put("mhmd-test", ResourceHelper.loadResourceAsByteArray(clazz, "mhmd_test.png"));

		cloudStorageService.uploadImage("lee-1", ResourceHelper.bytes2InputStream(imageBytesMap.get("lee-1")));
		cloudStorageService.uploadImage("lee-2", ResourceHelper.bytes2InputStream(imageBytesMap.get("lee-2")));
		cloudStorageService.uploadImage("mhmd", ResourceHelper.bytes2InputStream(imageBytesMap.get("mhmd")));
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

	@Test
	public void testSearchFacesByImage2() {
		List<Candidate> candidateList = rekognitionClient.searchFacesByImage(COLLECTION_ID, imageBytesMap.get("mhmd-test"), 2);
		candidateList.forEach(candidate -> Assert.assertTrue(candidate.getConfidence() > 80.0f));
	}

	@Test
	public void testIndexFace() {
		Optional<IndexFacesResult> indexFacesResultOptional =
			rekognitionClient.indexFace(COLLECTION_ID, imageBytesMap.get("lee-1"));
		Assert.assertTrue(indexFacesResultOptional.isPresent());
		IndexFacesResult indexFacesResult = indexFacesResultOptional.get();
		FaceRecord faceRecord = indexFacesResult.getFaceRecords().get(0);
		Assert.assertNotNull(faceRecord.getFace().getFaceId());
	}

	@After
	public void cleanup() {
		cloudStorageService.deleteObject("lee-1.jpg");
		cloudStorageService.deleteObject("lee-2.jpg");
		cloudStorageService.deleteObject("mhmd_test.png");
	}

}
