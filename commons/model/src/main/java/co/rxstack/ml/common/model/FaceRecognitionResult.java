package co.rxstack.ml.common.model;

public class FaceRecognitionResult {

	private int index;
	private String faceId;
	private double confidence;
	private FaceDetectionResult faceDetectionResult;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getFaceId() {
		return faceId;
	}

	public void setFaceId(String faceId) {
		this.faceId = faceId;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	public FaceDetectionResult getFaceDetectionResult() {
		return faceDetectionResult;
	}

	public void setFaceDetectionResult(FaceDetectionResult faceDetectionResult) {
		this.faceDetectionResult = faceDetectionResult;
	}
}
