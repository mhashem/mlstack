package co.rxstack.ml.aggregator.experimental.model;

import com.google.common.base.MoreObjects;

public class Person {

	private int faceId;
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getFaceId() {
		return faceId;
	}

	public void setFaceId(int faceId) {
		this.faceId = faceId;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(Person.class)
			.add("faceId", faceId).add("name", name).toString();
	}
}
