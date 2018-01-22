package co.rxstack.ml.cognitiveservices.config;

import com.google.common.base.MoreObjects;

public class CognitiveServicesConfig {

	private String personGroupId;
	private String personGroupName;

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

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(CognitiveServicesConfig.class)
			.add("personGroupId", getPersonGroupId())
			.add("personGroupName", getPersonGroupName())
			.toString();
	}
}
