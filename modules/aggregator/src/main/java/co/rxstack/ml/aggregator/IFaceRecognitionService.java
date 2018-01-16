package co.rxstack.ml.aggregator;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import co.rxstack.ml.aggregator.experimental.PersonBundle;
import co.rxstack.ml.aggregator.experimental.PredictionResult;

public interface IFaceRecognitionService {

	void loadModel(String modelName);

	void trainModel(List<PersonBundle> personBundleList);

	PredictionResult predict(BufferedImage faceImage);

}
