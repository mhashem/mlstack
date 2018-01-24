package co.rxstack.ml.cognitiveservices.config;

import com.google.common.base.MoreObjects;

public class CognitiveServicesConfig {

	private String personGroupId;
	private String personGroupName;
	private int maxCandidates;
	private double threshold;

	public String getPersonGroupId() {
		return personGroupId;
	}

	public void setPersonGroupId(String personGroupId) {
		this.personGroupId = personGroupId;
	}

	public String getPersonGroupName() {
		return personGroupName;
	}

	public void setPersonGroupName(String personGroupName) {
		this.personGroupName = personGroupName;
	}

	public int getMaxCandidates() {
		return maxCandidates;
	}

	public void setMaxCandidates(int maxCandidates) {
		this.maxCandidates = maxCandidates;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(CognitiveServicesConfig.class)
			.add("personGroupId", getPersonGroupId())
			.add("personGroupName", getPersonGroupName())
			.add("maxCandidates", getMaxCandidates())
			.add("threshold", getThreshold())
			.toString();
	}
}
