package co.rxstack.ml.aggregator.classifier;

import static org.bytedeco.javacpp.opencv_core.CV_STORAGE_WRITE;
import static org.bytedeco.javacpp.opencv_ml.SVM;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PreDestroy;

import co.rxstack.ml.aggregator.config.ClassifierConfig;

import org.bytedeco.javacpp.opencv_core.CvFileStorage;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.FileStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClassifierService implements IClassifierService {

	private static final Logger log = LoggerFactory.getLogger(ClassifierService.class);

	private ClassifierConfig classifierConfig;

	private SVM svmClassifier;
	private AtomicBoolean classifierLoaded = new AtomicBoolean(false);

	@Autowired
	public ClassifierService(ClassifierConfig classifierConfig) {

	}

	@Override
	public void load() {
		log.info("Loading SVM Classifier at {}", classifierConfig.getClassifierPath());
		try {
			svmClassifier = SVM.load(classifierConfig.getClassifierPath());
			classifierLoaded.set(true);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void save() {
		log.info("Saving SVM Classifier at {}", classifierConfig.getClassifierPath());
		String fileName = classifierConfig.getClassifierNamePrefix() +
			LocalDateTime.now().format(DateTimeFormatter.ISO_DATE) + ".xml";
		String savingDestination = classifierConfig.getClassifierPath() + File.separator + fileName;
		CvFileStorage cvFileStorage = CvFileStorage.open(savingDestination,
			CvMemStorage.create(), CV_STORAGE_WRITE);
		FileStorage fileStorage = new FileStorage(cvFileStorage);
		svmClassifier.write(fileStorage);
		cvFileStorage.release();
		fileStorage.release();
		log.info("Saved successfully at {}", savingDestination);
	}

	@Override
	public void train(String imagesDir) {

	}

	private void reload() {

	}

	@PreDestroy
	public void cleanUp() {
		svmClassifier.close();
	}

}
