package co.rxstack.ml.aws.rekognition.model;

import java.io.Serializable;

public class Face implements Serializable {

	private String faceId;
	private BoundingBox boundingBox;
	private String imageId;
	private String externalImageId;
	private Float confidence;

	public String getFaceId() {
		return faceId;
	}

	public void setFaceId(String faceId) {
		this.faceId = faceId;
	}

	public BoundingBox getBoundingBox() {
		return boundingBox;
	}

	public void setBoundingBox(BoundingBox boundingBox) {
		this.boundingBox = boundingBox;
	}

	public String getImageId() {
		return imageId;
	}

	public void setImageId(String imageId) {
		this.imageId = imageId;
	}

	public String getExternalImageId() {
		return externalImageId;
	}

	public void setExternalImageId(String externalImageId) {
		this.externalImageId = externalImageId;
	}

	public Float getConfidence() {
		return confidence;
	}

	public void setConfidence(Float confidence) {
		this.confidence = confidence;
	}

	@Override
	public String toString() {
		return "Face{" + "faceId='" + faceId + '\'' + ", boundingBox=" + boundingBox + ", imageId='" + imageId + '\''
			+ ", externalImageId='" + externalImageId + '\'' + ", confidence=" + confidence + '}';
	}
}
