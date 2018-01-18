package co.rxstack.ml.aggregator.experimental.model;

import java.util.List;

import com.google.common.base.MoreObjects;

public class PersonBundleStatistics {

	private int imagesCount;
	private int personsCount;

	private PersonBundleStatistics() {

	}

	public static PersonBundleStatistics fromBundle(List<PersonBundle> personBundles) {
		PersonBundleStatistics statistics = new PersonBundleStatistics();
		statistics.setPersonsCount(personBundles.size());
		statistics.setImagesCount(personBundles.stream().mapToInt(value -> value.getFaceImagesPaths().size()).sum());
		return statistics;
	}

	public int getPersonsCount() {
		return personsCount;
	}

	public void setPersonsCount(int personsCount) {
		this.personsCount = personsCount;
	}

	public int getImagesCount() {
		return imagesCount;
	}

	public void setImagesCount(int imagesCount) {
		this.imagesCount = imagesCount;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(PersonBundleStatistics.class)
			.add("personsCount", getPersonsCount())
			.add("imagesCount", getImagesCount()).toString();
	}
}
