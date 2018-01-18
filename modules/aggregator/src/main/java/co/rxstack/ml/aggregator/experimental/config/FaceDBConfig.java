package co.rxstack.ml.aggregator.experimental.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

public class FaceDBConfig {
	
	private String faceDbPath;
	private String faceDirectoryNameDelimiter;

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
}
