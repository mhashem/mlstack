package co.rxstack.ml.common.model;

import com.google.common.base.MoreObjects;

public enum Recognizer {
	UNDEFINED("Undefined"),
	OPEN_CV("OpenCV"),
	AWS_REKOGNITION("AWS Rekognition"),
	COGNITIVE_SERVICES("Cognitive Services"),
	TENSORFLOW("Tensorflow");

	private String type;

	Recognizer(String v) {
		this.type = v;
	}

	public String getValue() {
		return type;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(Recognizer.class).add("type", type).toString();
	}
}
