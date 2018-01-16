package co.rxstack.ml.aggregator.experimental;

import java.awt.image.BufferedImage;
import java.util.List;

public class PersonBundle {

	private String faceId;
	private String name;
	private List<BufferedImage> faceList;

	public PersonBundle() {
	}

	public PersonBundle(String faceId, String name, List<BufferedImage> faceList) {
		this.faceId = faceId;
		this.name = name;
		this.faceList = faceList;
	}

	public String getFaceId() {
		return faceId;
	}

	public void setFaceId(String faceId) {
		this.faceId = faceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<BufferedImage> getFaceList() {
		return faceList;
	}

	public void setFaceList(List<BufferedImage> faceList) {
		this.faceList = faceList;
	}

}
