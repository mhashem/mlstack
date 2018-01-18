package co.rxstack.ml.aggregator.experimental;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import co.rxstack.ml.aggregator.experimental.model.PersonBundle;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

public class DatasetUtilsTest {

	private Path facedbPath;

	@Before
	public void setup() throws IOException {
		facedbPath = Files.createDirectory(Paths.get("C:\\etc\\facedb"));
		Files.createDirectory(Paths.get("C:\\etc\\facedb\\1-foo"));
		Files.createFile(Paths.get("C:\\etc\\facedb\\1-foo\\1.png"));
	}

	@Test
	public void testLoadPersonBundleList() throws IOException {
		// in case code is moved would fail
		Path facedbPath = Paths.get("C:\\etc\\facedb");
		if (facedbPath.toFile().exists()) {
			List<PersonBundle> personBundleList = DatasetUtils.loadPersonBundleList(facedbPath, "-");
			Assert.assertTrue(!personBundleList.isEmpty());
			PersonBundle personBundle = personBundleList.get(0);
			Assert.assertEquals(1, personBundle.getPerson().getFaceId());
			Assert.assertEquals("foo", personBundle.getPerson().getName());
			Assert.assertEquals(Paths.get("C:\\etc\\facedb\\1-foo\\1.png"), personBundle.getFaceImagesPaths().get(0));
		}
	}

	@After
	public void cleanup() throws IOException {
		FileSystemUtils.deleteRecursively(facedbPath.toFile());
	}

}
