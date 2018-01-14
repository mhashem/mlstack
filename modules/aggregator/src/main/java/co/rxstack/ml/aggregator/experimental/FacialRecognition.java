package co.rxstack.ml.aggregator.experimental;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.tuple.Pair;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_face;
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;

public class FacialRecognition {

	/**
	 * Result returned by FacialRecognition
	 */
	public static class PotentialFace {
		public final Rectangle box;
		public String name;        // name of person at box - null if unidentified
		public double confidence;  // confidence that face at box is name - NaN if name is null

		static PotentialFace newUnidentifiedFace(Rectangle box) {
			return new PotentialFace(box, null, Double.NaN);
		}

		PotentialFace(Rectangle box, String name, double confidence) {
			this.box = box;
			this.name = name;
			this.confidence = confidence;
		}

		@Override
		public String toString() {
			return String.format("%s found at (%d,%d) with confidence = %s", name, box.x, box.y, confidence);
		}
	}

	/**
	 * Does facial recognition (or detection only if db is null or empty)
	 */
	public static synchronized List<PotentialFace> run(BufferedImage image, FaceDb db) {
		final List<PotentialFace> faces = detectFaces(image);

		if (db != null && db.size() > 0) {
			if (!trainingCache.containsKey(db)) {
				System.out.println("Training ...");
				trainingCache.put(db, new Training(db));
			}
			final Training training = trainingCache.get(db);

			for (PotentialFace face : faces) {
				training.identify(image, face);
			}
		}

		return faces;
	}

	private static Map<FaceDb, Training> trainingCache = Maps.newConcurrentMap();

	static void invalidateTrainingCache(FaceDb db) {
		trainingCache.remove(db);
	}

	private FacialRecognition() {
		// no one can construct me - I only have one public static method - run
	}

	private static class Training {
		// We can try out different algorithms here: http://docs.opencv.org/trunk/modules/contrib/doc/facerec/facerec_api.html
		private static final Double THRESHHOLD = 150d;
		private static final opencv_face.FaceRecognizer ALGO_FACTORY =
			opencv_face.createLBPHFaceRecognizer(1, 8, 8, 8, THRESHHOLD);
		//com.googlecode.javacv.cpp.opencv_contrib.createFisherFaceRecognizer(0, THRESHHOLD);
		//com.googlecode.javacv.cpp.opencv_contrib.createEigenFaceRecognizer(0, THRESHHOLD);
		private static final Pair<Integer, Integer> scale = Pair.of(100, 100);

		private final String[] names;
		private final opencv_face.FaceRecognizer algorithm;

		/**
		 * Creating new trainings are VERY expensive and should be always cached
		 * http://stackoverflow.com/questions/11913980/
		 */
		Training(FaceDb db) {
			final int numberOfImages = db.size();
			final Set<String> namesInDb = db.names();
			this.names = new String[namesInDb.size()];
			final opencv_core.MatVector images = new opencv_core.MatVector(numberOfImages);
			final opencv_core.CvMat labels = opencv_core.cvCreateMat(1, numberOfImages, opencv_core.CV_32SC1);

			int imgCount = 0, personCount = 0;
			for (String name : namesInDb) {
				for (BufferedImage image : db.get(name)) {
					opencv_core.Mat mat = new opencv_core.Mat(toTinyGray(image, scale));
					images.put(imgCount, mat);
					labels.put(imgCount, personCount);
					imgCount++;
				}
				names[personCount++] = name;
			}

			this.algorithm = ALGO_FACTORY;
			//algorithm.train(images, labels);
		}

		/**
		 * Identify the face in bounding box r in image
		 */
		void identify(BufferedImage image, PotentialFace face) {
			final Rectangle r = face.box;
			final BufferedImage candidate = image.getSubimage(r.x, r.y, r.width, r.height);
			final opencv_core.IplImage iplImage = toTinyGray(candidate, scale);
			final int[] prediction = new int[1];
			final double[] confidence = new double[1];
			//algorithm.predict(iplImage, prediction, confidence);
			if (prediction[0] >= 0 && prediction[0] < names.length) {
				face.name = names[prediction[0]];
				//face.confidence = 100*(THRESHHOLD - confidence[0])/THRESHHOLD;
				face.confidence = confidence[0];
			}
		}
	}

	private static final opencv_core.CvMemStorage storage = opencv_core.CvMemStorage.create();
	private static final int F = 4; // scaling factor
	private static final opencv_objdetect.CvHaarClassifierCascade classifier;

	static {
		final File classifierFile;
		try {
			classifierFile = Loader.extractResource("haarcascade_frontalface_alt.xml", null, "classifier", ".xml");
			classifier =
				new opencv_objdetect.CvHaarClassifierCascade(opencv_core.cvLoad(classifierFile.getAbsolutePath()));
		} catch (IOException e) {
			Throwables.throwIfUnchecked(e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * This does facial detection and NOT facial recognition
	 */
	private static synchronized List<PotentialFace> detectFaces(BufferedImage image) {
		opencv_core.cvClearMemStorage(storage);
		final opencv_core.IplImage iplImage = toTinyGray(image, null);
		final opencv_core.CvSeq cvSeq = opencv_objdetect
			.cvHaarDetectObjects(iplImage, classifier, storage, 1.1, 3, opencv_objdetect.CV_HAAR_DO_CANNY_PRUNING);
		final int N = cvSeq.total();
		final List<PotentialFace> ret = Lists.newArrayListWithCapacity(N);
		for (int i = 0; i < N; i++) {
			final opencv_core.CvRect r = new opencv_core.CvRect(opencv_core.cvGetSeqElem(cvSeq, i));
			final Rectangle box = new Rectangle(r.x() * F, r.y() * F, r.width() * F, r.height() * F);
			ret.add(PotentialFace.newUnidentifiedFace(box));
		}
		return ret;
	}

	/**
	 * Images should be grayscaled and scaled-down for faster calculations
	 */
	private static opencv_core.IplImage toTinyGray(BufferedImage image, Pair<Integer, Integer> scale) {
		OpenCVFrameConverter.ToIplImage toIplImage = new OpenCVFrameConverter.ToIplImage();
		Java2DFrameConverter java2DFrameConverter = new Java2DFrameConverter();
		final opencv_core.IplImage iplImage =
			new opencv_core.IplImage(toIplImage.convert(java2DFrameConverter.getFrame(image)));
		if (scale == null) {
			scale = Pair.of(iplImage.width() / F, iplImage.height() / F);
		}
		final opencv_core.IplImage gray =
			opencv_core.IplImage.create(iplImage.width(), iplImage.height(), opencv_core.IPL_DEPTH_8U, 1);
		final opencv_core.IplImage tiny =
			opencv_core.IplImage.create(scale.getLeft(), scale.getRight(), opencv_core.IPL_DEPTH_8U, 1);
		opencv_imgproc.cvCvtColor(iplImage, gray, opencv_imgproc.CV_BGR2GRAY);   //todo: do tiny before gray
		opencv_imgproc.cvResize(gray, tiny, opencv_imgproc.CV_INTER_AREA);
		opencv_imgproc.cvEqualizeHist(tiny, tiny);
		return tiny;
	}
}
