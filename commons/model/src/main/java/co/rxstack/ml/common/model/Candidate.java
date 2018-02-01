package co.rxstack.ml.common.model;

import com.google.common.base.MoreObjects;

/**
 * @author mhachem on 10/4/2017.
 */
public class Candidate implements Comparable<Candidate> {

	private String personId;
	private String dbPersonId;
	private double confidence;
	private FaceRectangle faceRectangle;

	private Recognizer recognizer = Recognizer.UNKNOWN;

	public Candidate() {
		// empty constructor
	}

	public Candidate(String personId, double confidence) {
		this.personId = personId;
		this.confidence = confidence;
	}

	public Candidate(String personId, double confidence, FaceRectangle faceRectangle,
		Recognizer recognizer) {
		this.personId = personId;
		this.confidence = confidence;
		this.faceRectangle = faceRectangle;
		this.recognizer = recognizer;
	}

	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	public String getDbPersonId() {
		return dbPersonId;
	}

	public void setDbPersonId(String dbPersonId) {
		this.dbPersonId = dbPersonId;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	public FaceRectangle getFaceRectangle() {
		return faceRectangle;
	}

	public void setFaceRectangle(FaceRectangle faceRectangle) {
		this.faceRectangle = faceRectangle;
	}

	public Recognizer getRecognizer() {
		return recognizer;
	}

	public void setRecognizer(Recognizer recognizer) {
		this.recognizer = recognizer;
	}

	@Override
	public int hashCode() {
		int result;
		long temp;
		result = getPersonId().hashCode();
		temp = Double.doubleToLongBits(getConfidence());
		result = 31 * result + (int) (temp ^ (temp >>> 32));
		result = 31 * result + (faceRectangle != null ? faceRectangle.hashCode() : 0);
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Candidate candidate = (Candidate) o;

		if (Double.compare(candidate.getConfidence(), getConfidence()) != 0) return false;
		if (getPersonId() != null ? !getPersonId().equals(candidate.getPersonId()) : candidate.getPersonId() != null)
			return false;
		return faceRectangle != null ? faceRectangle.equals(candidate.faceRectangle) : candidate.faceRectangle == null;
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
		return MoreObjects.toStringHelper(Candidate.class).add("personId", getPersonId())
			.add("confidence", getConfidence()).add("faceRectangle", getFaceRectangle()).toString();
	}
}
