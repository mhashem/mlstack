package co.rxstack.ml.aggregator.experimental;

import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_face.createEigenFaceRecognizer;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

import co.rxstack.ml.aggregator.IFaceRecognitionService;
import co.rxstack.ml.aggregator.experimental.config.FaceDBConfig;
import co.rxstack.ml.aggregator.experimental.model.PersonBundle;
import co.rxstack.ml.aggregator.experimental.model.PersonBundleStatistics;
import co.rxstack.ml.aggregator.experimental.model.PredictionResult;
import com.google.common.base.Stopwatch;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_face.FaceRecognizer;
import org.slf4j.Logger;

public class EigenFaceRecognitionService implements IFaceRecognitionService {

	private static final Logger logger = getLogger(EigenFaceRecognitionService.class);

	private FaceDBConfig faceDBConfig;
	private FaceRecognizer faceRecognizer;

	public EigenFaceRecognitionService(FaceDBConfig faceDBConfig) {
		this.faceDBConfig = faceDBConfig;
		this.faceRecognizer = createEigenFaceRecognizer();
	}

	@Override
	public void loadModel(String modelName) {
		// todo pass byte pointer instead of modelName ! how can we load it?
		faceRecognizer.load(modelName);
		// todo use loader strategy class for multiple storage options
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
		logger.info("Dataset statistics: {}", statistics);

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

				// todo save model to cloud or disk
				// todo add storage service for this 
				// todo save with name model_<date>.yml 
				faceRecognizer.save("");
			}

		}

	}

	@Override
	public PredictionResult predict(BufferedImage faceImage) {
		/*try (IntPointer label = new IntPointer(1); DoublePointer confidence = new DoublePointer(1)) {
			faceRecognizer.predict(img2Mat(faceImage), label, confidence);
		}*/

		return null;
	}

	/*public static Mat img2Mat(BufferedImage image) {
		byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
		//Mat mat = new Mat(image.getHeight(), image.getWidth(), CV_8UC3);
		return imdecode(new MatOfByte(pixels), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
	}*/
}
