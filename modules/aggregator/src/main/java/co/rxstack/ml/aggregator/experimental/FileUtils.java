package co.rxstack.ml.aggregator.experimental;

import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.io.FilenameUtils;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;

public class FileUtils {

	public static void loadDataSet(Path dir) throws IOException {
		List<Person> personList = Files.list(dir).filter(imagePredicate).map(path -> {
			// <label>-<number>.<extension>
			String label = path.getFileName().toString().split("-")[0];
			Person person = new Person();
			person.setName(label);
			person.setImagePath(path);
			return person;
		}).collect(Collectors.toList());

		MatVector images = new MatVector(personList.size());
		Mat labels = new Mat(personList.size(), 1, CV_32SC1);
		IntBuffer labelsBuf = labels.createBuffer();

		int counter = 0;

	}

	private static final Set<String> IMAGE_EXTENSIONS = ImmutableSet.of("jpg", "pgm", "png");

	private static Predicate<Path> imagePredicate = path -> {
		if (Files.isDirectory(path)) {
			return false;
		}
		String extension = FilenameUtils.getExtension(path.getFileName().toString());
		if (IMAGE_EXTENSIONS.contains(extension.toLowerCase())) {
			return true;
		}
		return false;
	};

}
