package co.rxstack.ml.common.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AggregateFaceIdentification {

	@JsonProperty("aws")
	private List<Candidate> awsCandidates;
	@JsonProperty("open_cv")
	private List<Candidate> openCVCandidates;
	@JsonProperty("cognitive")
	private List<Candidate> cognitiveCandidates;

	public List<Candidate> getAwsCandidates() {
		return awsCandidates;
	}

	public void setAwsCandidates(List<Candidate> awsCandidates) {
		this.awsCandidates = awsCandidates;
	}

	public List<Candidate> getOpenCVCandidates() {
		return openCVCandidates;
	}

	public void setOpenCVCandidates(List<Candidate> openCVCandidates) {
		this.openCVCandidates = openCVCandidates;
	}

	public List<Candidate> getCognitiveCandidates() {
		return cognitiveCandidates;
	}

	public void setCognitiveCandidates(List<Candidate> cognitiveCandidates) {
		this.cognitiveCandidates = cognitiveCandidates;
	}
}
