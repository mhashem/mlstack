package co.rxstack.ml.common.model;

import java.util.List;

/**
 * @author mhachem on 10/4/2017.
 */
public class FaceIdentificationResult {

	private String faceId;
	private List<Candidates> candidates;

	public FaceIdentificationResult() {
		// empty constructor
	}

	public FaceIdentificationResult(String faceId, List<Candidates> candidates) {
		this.faceId = faceId;
		this.candidates = candidates;
	}

	public String getFaceId() {
		return faceId;
	}

	public void setFaceId(String faceId) {
		this.faceId = faceId;
	}

	public List<Candidates> getCandidates() {
		return candidates;
	}

	public void setCandidates(List<Candidates> candidates) {
		this.candidates = candidates;
	}

	@Override
	public String toString() {
		return "FaceIdentificationResult{" + "faceId='" + faceId + '\'' + ", candidates=" + candidates + '}';
	}
}
