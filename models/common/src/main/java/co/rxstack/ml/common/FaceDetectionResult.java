package co.rxstack.ml.common;

/**
 * @author mhachem on 9/27/2017.
 */
public class FaceDetectionResult {
	
	private String faceId;
	private FaceRectangle faceRectangle;
	private FaceAttributes faceAttributes;

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

	@Override
	public String toString() {
		return "FaceDetectionResult{" + "faceId='" + faceId + '\'' + ", faceRectangle=" + faceRectangle
			+ ", faceAttributes=" + faceAttributes + '}';
	}
}
