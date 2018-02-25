package co.rxstack.ml.aggregator.experimental;

import static org.bytedeco.javacpp.opencv_core.Mat;
import static org.bytedeco.javacpp.opencv_core.NORM_MINMAX;
import static org.bytedeco.javacpp.opencv_core.Size;
import static org.bytedeco.javacpp.opencv_core.normalize;
import static org.bytedeco.javacpp.opencv_dnn.Blob;
import static org.bytedeco.javacpp.opencv_dnn.Importer;
import static org.bytedeco.javacpp.opencv_dnn.Net;
import static org.bytedeco.javacpp.opencv_dnn.createCaffeImporter;
import static org.bytedeco.javacpp.opencv_imgproc.resize;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.bytedeco.javacpp.indexer.Indexer;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class responsible for recognizing gender. This class use the concept of CNN (Convolution Neural Networks) to
 * identify the gender of a detected face.
 *
 * @author Imesha Sudasingha
 */
public class CNNGenderDetector {

	private static final Logger logger = LoggerFactory.getLogger(CNNGenderDetector.class);

	private Net genderNet;

	public CNNGenderDetector() {
		try {
			genderNet = new Net();
			File protobuf = new File(getClass().getResource("/caffe/deploy_gendernet.prototxt").toURI());
			File caffeModel = new File(getClass().getResource("/caffe/gender_net.caffemodel").toURI());
			Importer importer = createCaffeImporter(protobuf.getAbsolutePath(), caffeModel.getAbsolutePath());
			importer.populateNet(genderNet);
			importer.close();
		} catch (Exception e) {
			logger.error("Error reading prototxt", e);
			throw new IllegalStateException("Unable to start CNNGenderDetector", e);
		}
	}

	/**
	 * Predicts gender of a given cropped face
	 *
	 * @param face  the cropped face as a {@link Mat}
	 * @param frame the original frame where the face was cropped from
	 * @return Gender
	 */
	public Gender predictGender(Mat face, Frame frame) {
		try {
			Mat croppedMat = new Mat();
			resize(face, croppedMat, new Size(256, 256));
			normalize(croppedMat, croppedMat, 0, 4, NORM_MINMAX, -1, null);

			Blob inputBlob = Blob.fromImages(croppedMat);
			genderNet.setBlob(".data", inputBlob);
			genderNet.forward();
			Blob prob = genderNet.getBlob("prob");

			Indexer indexer = prob.matRefConst().createIndexer();
			logger.debug("CNN results {},{}", indexer.getDouble(0, 0), indexer.getDouble(0, 1));
			if (indexer.getDouble(0, 0) > indexer.getDouble(0, 1)) {
				logger.debug("Male detected");
				return Gender.MALE;
			} else {
				logger.debug("Female detected");
				return Gender.FEMALE;
			}
		} catch (Exception e) {
			logger.error("Error when processing gender", e);
		}
		return Gender.NOT_RECOGNIZED;
	}

	public enum Gender {
		MALE, FEMALE, NOT_RECOGNIZED
	}

	public static void main(String[] args) throws IOException {
		BufferedImage image = ImageIO.read(CNNGenderDetector.class.getClassLoader().getResourceAsStream("boy-1.jpg"));
		CNNGenderDetector genderDetector = new CNNGenderDetector();
		Gender g = genderDetector.predictGender(Java2DFrameUtils.toMat(image), Java2DFrameUtils.toFrame(image));
		System.out.println(g.toString());
	}
}
