package co.rxstack.ml.tensorflow;

import java.io.Serializable;

public class TensorFlowResult implements Serializable {

	private String label;
	private double confidence;

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

	@Override
	public String toString() {
		return "TensorFlowResult{" + "label='" + label + '\'' + ", confidence=" + confidence + '}';
	}
}
