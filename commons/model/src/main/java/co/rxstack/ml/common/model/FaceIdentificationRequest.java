package co.rxstack.ml.common.model;

import java.util.List;

/**
 * @author mhachem on 10/4/2017.
 */
public class FaceIdentificationRequest {

	private String personGroupId;
	private List<String> faceIds;
	private int maxNumOfCandidatesReturned = 1;
	private double confidenceThreshold = 1;

	public FaceIdentificationRequest() {
		// empty constructor
	}

	public FaceIdentificationRequest(String personGroupId, List<String> faceIds, int maxNumOfCandidatesReturned,
		double confidenceThreshold) {
		this.personGroupId = personGroupId;
		this.faceIds = faceIds;
		this.maxNumOfCandidatesReturned = maxNumOfCandidatesReturned;
		this.confidenceThreshold = confidenceThreshold;
	}

	public String getPersonGroupId() {
		return personGroupId;
	}

	public void setPersonGroupId(String personGroupId) {
		this.personGroupId = personGroupId;
	}

	public List<String> getFaceIds() {
		return faceIds;
	}

	public void setFaceIds(List<String> faceIds) {
		this.faceIds = faceIds;
	}

	public int getMaxNumOfCandidatesReturned() {
		return maxNumOfCandidatesReturned;
	}

	public void setMaxNumOfCandidatesReturned(int maxNumOfCandidatesReturned) {
		this.maxNumOfCandidatesReturned = maxNumOfCandidatesReturned;
	}

	public double getConfidenceThreshold() {
		return confidenceThreshold;
	}

	public void setConfidenceThreshold(double confidenceThreshold) {
		this.confidenceThreshold = confidenceThreshold;
	}

	@Override
	public String toString() {
		return "FaceIdentificationRequest{" + "personGroupId='" + personGroupId + '\'' + ", faceIds=" + faceIds
			+ ", maxNumOfCandidatesReturned=" + maxNumOfCandidatesReturned + ", confidenceThreshold="
			+ confidenceThreshold + '}';
	}
}
