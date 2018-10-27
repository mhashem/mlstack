package co.rxstack.ml.tensorflow.config;

public class InceptionConfig {

	private String graphPath;
	private String labelsPath;
	private boolean required;

	public String getGraphPath() {
		return graphPath;
	}

	public void setGraphPath(String graphPath) {
		this.graphPath = graphPath;
	}

	public String getLabelsPath() {
		return labelsPath;
	}

	public void setLabelsPath(String labelsPath) {
		this.labelsPath = labelsPath;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}
}
