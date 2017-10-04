package co.rxstack.ml.common.model;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * @author mhachem on 10/4/2017.
 */
public class FaceIdentificationResult {

	private String faceId;
	private List<Candidate> candidates;

	public FaceIdentificationResult() {
		// empty constructor
	}

	public FaceIdentificationResult(String faceId, List<Candidate> candidates) {
		this.faceId = faceId;
		this.candidates = candidates;
	}

	public String getFaceId() {
		return faceId;
	}

	public void setFaceId(String faceId) {
		this.faceId = faceId;
	}

	public List<Candidate> getCandidates() {
		return candidates;
	}

	public void setCandidates(List<Candidate> candidates) {
		this.candidates = candidates;
	}
	
	public Optional<Candidate> getCandidateBestMatch() {
		return candidates.stream().max(Comparator.naturalOrder());
	}

	@Override
	public String toString() {
		return "FaceIdentificationResult{" + "faceId='" + faceId + '\'' + ", candidates=" + candidates + '}';
	}
}
