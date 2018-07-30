package co.rxstack.ml.tensorflow;

import java.io.Serializable;

import co.rxstack.ml.common.model.FaceBox;
import co.rxstack.ml.common.model.FaceRectangle;

import com.google.common.base.MoreObjects;

public class TensorFlowResult implements Serializable {

	private int faceId;
	private String label;
	private double confidence;
	private FaceBox faceBox;

	public TensorFlowResult(int faceId, double confidence) {
		this.faceId = faceId;
		this.confidence = confidence;
	}
	
	public TensorFlowResult(String label, double confidence) {
		this.label = label;
		this.confidence = confidence;
	}

	public TensorFlowResult(int faceId, String label, double confidence) {
		this(faceId, confidence);
		this.label = label;
	}

	public int getFaceId() {
		return faceId;
	}

	public void setFaceId(int faceId) {
		this.faceId = faceId;
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

	public FaceBox getFaceBox() {
		return faceBox;
	}

	public void setFaceBox(FaceBox faceBox) {
		this.faceBox = faceBox;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(TensorFlowResult.class.getSimpleName()).add("faceId", faceId).add("label", label)
			.add("confidence", confidence).add("faceBox", faceBox).toString();
	}
}
