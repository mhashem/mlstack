package co.rxstack.ml.aggregator.config;

import co.rxstack.ml.aggregator.model.FaceRecognitionAlgorithm;

import com.google.common.base.MoreObjects;

public class FaceDBConfig {
	
	private String faceDbPath;
	private String faceDirectoryNameDelimiter;
	private String modelStoragePath;
	private FaceRecognitionAlgorithm algorithm;

	private int standardWidth;
	private int standardHeight;

	public String getFaceDbPath() {
		return faceDbPath;
	}

	public void setFaceDbPath(String faceDbPath) {
		this.faceDbPath = faceDbPath;
	}

	public String getFaceDirectoryNameDelimiter() {
		return faceDirectoryNameDelimiter;
	}

	public void setFaceDirectoryNameDelimiter(String faceDirectoryNameDelimiter) {
		this.faceDirectoryNameDelimiter = faceDirectoryNameDelimiter;
	}

	public String getModelStoragePath() {
		return modelStoragePath;
	}

	public void setModelStoragePath(String modelStoragePath) {
		this.modelStoragePath = modelStoragePath;
	}

	public int getStandardWidth() {
		return standardWidth;
	}

	public void setStandardWidth(int standardWidth) {
		this.standardWidth = standardWidth;
	}

	public int getStandardHeight() {
		return standardHeight;
	}

	public void setStandardHeight(int standardHeight) {
		this.standardHeight = standardHeight;
	}
	
	public FaceRecognitionAlgorithm getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(FaceRecognitionAlgorithm algorithm) {
		this.algorithm = algorithm;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(FaceDBConfig.class).add("faceDbPath", getFaceDbPath())
			.add("faceDirectoryNameDelimiter", getFaceDirectoryNameDelimiter())
			.add("modelStoragePath", getModelStoragePath()).add("standardWidth", getStandardWidth())
			.add("standardHeight", getStandardHeight()).add("algorithm", getAlgorithm()).toString();
	}
}
