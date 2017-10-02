package co.rxstack.ml.utils;

import java.io.IOException;
import java.io.InputStream;

import co.rxstack.ml.cognitiveservices.CognitiveServicesClientTest;
import com.amazonaws.util.IOUtils;

/**
 * @author mhachem on 10/2/2017.
 */
public class ImageHelper {

	public static byte[] loadResourceAsByteArray(String imageName) throws IOException {
		return toByteArray(loadImage(imageName));
	}

	public static InputStream loadImage(String imageName) {
		return CognitiveServicesClientTest.class.getClassLoader()
			.getResourceAsStream(imageName);
	}

	public static byte[] toByteArray(InputStream inputStream) throws IOException {
		return IOUtils.toByteArray(inputStream);
	}

}

