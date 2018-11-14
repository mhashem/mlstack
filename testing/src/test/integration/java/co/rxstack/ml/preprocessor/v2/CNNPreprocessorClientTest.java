package co.rxstack.ml.preprocessor.v2;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import co.rxstack.ml.client.preprocessor.v2.CNNPreprocessorClient;
import co.rxstack.ml.utils.ResourceHelper;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public class CNNPreprocessorClientTest {

	private CNNPreprocessorClient preprocessorClient;

	@Before
	public void setup() {
		preprocessorClient = new CNNPreprocessorClient();
	}

	@Test
	public void testAlignment() throws IOException {
		List<BufferedImage> alignedImages = preprocessorClient.align(
			ResourceHelper.loadResourceAsByteArray(CNNPreprocessorClientTest.class, "multiple-faces-700x420.jpg"));
		Assertions.assertThat(alignedImages).hasSize(5);
	}

	@Test
	public void testDetection() {

	}

}
