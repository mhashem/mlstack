package co.rxstack.ml.aggregator.model;

import java.awt.Rectangle;

public class PotentialFace {

	private int label;
	private double confidence;
	private final Rectangle box;

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

	@Override
	public String toString() {
		return String.format("%s found at (%d,%d) with confidence = %s", label, box.x, box.y, confidence);
	}
}
