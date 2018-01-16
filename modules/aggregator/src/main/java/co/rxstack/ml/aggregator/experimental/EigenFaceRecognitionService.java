package co.rxstack.ml.aggregator.experimental;

import static org.bytedeco.javacpp.opencv_core.CV_32SC1;
import static org.bytedeco.javacpp.opencv_face.createEigenFaceRecognizer;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import co.rxstack.ml.aggregator.IFaceRecognitionService;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_face.FaceRecognizer;

public class EigenFaceRecognitionService implements IFaceRecognitionService {

	private FaceRecognizer faceRecognizer;

	public EigenFaceRecognitionService() {
		faceRecognizer = createEigenFaceRecognizer();
	}

	@Override
	public void loadModel(String modelName) {
		// todo pass byte pointer instead of modelName ! how can we load it?
		faceRecognizer.load(modelName);
		// todo use loader strategy class for multiple storage options
	}

	@Override
	public void trainModel(List<PersonBundle> personBundleList) {
		// todo pass a model object instead of map!

//		personBundleList.stream().map(personBundle -> {
//
//			MatVector images = new MatVector(personBundle.getFaceList().size());
//
//
//
//			personBundle.get
//
//			// Imgcodecs.imdecode(new MatOfByte(byteArrayOutputStream.toByteArray()), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
//
//		});
//
//		// MatVector images = (MatVector) dataMap.get("images_vector");
//		// Mat labels = (Mat) dataMap.get("labels");
//		faceRecognizer.train(images, labels);
	}

	@Override
	public PredictionResult predict(BufferedImage faceImage) {



		return null;
	}
}
