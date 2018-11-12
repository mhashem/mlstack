package com.rxstack.ml.manual;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import co.rxstack.ml.aggregator.model.Person;
import co.rxstack.ml.aggregator.model.PersonBundle;
import co.rxstack.ml.aggregator.utils.DataSetUtils;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;

public class EndToEndTest {

	private static final Logger logger = getLogger(EndToEndTest.class);
	
	private RxStackClient rxStackClient;
	private List<PersonBundle> trainPhotos;
	private List<PersonBundle> testPhotos;

	private Map<String, List<Path>> identitiesPathMap;
	
	@Before
	public void setup() throws IOException {
		rxStackClient = RxStackClient.createDefaultClient();
		rxStackClient.startWebSocketConnection();

		// trainPhotos = DataSetUtils.loadPersonBundleList(Paths.get("C:/etc/mlstack/photos"), "-");
		trainPhotos = DataSetUtils.loadPersonBundleList(Paths.get("C:/etc/mlstack/lfw-photos"), "-", 15, 9);
		testPhotos = DataSetUtils.loadPersonBundleList(Paths.get("C:/etc/mlstack/test-photos"), "-", 15, 9);

		// identitiesPathMap = DataSetUtils.loadFaceIdImagePathsMap(Paths.get("C:/etc/mlstack/photos"), 20, 3);

	}
	
	@Test
	@Ignore
	public void moveFiles() throws IOException {
		Map<String, List<Path>> listMap = DataSetUtils.loadFaceIdImagePathsMap(
			Paths.get("C:/Users/mhachem/Downloads/lfw-dataset/lfw-deepfunneled/lfw-deepfunneled"), 
			2500, 10);
		int index = 2054;
		String sql = "";
		for (String s : listMap.keySet()) {
			String p = "C:/etc/mlstack/lfw-photos/" + index + "-" +s;
			Files.createDirectory(Paths.get(p));;
			for (Path path : listMap.get(s)) {
				Files.copy(path, Paths.get(p + "/" + UUID.randomUUID().toString() + ".jpg"));
			}
			index++;
			sql += "INSERT INTO person (id, name) VALUES (" + index + ", '" + s + "');\n";
		}
		System.out.println(sql);
	}
	
	@Test
	public void indexingE2e() {
		trainPhotos.forEach(personBundle -> index(personBundle.getPerson(), personBundle.getFaceImagesPaths()));
	}

	@Test
	public void indexing() {
		identitiesPathMap.keySet().forEach(identity -> {
			Person person = Person.createFromString(identity);
			index(person, identitiesPathMap.get(identity));
		});
	}
	
	@Test
	public void recognitionE2e() throws InterruptedException {

		PersonBundle personBundle = testPhotos.get(0);
		personBundle.getFaceImagesPaths().stream().findFirst().ifPresent(path -> {
			try {
				rxStackClient.recognize(path);
			} catch (UnirestException e) {
				e.printStackTrace();
			}
		});

		TimeUnit.SECONDS.sleep(5);
	}

	@After
	public void tearDown() {
		rxStackClient.stopWebSocketConnection();
	}
	
	private void index(Person person, List<Path> paths) {
		logger.info("Indexing {}-{}", person.getFaceId(), person.getName());
		for (Path path : paths) {
			try {
				System.out.println("Indexing --> " + path.toString());
				rxStackClient.index(person.getFaceId(), person.getName(), path);
				TimeUnit.SECONDS.sleep(5); // prevent rate limiting exception
			} catch (UnirestException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	} 

}
