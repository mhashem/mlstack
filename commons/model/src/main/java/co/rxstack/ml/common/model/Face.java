package co.rxstack.ml.common.model;

/**
 * @author mhachem on 9/27/2017.
 */
public class Face {

	private String faceId;
	private FaceRectangle faceRectangle;

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

	@Override
	public String toString() {
		return "Face{" + "faceId='" + faceId + '\'' + ", faceRectangle=" + faceRectangle + '}';
	}
}
