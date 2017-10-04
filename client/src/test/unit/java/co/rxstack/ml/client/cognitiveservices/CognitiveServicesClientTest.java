package co.rxstack.ml.client.cognitiveservices;

import java.net.URI;

import co.rxstack.ml.client.cognitiveservices.impl.CognitiveServicesClient;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author mhachem on 10/4/2017.
 */
public class CognitiveServicesClientTest {

	private ICognitiveServicesClient cognitiveServicesClient;

	@Before
	public void setup() {
		cognitiveServicesClient = new CognitiveServicesClient(URI.create("https://localhost:8082"), "");
	}

	@Test
	public void testCreatePersonGroup() {
		boolean result = cognitiveServicesClient.createPersonGroup("12345", "test-group");
		Assert.assertFalse(result);
	}

}
