package co.rxstack.ml.tensorflow.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphUtils {

	private static final Logger log = LoggerFactory.getLogger(GraphUtils.class);

	public static byte[] readAllBytes(Path path) {
		try {
			return Files.readAllBytes(path);
		} catch (IOException e) {
			log.error("Failed to read [{}]", path);
			log.error(e.getMessage(), e);
		}
		return null;
	}

}
