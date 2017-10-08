package co.rxstack.ml.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.amazonaws.util.IOUtils;

/**
 * @author mhachem on 10/2/2017.
 */
public class ImageHelper {

	public static byte[] loadResourceAsByteArray(Class clazz, String imageName) throws IOException {
		return toByteArray(loadImage(clazz, imageName));
	}

	public static InputStream loadImage(Class clazz, String imageName) {
		return clazz.getClassLoader().getResourceAsStream(imageName);
	}

	public static byte[] toByteArray(InputStream inputStream) throws IOException {
		return IOUtils.toByteArray(inputStream);
	}

	public static InputStream bytes2InputStream(byte[] imageBytes) {
		return new ByteArrayInputStream(imageBytes);
	}

}

