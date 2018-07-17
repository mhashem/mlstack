package co.rxstack.ml.tensorflow.exception;

public class GraphLoadingException extends Exception {
	
	public GraphLoadingException(String message) {
		super("Failed to load ProtoBuffer graph at [" + message + "] most probably file does not exist");
	}
	
}
