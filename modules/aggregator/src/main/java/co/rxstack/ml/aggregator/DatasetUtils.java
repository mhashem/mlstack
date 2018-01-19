package co.rxstack.ml.aggregator;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import co.rxstack.ml.aggregator.model.Person;
import co.rxstack.ml.aggregator.model.PersonBundle;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;

public class DatasetUtils {

	private static final Logger logger = getLogger(DatasetUtils.class);

	private DatasetUtils() {

	}

	/**
	 * Directory <faceId>-<name>
	 * Directory images <number>.<extension>
	 *
	 * @param dir Path to directory containing folders with sub image files
	 * @return List<PersonBundle>
	 * @throws IOException when Failing to open directory
	 */
	public static List<PersonBundle> loadPersonBundleList(Path dir, String directoryNameDelimiter) throws IOException {
		List<PersonBundle> personBundleList;
		try (Stream<Path> pathStream = Files.list(dir)) {
			personBundleList = pathStream.filter(isDirectory).map(path -> {
				String[] dirName = path.getFileName().toString().split(directoryNameDelimiter);
				if (NumberUtils.isDigits(dirName[0])) {
					int faceId = Integer.parseInt(dirName[0]);
					Person person = new Person();
					person.setFaceId(faceId);
					person.setName(dirName[1]);
					List<Path> imagePaths = ImmutableList.of();
					try (Stream<Path> imagePathStream = Files.list(path)) {
						imagePaths =
							imagePathStream.filter(isFile).filter(extensionPredicate).collect(Collectors.toList());
					} catch (IOException e) {
						logger.warn(e.getMessage(), e);
					}
					return new PersonBundle(person, imagePaths);
				}
				return null;
			}).filter(Objects::nonNull).collect(Collectors.toList());
		}
		return personBundleList;
	}

	private static final Set<String> IMAGE_EXTENSIONS = ImmutableSet.of("jpg", "pgm", "png");

	private static Predicate<Path> isDirectory = path -> path.toFile().isDirectory();

	private static Predicate<Path> isFile = path -> path.toFile().isFile();

	private static Predicate<Path> extensionPredicate = path -> {
		String extension = FilenameUtils.getExtension(path.getFileName().toString());
		return IMAGE_EXTENSIONS.contains(extension.toLowerCase());
	};

}
