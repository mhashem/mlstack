package co.rxstack.ml.aggregator.service.impl;

import static org.bytedeco.javacpp.opencv_core.CV_STORAGE_WRITE;
import static org.bytedeco.javacpp.opencv_core.CV_TERMCRIT_ITER;
import static org.bytedeco.javacpp.opencv_ml.ROW_SAMPLE;
import static org.bytedeco.javacpp.opencv_ml.SVM;

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
import org.bytedeco.javacpp.opencv_core.CvFileStorage;
import org.bytedeco.javacpp.opencv_core.CvMemStorage;
import org.bytedeco.javacpp.opencv_core.CvTermCriteria;
import org.bytedeco.javacpp.opencv_core.FileStorage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_ml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

//@Component
public class ClassifierService implements IClassifierService<SVM> {

	private static final Logger log = LoggerFactory.getLogger(ClassifierService.class);

	private Mat classes;
	private SVM classifier;
	private ClassifierConfig classifierConfig;

	private AtomicBoolean classifierLoaded = new AtomicBoolean(false);

	//@Autowired
	public ClassifierService(ClassifierConfig classifierConfig) {
		this.classifierConfig = classifierConfig;
		this.classes = new Mat();
	}

	@PostConstruct
	public void init() {
		log.info("Initializing ClassifierService");
		load();
	}

	@Override
	public synchronized SVM getClassifier() {

		if (classifier == null || classifier.isNull()) {
			log.info("no loaded classifier found, initializing new classifier!");
			load();
		}

		try (CvTermCriteria cvTermCriteria = new CvTermCriteria(CV_TERMCRIT_ITER, 1000, 0.0001)) {
			classifier.setType(SVM.C_SVC);
			classifier.setKernel(SVM.LINEAR);
			classifier.setTermCriteria(cvTermCriteria.asTermCriteria());
		}

		if (!classifier.isTrained()) {
			log.warn("classifier is not trained!");
		}

		return classifier;
	}

	@Override
	public void load() {
		log.info("loading classifier at {}", classifierConfig.getClassifierPath());
		try {
			Path classifierPath = Paths.get(classifierConfig.getClassifierPath());
			if (!classifierPath.toFile().exists()) {
				log.warn("no classifier found at {}", classifierConfig.getClassifierPath());
				classifier = SVM.create();
			} else {
				classifier = SVM.load(classifierConfig.getClassifierPath());
			}
			classifierLoaded.set(true);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void train(Map<Integer, List<float[]>> idFeaturesListMap) {
		List<Integer> trainingLabels = new ArrayList<>();
		try (Mat trainingImages = new Mat(); Mat trainingData = new Mat();) {
			
			// mapping features list to matrices 
			
			idFeaturesListMap.keySet().forEach(id -> idFeaturesListMap.get(id).forEach(featuresArray -> {
				Mat featuresArrayMat = new Mat(featuresArray);
				featuresArrayMat = featuresArrayMat.reshape(0, 1);
				trainingImages.push_back(featuresArrayMat);
				trainingLabels.add(id);
			}));
			
			// mapping classes to vector

			int[] labelsArray = new int[trainingLabels.size()];
			for (int i = 0; i < trainingLabels.size(); ++i)
				labelsArray[i] = trainingLabels.get(i);
			try (Mat labelsArrayMat = new Mat(labelsArray)) {
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
		String fileName = classifierConfig.getClassifierNamePrefix() 
			+ LocalDateTime.now().format(DateTimeFormatter.ISO_DATE) + ".xml";
		String savingDestination = classifierConfig.getClassifierPath() + File.separator + fileName;
		CvFileStorage cvFileStorage = CvFileStorage.open(savingDestination, CvMemStorage.create(), CV_STORAGE_WRITE);
		FileStorage fileStorage = new FileStorage(cvFileStorage);
		classifier.write(fileStorage);
		cvFileStorage.release();
		fileStorage.release();
		log.info("Saved successfully at {}", savingDestination);
	}

	@Override
	public float predict(float[] vector) {
		log.info("predicting class for vector with {} dimensions", vector.length);
		Stopwatch predictionStopwatch = Stopwatch.createStarted();
		Mat reshapedMat = new Mat(vector).reshape(0, 1);
		float classVal = getClassifier().predict(reshapedMat, classes, opencv_ml.StatModel.RAW_OUTPUT);
		log.info("classification completed in {}", predictionStopwatch);
		return classVal;
	}

	@PreDestroy
	public void cleanUp() {
		classifier.close();
		classes.close();
	}

	private void loadTrainingExamples() {

	}

}
