package co.rxstack.ml.aggregator.service.impl;

import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_face.createFisherFaceRecognizer;
import static org.bytedeco.javacpp.opencv_face.createLBPHFaceRecognizer;
import static org.slf4j.LoggerFactory.getLogger;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import co.rxstack.ml.aggregator.DatasetUtils;
import co.rxstack.ml.aggregator.config.FaceDBConfig;
import co.rxstack.ml.aggregator.model.PersonBundle;
import co.rxstack.ml.aggregator.model.PersonBundleStatistics;
import co.rxstack.ml.aggregator.model.PotentialFace;
import co.rxstack.ml.aggregator.service.IFaceExtractorService;
import co.rxstack.ml.aggregator.service.IFaceRecognitionService;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.tuple.Pair;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.FileStorage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_face.FaceRecognizer;
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.Java2DFrameUtils;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.slf4j.Logger;

public class FaceRecognitionService implements IFaceRecognitionService {

	private static final Logger logger = getLogger(FaceRecognitionService.class);

	private static final String MODEL_NAME_REG_EX =
		"model_[0-9]{4}-(((0[13578]|(10|12))-(0[1-9]|[1-2][0-9]|3[0-1]))|(02-(0[1-9]|[1-2][0-9]))|((0[469]|11)-(0[1-9]|[1-2][0-9]|30))).yml";

	private static final double THRESHOLD = 150d;
	private static final Pair<Integer, Integer> scale = Pair.of(100, 100);

	private FaceDBConfig faceDBConfig;
	private IFaceExtractorService faceExtractorService;

	private FaceRecognizer faceRecognizer;

	public FaceRecognitionService(FaceDBConfig faceDBConfig, IFaceExtractorService faceExtractorService) {
		this.faceDBConfig = faceDBConfig;
		this.faceExtractorService = faceExtractorService;

		this.faceRecognizer = createLBPHFaceRecognizer(1, 8, 8, 8, THRESHOLD);
		//this.faceRecognizer = createEigenFaceRecognizer();
		//this.faceRecognizer = createFisherFaceRecognizer(0, THRESHOLD);

		this.loadModel();
	}

	@Override
	public void loadModel() {
		try {
			Path modelsDir = Paths.get(faceDBConfig.getModelStoragePath());
			Optional<Path> lastFilePath;
			lastFilePath = Files.list(modelsDir).filter(f -> !Files.isDirectory(f))
				.filter(f -> f.toFile().getName().matches(MODEL_NAME_REG_EX))
				.max(Comparator.comparingLong(f -> f.toFile().lastModified()));
			if (lastFilePath.isPresent()) {
				logger.info("Loading model file: {}", lastFilePath.get().getFileName());
				faceRecognizer.load(lastFilePath.get().toFile().getAbsolutePath());
				logger.info("Model loaded successfully");
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
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

		try {
			List<Holder> holderList = Lists.newArrayList();
			for (PersonBundle personBundle : personBundleList) {
				List<Path> faceImagesPaths = personBundle.getFaceImagesPaths();
				for (Path faceImagesPath : faceImagesPaths) {

					/*Mat image = opencv_imgcodecs
						.imread(faceImagesPath.toFile().getAbsolutePath(),
						 opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE);*/

					BufferedImage bufferedImage = ImageIO.read(faceImagesPath.toFile());
					List<PotentialFace> potentialFaces = faceExtractorService.detectFaces(bufferedImage);
					if (!potentialFaces.isEmpty()) {
						logger.info("Detected {} face(s) in image -> {}", potentialFaces.size(),
							faceImagesPath.toAbsolutePath().toFile());
						logger.info("Potential faces: {}", potentialFaces);
						PotentialFace potentialFace = potentialFaces.get(0);
						Rectangle box = potentialFace.getBox();
						BufferedImage faceImage = bufferedImage.getSubimage(box.x, box.y, box.width, box.height);

						Mat convertedMat = Java2DFrameUtils.toMat(faceImage);
						Mat grayScaledMat = new Mat();
						Mat equalizedMat = new Mat();

						opencv_imgproc.cvtColor(convertedMat, grayScaledMat, opencv_imgproc.CV_BGR2GRAY);
						opencv_imgproc.equalizeHist(grayScaledMat, equalizedMat);

						Mat resizedImageMatrix = new Mat();
						opencv_imgproc.resize(equalizedMat, resizedImageMatrix, new opencv_core.Size(100, 100));

						/*Mat toWriteMatrix = resizedImageMatrix.clone();
						opencv_imgcodecs.imwrite("C:/etc/mlstack/misc/faces/"
							+ System.currentTimeMillis() + ".jpg", toWriteMatrix);*/

						Holder h = new Holder();
						h.faceId = personBundle.getPerson().getFaceId();
						h.imageMatrix = resizedImageMatrix;
						holderList.add(h);
					}
				}
			}

			MatVector imagesMatVector = new MatVector(holderList.size());
			Mat labels = new Mat(holderList.size(), 1, CV_32SC1);
			IntBuffer labelsBuf = labels.createBuffer();

			for (int i = 0; i < holderList.size(); i++) {
				imagesMatVector.put(i, holderList.get(i).imageMatrix);
				labelsBuf.put(i, holderList.get(i).faceId);
			}

			Stopwatch stopwatch = Stopwatch.createStarted();

			for (int i = 0; i < imagesMatVector.size(); i++) {
				logger.info("mat obj: {}", imagesMatVector.get(i).total());
			}

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

			loadModel();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	private class Holder {
		Integer faceId;
		Mat imageMatrix;
	}

	@Override
	public List<PotentialFace> predict(BufferedImage faceImage) {
		List<PotentialFace> potentialFaces = faceExtractorService.detectFaces(faceImage);
		return predict(faceImage, potentialFaces);
	}

	@Override
	public List<PotentialFace> predict(BufferedImage faceImage, List<PotentialFace> potentialFaces) {
		for (PotentialFace potentialFace : potentialFaces) {
			Rectangle faceBox = potentialFace.getBox();
			BufferedImage subImage = faceImage.getSubimage(faceBox.x, faceBox.y, faceBox.width, faceBox.height);
			IntPointer prediction = new IntPointer(1);
			DoublePointer confidence = new DoublePointer(1);

			opencv_core.IplImage iplImage = FaceExtractorService.toTinyGray(subImage, scale);
			faceRecognizer.predict(new Mat(iplImage), prediction, confidence);

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

	public BufferedImage grayscale(BufferedImage img) {
		for (int i = 0; i < img.getHeight(); i++) {
			for (int j = 0; j < img.getWidth(); j++) {
				Color c = new Color(img.getRGB(j, i));

				int red = (int) (c.getRed() * 0.299);
				int green = (int) (c.getGreen() * 0.587);
				int blue = (int) (c.getBlue() * 0.114);

				Color newColor = new Color(red + green + blue, red + green + blue, red + green + blue);

				img.setRGB(j, i, newColor.getRGB());
			}
		}

		return img;
	}

}
