package co.rxstack.ml.awsrekognition;

import java.io.IOException;
import java.io.InputStream;

import co.rxstack.ml.aws.rekognition.service.ICloudStorageService;
import co.rxstack.ml.context.TestContext;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author mhachem on 9/30/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestContext.class)
public class CloudStorageServiceTest {

	private final String testPersonName = "bill";

	@Autowired
	private ICloudStorageService cloudStorageService;

	private InputStream imageInputStream;

	@Before
	public void setup() {
		imageInputStream = getClass().getClassLoader().getResourceAsStream("bill-gates.jpg");
	}

	@Test
	public void testUploadImage() {
		try {
			int available = imageInputStream.available();
			cloudStorageService.uploadPersonFaceImage(testPersonName, imageInputStream);
			byte[] objectBytes = cloudStorageService.getObjectAsByteArray(testPersonName + ".jpg");
			Assert.assertTrue(objectBytes.length > 0);
			Assert.assertEquals(available, objectBytes.length, 10);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDeleteNonExistingObject() {
		cloudStorageService.deleteObject("Foo.jpg");
	}

	@Test
	public void testGetCloudIndexFaceIds() {
		cloudStorageService.getCloudIndexFaceIds("employee_collection");
	}

	@After
	public void cleanup() {
		cloudStorageService.deleteObject(testPersonName);
	}

}
