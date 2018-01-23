package co.rxstack.ml.common.model;

public class Ticket {
	
	private Type type;
	
	public enum Type {
		TRAINING, INDEXING, SEARCH
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
}
