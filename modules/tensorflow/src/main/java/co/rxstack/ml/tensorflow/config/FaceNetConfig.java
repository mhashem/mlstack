package co.rxstack.ml.tensorflow.config;

public class FaceNetConfig {

	private String faceNetGraphPath;
	private String embeddingsFilePath;

	public String getFaceNetGraphPath() {
		return faceNetGraphPath;
	}

	public void setFaceNetGraphPath(String faceNetGraphPath) {
		this.faceNetGraphPath = faceNetGraphPath;
	}

	public String getEmbeddingsFilePath() {
		return embeddingsFilePath;
	}

	public void setEmbeddingsFilePath(String embeddingsFilePath) {
		this.embeddingsFilePath = embeddingsFilePath;
	}
}
