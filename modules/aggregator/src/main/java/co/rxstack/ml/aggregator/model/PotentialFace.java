package co.rxstack.ml.aggregator.model;

import java.awt.Rectangle;

import com.fasterxml.jackson.annotation.JsonManagedReference;

public class PotentialFace {

	private int label;
	private double confidence;

	@JsonManagedReference
	private final Rectangle box;

	private String labelString;

	public static PotentialFace newUnIdentifiedFace(Rectangle box) {
		return new PotentialFace(box, 0, Double.NaN);
	}

	public PotentialFace(Rectangle box, int label, double confidence) {
		this.box = box;
		this.label = label;
		this.confidence = confidence;
	}

	public int getLabel() {
		return label;
	}

	public void setLabel(int label) {
		this.label = label;
	}

	public Rectangle getBox() {
		return box;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	public String getLabelString() {
		return labelString;
	}

	public void setLabelString(String labelString) {
		this.labelString = labelString;
	}

	@Override
	public String toString() {
		return String.format("%s found at (%d,%d) with confidence = %s", label, box.x, box.y, confidence);
	}
}
