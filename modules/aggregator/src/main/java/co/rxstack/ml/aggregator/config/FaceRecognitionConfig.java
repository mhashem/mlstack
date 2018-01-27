package co.rxstack.ml.aggregator.config;

import co.rxstack.ml.aggregator.model.FaceRecognitionAlgorithm;

public class FaceRecognitionConfig {

	private FaceRecognitionAlgorithm faceRecognitionAlgorithm;

	public FaceRecognitionAlgorithm getFaceRecognitionAlgorithm() {
		return faceRecognitionAlgorithm;
	}

	public void setFaceRecognitionAlgorithm(FaceRecognitionAlgorithm faceRecognitionAlgorithm) {
		this.faceRecognitionAlgorithm = faceRecognitionAlgorithm;
	}
}
