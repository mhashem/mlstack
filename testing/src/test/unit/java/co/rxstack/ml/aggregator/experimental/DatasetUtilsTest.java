package co.rxstack.ml.aggregator.experimental;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import co.rxstack.ml.aggregator.utils.DataSetUtils;
import co.rxstack.ml.aggregator.model.PersonBundle;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DatasetUtilsTest {

	private Path facedbPath;

	@Before
	public void setup() throws IOException {
//		facedbPath = Files.createDirectory(Paths.get("C:\\etc\\facedb"));
//		Files.createDirectory(Paths.get("C:\\etc\\facedb\\1-foo"));
//		Files.createFile(Paths.get("C:\\etc\\facedb\\1-foo\\1.png"));
	}

	@Test
	public void testLoadPersonBundleList() throws IOException {
		// in case code is moved would fail
		Path facedbPath = Paths.get("C:\\etc\\mlstack\\facedb");
		if (facedbPath.toFile().exists()) {
			List<PersonBundle> personBundleList = DataSetUtils.loadPersonBundleList(facedbPath, "-");
			Assert.assertTrue(!personBundleList.isEmpty());

/*			PersonBundle personBundle = personBundleList.get(0);

			Assert.assertEquals(1, personBundle.getIdentity().getFaceId());
			Assert.assertEquals("foo", personBundle.getIdentity().getName());
			Assert.assertEquals(Paths.get("C:\\etc\\facedb\\1-foo\\1.png"), personBundle.getFaceImagesPaths().get(0));
*/
		}
	}

	@After
	public void cleanup() throws IOException {
		//FileSystemUtils.deleteRecursively(facedbPath.toFile());
	}

}
