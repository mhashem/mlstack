package co.rxstack.ml.common.model;

/**
 * @author mhachem on 10/4/2017.
 */
public class Candidates {

	private String personId;
	private double confidence;

	public Candidates() {
		// empty constructor
	}

	public Candidates(String personId, double confidence) {
		this.personId = personId;
		this.confidence = confidence;
	}

	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	@Override
	public String toString() {
		return "Candidates{" + "personId='" + personId + '\'' + ", confidence=" + confidence + '}';
	}
}
