package co.rxstack.ml.common.model;

import com.google.common.base.MoreObjects;

public enum Recognizer {
	UNKNOWN("unknown"),
	OPEN_CV("open_cv"),
	AWS_REKOGNITION("aws_rekognition"),
	COGNITIVE_SERVICES("cognitive_services"),
	TENSOR_FLOW_FACE_NET("tensorflow_face_net");

	private String type;

	Recognizer(String v) {
		this.type = v;
	}

	public String getType() {
		return type;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(Recognizer.class).add("type", type).toString();
	}
}
