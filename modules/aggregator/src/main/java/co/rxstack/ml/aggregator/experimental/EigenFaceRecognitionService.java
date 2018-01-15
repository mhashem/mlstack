package co.rxstack.ml.aggregator.experimental;

import static org.bytedeco.javacpp.opencv_face.createEigenFaceRecognizer;

import java.util.Map;

import co.rxstack.ml.aggregator.IFaceRecognitionService;

import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_face;
import org.bytedeco.javacpp.opencv_face.FaceRecognizer;

public class EigenFaceRecognitionService implements IFaceRecognitionService<String, String> {

	private FaceRecognizer faceRecognizer;

	public EigenFaceRecognitionService() {
		faceRecognizer = createEigenFaceRecognizer();
	}

	@Override
	public void loadModel(String modelName) {
		// todo pass byte pointer instead of modelName ! how can we load it?
		faceRecognizer.load(modelName);
	}

	@Override
	public void trainModel(Map<String, Object> dataMap) {
		// todo pass a model object instead of map!
		MatVector images = (MatVector) dataMap.get("images_vector");
		Mat labels = (Mat) dataMap.get("labels");
		faceRecognizer.train(images, labels);
	}

	@Override
	public String predict(String record) {
		return null;
	}
}
