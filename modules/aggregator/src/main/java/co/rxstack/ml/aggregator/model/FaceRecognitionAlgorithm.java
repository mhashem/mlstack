package co.rxstack.ml.aggregator.model;

import java.util.Arrays;
import java.util.Optional;

public enum FaceRecognitionAlgorithm {

	EIGEN("eigen"), FISHER("fisher"), LBPH("lbph");

	private String algorithm;

	FaceRecognitionAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public static Optional<FaceRecognitionAlgorithm> fromString(String value) {
		return Arrays.stream(FaceRecognitionAlgorithm.values())
			.filter(faceRecognitionAlgorithm -> faceRecognitionAlgorithm.getAlgorithm().equalsIgnoreCase(value))
			.findAny();
	}

}
