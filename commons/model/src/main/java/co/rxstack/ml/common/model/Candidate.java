package co.rxstack.ml.common.model;

/**
 * @author mhachem on 10/4/2017.
 */
public class Candidate implements Comparable<Candidate> {

	private String personId;
	private double confidence;

	public Candidate() {
		// empty constructor
	}

	public Candidate(String personId, double confidence) {
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
	public int hashCode() {
		int result;
		long temp;
		result = personId.hashCode();
		temp = Double.doubleToLongBits(confidence);
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return obj != null && obj.getClass() == Candidate.class && this.compareTo((Candidate) obj) == 0
			&& this.personId.equals(((Candidate) obj).getPersonId());
	}

	@Override
	public int compareTo(Candidate candidate) {
		if (this.getConfidence() > candidate.getConfidence())
			return 1;
		if (this.getConfidence() < candidate.getConfidence()) 
			return -1;
		return 0;
	}
	
	@Override
	public String toString() {
		return "Candidates{" + "personId='" + personId + '\'' + ", confidence=" + confidence + '}';
	}
}
