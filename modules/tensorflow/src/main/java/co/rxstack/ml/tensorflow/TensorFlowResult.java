package co.rxstack.ml.tensorflow;

import java.io.Serializable;

import co.rxstack.ml.common.model.FaceBox;
import com.google.common.base.MoreObjects;

public class TensorFlowResult implements Serializable {

	private String label;
	private double confidence;
	private FaceBox faceBox;

	public TensorFlowResult(String label, double confidence) {
		this.label = label;
		this.confidence = confidence;
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
		return MoreObjects.toStringHelper(TensorFlowResult.class.getSimpleName()).add("label", label)
			.add("confidence", confidence).add("faceBox", faceBox).toString();
	}
}
