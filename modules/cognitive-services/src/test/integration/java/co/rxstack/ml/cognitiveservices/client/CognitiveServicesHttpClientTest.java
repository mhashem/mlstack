package co.rxstack.ml.cognitiveservices.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import co.rxstack.ml.common.FaceDetectionResult;
import co.rxstack.ml.common.PersonGroup;
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
public class CognitiveServicesHttpClientTest {
	
	private final String validPersonGroupId = "12345678";
	
	@Autowired
	private ICognitiveServicesHttpClient cognitiveServicesHttpClient;
	
	@Before
	public void setup() {
	}
	
	@Test
	public void testCreatePersonGroup() {
		boolean result = cognitiveServicesHttpClient.createPersonGroup(validPersonGroupId, "test-group");
		Assert.assertTrue(result);
	}
	
	@Test
	public void testCreatePersonGroupBadPersonGroupId() {
		boolean result = cognitiveServicesHttpClient.createPersonGroup("ABC123@", "test-group");
		Assert.assertFalse(result);
	}
	
	@Test
	public void testGetPersonGroup() {
		boolean result = cognitiveServicesHttpClient.createPersonGroup(validPersonGroupId, "test-group");
		Assert.assertTrue(result);
		
		Optional<PersonGroup> personGroupOptional = cognitiveServicesHttpClient.getPersonGroup(validPersonGroupId);
		Assert.assertTrue(personGroupOptional.isPresent());
		Assert.assertEquals("test-group", personGroupOptional.get().getName());
		Assert.assertEquals(validPersonGroupId, personGroupOptional.get().getPersonGroupId());
	}
	
	@Test
	public void testDetectFace() throws URISyntaxException, FileNotFoundException {
		List<FaceDetectionResult> faceDetectionResults = 
			cognitiveServicesHttpClient.detect(
				CognitiveServicesHttpClientTest.class.getClassLoader().
					getResourceAsStream("bill-gates.jpg"));
		Assert.assertFalse(faceDetectionResults.isEmpty());
		Assert.assertNotNull(faceDetectionResults.get(0).getFaceId());
	}
	
	
	@After
	public void cleanup() {
		cognitiveServicesHttpClient.deletePersonGroup(validPersonGroupId);
	}
	
}