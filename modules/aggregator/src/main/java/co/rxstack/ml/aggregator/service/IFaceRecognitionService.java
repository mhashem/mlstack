package co.rxstack.ml.aggregator.service;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.List;

import co.rxstack.ml.aggregator.model.PersonBundle;
import co.rxstack.ml.aggregator.model.PotentialFace;

public interface IFaceRecognitionService {

	void loadModel();

	void trainModel();

	void trainModel(Path dataSetPath);
	
	void trainModel(List<PersonBundle> personBundleList);

	List<PotentialFace> predict(BufferedImage faceImage);

	List<PotentialFace> predict(BufferedImage bufferedImage, List<PotentialFace> detectedFaces);
}
