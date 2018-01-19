package co.rxstack.ml.aggregator.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.MoreObjects;

public class PersonBundle {

	private Person person;
	private List<Path> faceImagesPaths;

	public PersonBundle() {
		faceImagesPaths = new ArrayList<>();
	}

	public PersonBundle(Person person, List<Path> faceList) {
		this.person = person;
		this.faceImagesPaths = faceList;
	}

	public Person getPerson() {
		return person;
	}

	public void setPerson(Person person) {
		this.person = person;
	}

	public List<Path> getFaceImagesPaths() {
		return faceImagesPaths;
	}

	public void setFaceImagesPaths(List<Path> faceImagesPaths) {
		this.faceImagesPaths = faceImagesPaths;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(PersonBundle.class)
			.add("person", person)
			.add("faceImagesPaths", faceImagesPaths).toString();
	}
}
