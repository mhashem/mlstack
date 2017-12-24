package co.rxstack.ml.common.model;

/**
 * @author mhachem on 9/27/2017.
 */
public class FaceDetectionResult {
	
	private String faceId;
	private FaceRectangle faceRectangle;
	private FaceAttributes faceAttributes;
	private byte[] imageBytes;

	public String getFaceId() {
		return faceId;
	}

	public void setFaceId(String faceId) {
		this.faceId = faceId;
	}

	public FaceRectangle getFaceRectangle() {
		return faceRectangle;
	}

	public void setFaceRectangle(FaceRectangle faceRectangle) {
		this.faceRectangle = faceRectangle;
	}

	public FaceAttributes getFaceAttributes() {
		return faceAttributes;
	}

	public void setFaceAttributes(FaceAttributes faceAttributes) {
		this.faceAttributes = faceAttributes;
	}

	public byte[] getImageBytes() {
		return imageBytes;
	}

	public void setImageBytes(byte[] imageBytes) {
		this.imageBytes = imageBytes;
	}

	@Override
	public String toString() {
		return "FaceDetectionResult{" + "faceId='" + faceId + '\'' + ", faceRectangle=" + faceRectangle
			+ ", faceAttributes=" + faceAttributes + '}';
	}
}
