package co.rxstack.ml.aggregator.experimental.config;

import com.google.common.base.MoreObjects;

public class FaceDBConfig {
	
	private String faceDbPath;
	private String faceDirectoryNameDelimiter;
	private String modelStoragePath;

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

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(FaceDBConfig.class).add("faceDbPath", getFaceDbPath())
			.add("faceDirectoryNameDelimiter", getFaceDirectoryNameDelimiter())
			.add("modelStoragePath", getModelStoragePath()).add("standardWidth", getStandardWidth())
			.add("standardHeight", getStandardHeight()).toString();
	}
}
