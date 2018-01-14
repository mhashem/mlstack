package co.rxstack.ml.aws.rekognition.model;

import com.google.common.base.MoreObjects;

public class FaceIndexingResult {

	private Face face;
	private float confidence;

	public void setConfidence(float confidence) {
		this.confidence = confidence;
	}

	public float getConfidence() {
		return confidence;
	}

	public Face getFace() {
		return face;
	}

	public void setFace(Face face) {
		this.face = face;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(FaceIndexingResult.class)
			.add("face", face)
			.add("confidence", confidence).toString();
	}
}
