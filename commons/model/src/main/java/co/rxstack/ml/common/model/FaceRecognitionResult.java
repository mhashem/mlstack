package co.rxstack.ml.common.model;

public class FaceRecognitionResult {

	private int index;
	private int faceId;
	private int identityId;
	private String label;
	private double confidence;
	private FaceRectangle faceRectangle;
	private String recognizer;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getFaceId() {
		return faceId;
	}

	public void setFaceId(int faceId) {
		this.faceId = faceId;
	}

	public int getIdentityId() {
		return identityId;
	}

	public void setIdentityId(int identityId) {
		this.identityId = identityId;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	public FaceRectangle getFaceRectangle() {
		return faceRectangle;
	}

	public void setFaceRectangle(FaceRectangle faceRectangle) {
		this.faceRectangle = faceRectangle;
	}

	public String getRecognizer() {
		return recognizer;
	}

	public void setRecognizer(String recognizer) {
		this.recognizer = recognizer;
	}

	@Override
	public String toString() {
		return "FaceRecognitionResult{" + "label='" + label + '\'' + ", confidence=" + confidence + ", faceRectangle="
			+ faceRectangle + '}';
	}

	public static FaceRecognitionResult.Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private FaceRecognitionResult faceRecognitionResult;

		public Builder() {
			faceRecognitionResult = new FaceRecognitionResult();
		}

		public Builder index(int index) {
			faceRecognitionResult.setIndex(index);
			return this;
		}

		public Builder label(String label) {
			faceRecognitionResult.setLabel(label);
			return this;
		}

		public Builder confidence(double confidence) {
			faceRecognitionResult.setConfidence(confidence);
			return this;
		}

		public Builder faceId(int faceId) {
			faceRecognitionResult.setFaceId(faceId);
			return this;
		}

		public Builder identityId(int identityId) {
			faceRecognitionResult.setIdentityId(identityId);
			return this;
		}

		public Builder faceRectangle(FaceRectangle faceRectangle) {
			faceRecognitionResult.setFaceRectangle(faceRectangle);
			return this;
		}

		public Builder recognizer(String recognizer) {
			faceRecognitionResult.setRecognizer(recognizer);
			return this;
		}

		public FaceRecognitionResult build() {
			return faceRecognitionResult;
		}
	}

}
