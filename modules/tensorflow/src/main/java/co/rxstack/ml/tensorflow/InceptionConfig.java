package co.rxstack.ml.tensorflow;

public class InceptionConfig {

	private String graphPath;
	private String labelsPath;

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
}
