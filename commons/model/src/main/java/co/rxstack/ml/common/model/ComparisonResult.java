package co.rxstack.ml.common.model;

/**
 * @author mhachem on 9/30/2017.
 */
public class ComparisonResult {

	private double confidence;

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	@Override
	public String toString() {
		return "ComparisonResult{" + "confidence=" + confidence + '}';
	}
}
