package co.rxstack.ml.tensorflow.exception;

public class GraphLoadingException extends Exception {
	
	public GraphLoadingException(String message) {
		super("No ProtoBuffer graph found: " + message);
	}
	
}
