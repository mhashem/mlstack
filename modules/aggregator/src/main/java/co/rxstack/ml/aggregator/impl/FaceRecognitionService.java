package co.rxstack.ml.aggregator.impl;

import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_face.createEigenFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;

import co.rxstack.ml.aggregator.DatasetUtils;
import co.rxstack.ml.aggregator.IFaceRecognitionService;
import co.rxstack.ml.aggregator.config.FaceDBConfig;
import co.rxstack.ml.aggregator.model.PersonBundle;
import co.rxstack.ml.aggregator.model.PersonBundleStatistics;
import co.rxstack.ml.aggregator.model.PredictionResult;

import com.google.common.base.Stopwatch;
import org.bytedeco.javacpp.opencv_core.FileStorage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_face.FaceRecognizer;
import org.slf4j.Logger;

public class FaceRecognitionService implements IFaceRecognitionService {

	private static final Logger logger = getLogger(FaceRecognitionService.class);

	private FaceDBConfig faceDBConfig;
	private FaceRecognizer faceRecognizer;

	public FaceRecognitionService(FaceDBConfig faceDBConfig) {
		this.faceDBConfig = faceDBConfig;
		this.faceRecognizer = createEigenFaceRecognizer();
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

				// todo add storage service for this
				String modelName = "model_" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE) + ".yml";
				FileStorage cvStorage = new FileStorage();
				cvStorage.open(faceDBConfig.getModelStoragePath() + "\\" + modelName, FileStorage.WRITE);
				logger.info("saving trained model {} to {}", modelName, faceDBConfig.getModelStoragePath());
				faceRecognizer.save(cvStorage);
				cvStorage.release();
			}
		}
	}

	@Override
	public PredictionResult predict(BufferedImage faceImage) {
		/*try (IntPointer label = new IntPointer(1); DoublePointer confidence = new DoublePointer(1)) {
			faceRecognizer.predict(img2Mat(faceImage), label, confidence);
		}*/

		// 1. load model
		// 2. predict
		// 3. return result

		return null;
	}

	/*public static Mat img2Mat(BufferedImage image) {
		byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		//Mat mat = new Mat(image.getHeight(), image.getWidth(), CV_8UC3);
		return imdecode(new MatOfByte(pixels), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
	}*/
}
