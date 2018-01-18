package co.rxstack.ml.aggregator;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.List;

import co.rxstack.ml.aggregator.experimental.model.PersonBundle;
import co.rxstack.ml.aggregator.experimental.model.PredictionResult;

public interface IFaceRecognitionService {

	void loadModel(String modelName);

	void trainModel(Path dataSetPath);
	
	void trainModel(List<PersonBundle> personBundleList);

	PredictionResult predict(BufferedImage faceImage);

}
