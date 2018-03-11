package co.rxstack.ml.aggregator.config;

public class ClassifierConfig {

	private String classifierPath;
	private String classifierNamePrefix;

	public void setClassifierPath(String classifierPath) {
		this.classifierPath = classifierPath;
	}

	public String getClassifierPath() {
		return classifierPath;
	}

	public String getClassifierNamePrefix() {
		return classifierNamePrefix;
	}

	public void setClassifierNamePrefix(String classifierNamePrefix) {
		this.classifierNamePrefix = classifierNamePrefix;
	}
}
