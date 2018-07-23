package co.rxstack.ml.aggregator.service.impl;

import static org.bytedeco.javacpp.opencv_core.CV_STORAGE_WRITE;
import static org.bytedeco.javacpp.opencv_core.CV_TERMCRIT_ITER;
import static org.bytedeco.javacpp.opencv_ml.ROW_SAMPLE;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import co.rxstack.ml.aggregator.config.ClassifierConfig;
import co.rxstack.ml.aggregator.service.IClassifierService;

import com.google.common.base.Stopwatch;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_ml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@Component
public class EClassifierService implements IClassifierService<opencv_ml.SVM> {

	private static final Logger log = LoggerFactory.getLogger(EClassifierService.class);

	private opencv_core.Mat classes;
	private opencv_ml.SVM classifier;
	private ClassifierConfig classifierConfig;

	private AtomicBoolean classifierLoaded = new AtomicBoolean(false);

	//@Autowired
	public EClassifierService(ClassifierConfig classifierConfig) {
		this.classifierConfig = classifierConfig;
		this.classes = new opencv_core.Mat();
	}

	@PostConstruct
	public void init() {
		log.info("Initializing ClassifierService");
		load();
	}

	@Override
	public synchronized opencv_ml.SVM getClassifier() {

		if (classifier == null || classifier.isNull()) {
			log.info("no loaded classifier found, initializing new classifier!");
			load();
		}

		try (opencv_core.CvTermCriteria cvTermCriteria = new opencv_core.CvTermCriteria(CV_TERMCRIT_ITER, 1000,
			0.0001)) {
			classifier.setType(opencv_ml.SVM.C_SVC);
			classifier.setKernel(opencv_ml.SVM.LINEAR);
			classifier.setTermCriteria(cvTermCriteria.asTermCriteria());
		}

		if (!classifier.isTrained()) {
			log.warn("classifier is not trained!");
		}

		return classifier;
	}

	@Override
	public void load() {
		log.info("Loading classifier at {}", classifierConfig.getClassifierPath());
		try {
			Path classifierPath = Paths.get(classifierConfig.getClassifierPath());
			if (!classifierPath.toFile().exists()) {
				log.warn("No classifier found at {}", classifierConfig.getClassifierPath());
				classifier = opencv_ml.SVM.create();
			} else {
				classifier = opencv_ml.SVM.load(classifierConfig.getClassifierPath());
			}
			classifierLoaded.set(true);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void train(Map<Integer, List<float[]>> idFeaturesListMap) {
		List<Integer> trainingLabels = new ArrayList<>();
		try (opencv_core.Mat trainingImages = new opencv_core.Mat();
			opencv_core.Mat trainingData = new opencv_core.Mat();) {

			// mapping features list to matrices

			idFeaturesListMap.keySet().forEach(id -> idFeaturesListMap.get(id).forEach(featuresArray -> {
				opencv_core.Mat featuresArrayMat = new opencv_core.Mat(featuresArray);
				featuresArrayMat = featuresArrayMat.reshape(0, 1);
				trainingImages.push_back(featuresArrayMat);
				trainingLabels.add(id);
			}));

			// mapping classes to vector

			int[] labelsArray = new int[trainingLabels.size()];
			for (int i = 0; i < trainingLabels.size(); ++i)
				labelsArray[i] = trainingLabels.get(i);
			try (opencv_core.Mat labelsArrayMat = new opencv_core.Mat(labelsArray)) {
				labelsArrayMat.copyTo(classes);
			}

			boolean isTrained = getClassifier().train(trainingData, ROW_SAMPLE, classes);
			if (isTrained) {
				log.info("SVM classifier was trained successfully");
				save();
			} else {
				log.warn("Failed to train SVM classifier!");
			}
		}
	}

	@Override
	public void save() {
		log.info("Saving Classifier at {}", classifierConfig.getClassifierPath());
		String fileName =
			classifierConfig.getClassifierNamePrefix() + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE)
				+ ".xml";
		String savingDestination = classifierConfig.getClassifierPath() + File.separator + fileName;
		opencv_core.CvFileStorage cvFileStorage =
			opencv_core.CvFileStorage.open(savingDestination, opencv_core.CvMemStorage.create(), CV_STORAGE_WRITE);
		opencv_core.FileStorage fileStorage = new opencv_core.FileStorage(cvFileStorage);
		classifier.write(fileStorage);
		cvFileStorage.release();
		fileStorage.release();
		log.info("Saved successfully at {}", savingDestination);
	}

	@Override
	public int predict(float[] vector) {
		log.info("Predicting class for vector with {} dimensions", vector.length);
		Stopwatch predictionStopwatch = Stopwatch.createStarted();
		opencv_core.Mat reshapedMat = new opencv_core.Mat(vector).reshape(0, 1);
		float classVal = getClassifier().predict(reshapedMat, classes, opencv_ml.StatModel.RAW_OUTPUT);
		log.info("Classification completed in {}", predictionStopwatch);
		return (int) classVal;
	}

	@PreDestroy
	public void cleanUp() {
		classifier.close();
		classes.close();
	}

	private void loadTrainingExamples() {

	}

}
