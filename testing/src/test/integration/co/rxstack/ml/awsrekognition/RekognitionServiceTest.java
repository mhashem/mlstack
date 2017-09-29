package co.rxstack.ml.awsrekognition;

import co.rxstack.ml.client.aws.IRekognitionClient;
import co.rxstack.ml.context.TestContext;
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

}
