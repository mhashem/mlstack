package co.rxstack.ml.awsrekognition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import co.rxstack.ml.aws.rekognition.service.ICloudStorageService;
import co.rxstack.ml.context.TestContext;

import com.amazonaws.util.IOUtils;
import com.google.common.collect.ImmutableMap;
import org.junit.After;
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

	@Autowired
	private ICloudStorageService cloudStorageService;

	private InputStream imageInputStream;

	@Before
	public void setup() {
		imageInputStream = getClass().getClassLoader().getResourceAsStream("bill-gates.jpg");
	}

	@Test
	public void testUploadImage() {
		cloudStorageService.uploadImage("bill-gates.jpg", imageInputStream,
				ImmutableMap.of("FullName", "Bill Gates"));
	}

	@After
	public void cleanup() {

	}

	private File inputStream2File(InputStream in) throws IOException {
		File tempFile = File.createTempFile("test-image", "jpg");
		tempFile.deleteOnExit();
		FileOutputStream out = new FileOutputStream(tempFile);
		IOUtils.copy(in, out);
		return tempFile;
	}

}
