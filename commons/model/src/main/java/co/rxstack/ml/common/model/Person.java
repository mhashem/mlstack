package co.rxstack.ml.common.model;

import java.util.List;

/**
 * @author mhachem on 9/27/2017.
 */
public class Person {

	String personId;
	String name;
	String userData;
	List<String> persistedFaceIds;

	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUserData() {
		return userData;
	}

	public void setUserData(String userData) {
		this.userData = userData;
	}

	public List<String> getPersistedFaceIds() {
		return persistedFaceIds;
	}

	public void setPersistedFaceIds(List<String> persistedFaceIds) {
		this.persistedFaceIds = persistedFaceIds;
	}

	@Override
	public String toString() {
		return "Person{" + "personId='" + personId + '\'' + ", name='" + name + '\'' + ", userData='" + userData + '\''
			+ ", persistedFaceIds=" + persistedFaceIds + '}';
	}
}
