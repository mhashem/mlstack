package co.rxstack.ml.aggregator.experimental.model;

import com.google.common.base.MoreObjects;

public class PredictionResult {

	private Person person;
	private double confidence;

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(PredictionResult.class)
			.add("person", person).add("confidence", confidence).toString();
	}
}
