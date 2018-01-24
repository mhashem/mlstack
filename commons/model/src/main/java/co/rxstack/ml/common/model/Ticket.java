package co.rxstack.ml.common.model;

public class Ticket {

	public enum Type {
		TRAINING, INDEXING, RECOGNITION
	}

	private String id;
	private String personId;
	private String personName;
	private byte[] imageBytes;
	private Type type;

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
}
