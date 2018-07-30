package co.rxstack.ml.common.model;

import java.util.Arrays;

import com.google.common.base.MoreObjects;
import com.google.common.base.Ticker;

public class Ticket {

	public enum Type {
		TRAINING, INDEXING, RECOGNITION
	}

	public enum ImageType {
		PNG, JPG
	}

	private String id;
	private String personId;
	private String personName;
	private byte[] imageBytes;
	private Type type;
	private String imageName;
	private ImageType imageType;

	public Ticket(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}
	
	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	public byte[] getImageBytes() {
		return imageBytes;
	}

	public void setImageBytes(byte[] imageBytes) {
		this.imageBytes = imageBytes;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getPersonName() {
		return personName;
	}

	public void setPersonName(String personName) {
		this.personName = personName;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public ImageType getImageType() {
		return imageType;
	}

	public void setImageType(ImageType imageType) {
		this.imageType = imageType;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(Ticket.class)
			.add("id", id)
			.add("personId", personId)
			.add("personName", personName)
			.add("imageBytes", Arrays.toString(imageBytes))
			.add("type", type)
			.add("imageType", imageType)
			.toString();
	}
}
