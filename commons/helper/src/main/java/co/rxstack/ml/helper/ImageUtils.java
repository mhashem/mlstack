package co.rxstack.ml.helper;

import java.io.IOException;
import java.io.InputStream;

import com.amazonaws.util.IOUtils;

/**
 * @author mhachem on 10/3/2017.
 */
public class ImageUtils {

	public static byte[] toByteArrays(InputStream inputStream) throws IOException {
		return IOUtils.toByteArray(inputStream);
	}

}
