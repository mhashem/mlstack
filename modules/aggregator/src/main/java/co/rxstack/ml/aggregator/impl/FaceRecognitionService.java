package co.rxstack.ml.aggregator.impl;

import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_face.createEigenFaceRecognizer;
import static org.bytedeco.javacpp.opencv_face.createFisherFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import co.rxstack.ml.aggregator.DatasetUtils;
import co.rxstack.ml.aggregator.IFaceExtractorService;
import co.rxstack.ml.aggregator.IFaceRecognitionService;
import co.rxstack.ml.aggregator.config.FaceDBConfig;
import co.rxstack.ml.aggregator.model.PersonBundle;
import co.rxstack.ml.aggregator.model.PersonBundleStatistics;
import co.rxstack.ml.aggregator.model.PotentialFace;

import com.google.common.base.Stopwatch;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.opencv_core.FileStorage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_face.FaceRecognizer;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.slf4j.Logger;

public class FaceRecognitionService implements IFaceRecognitionService {

	private static final Logger logger = getLogger(FaceRecognitionService.class);

	private static final double THRESHOLD = 150d;

	private FaceDBConfig faceDBConfig;
	private IFaceExtractorService faceExtractorService;

	private FaceRecognizer faceRecognizer;

	public FaceRecognitionService(FaceDBConfig faceDBConfig, IFaceExtractorService faceExtractorService) {
		this.faceDBConfig = faceDBConfig;
		this.faceExtractorService = faceExtractorService;

		//this.faceRecognizer = createEigenFaceRecognizer();
		this.faceRecognizer = createFisherFaceRecognizer();
	}

	@Override
	public void loadModel(String modelName) {
		faceRecognizer.load(modelName);
	}

	@Override
	public void trainModel() {
		trainModel(Paths.get(faceDBConfig.getFaceDbPath()));
	}

	@Override
	public void trainModel(Path dataSetPath) {
		try {
			List<PersonBundle> personBundles =
				DatasetUtils.loadPersonBundleList(dataSetPath, faceDBConfig.getFaceDirectoryNameDelimiter());
			trainModel(personBundles);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	@Override
	public void trainModel(List<PersonBundle> personBundleList) {
		logger.info("training new face recognizer model");

		PersonBundleStatistics statistics = PersonBundleStatistics.fromBundle(personBundleList);
		logger.info("DataSet statistics: {}", statistics);

		try (MatVector imagesMatVector = new MatVector(statistics.getImagesCount());
			Mat labels = new Mat(statistics.getImagesCount(), 1, CV_32SC1)) {
			int counter = 0;
			IntBuffer labelsBuf = labels.createBuffer();
			for (PersonBundle personBundle : personBundleList) {
				List<Path> faceImagesPaths = personBundle.getFaceImagesPaths();
				for (Path faceImagesPath : faceImagesPaths) {
					Mat img = imread(faceImagesPath.toAbsolutePath().toString(), CV_LOAD_IMAGE_GRAYSCALE);
					imagesMatVector.put(counter, img);
					labelsBuf.put(counter, personBundle.getPerson().getFaceId());
					counter++;
				}

				Stopwatch stopwatch = Stopwatch.createStarted();
				logger.info("training face recognizer started");
				faceRecognizer.train(imagesMatVector, labels);
				logger.info("training face recognizer finished successfully in {} ms",
					stopwatch.elapsed(TimeUnit.MILLISECONDS));


				File modelStorageDir = Paths.get(faceDBConfig.getModelStoragePath()).toFile();
				if (!modelStorageDir.exists()) {
					modelStorageDir.mkdir();
				}
				
				// todo add storage service for this
				String modelName = "model_" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE) + ".yml";
				FileStorage cvStorage = new FileStorage();
				cvStorage.open(faceDBConfig.getModelStoragePath() + File.separator + modelName, FileStorage.WRITE);
				logger.info("saving trained model {} to {}", modelName, faceDBConfig.getModelStoragePath());
				faceRecognizer.save(cvStorage);
				cvStorage.release();
			}
		}
	}

	@Override
	public List<PotentialFace> predict(BufferedImage faceImage) {
		List<PotentialFace> potentialFaces = faceExtractorService.detectFaces(faceImage);
		for (PotentialFace potentialFace : potentialFaces) {
			Rectangle faceBox = potentialFace.getBox();
			BufferedImage subImage = faceImage.getSubimage(faceBox.x, faceBox.y, faceBox.width, faceBox.height);
			IntPointer prediction = new IntPointer(1);
			DoublePointer confidence = new DoublePointer(1);
			faceRecognizer.predict(toMat(subImage), prediction, confidence);

			int label = prediction.get(0);
			double confidenceVal = confidence.get(0);

			potentialFace.setLabel(label);
			potentialFace.setConfidence(100 * (THRESHOLD - confidenceVal) / THRESHOLD);
		}
		logger.info("Prediction result: {}", potentialFaces);
		return potentialFaces;
	}

	/**
	 *
	 * @param img {@link BufferedImage}
	 * @return {@link Mat}
	 */
	private Mat toMat(BufferedImage img) {
		OpenCVFrameConverter.ToIplImage cv = new OpenCVFrameConverter.ToIplImage();
		Java2DFrameConverter jcv = new Java2DFrameConverter();
		return cv.convertToMat(jcv.convert(img));
	}
}
