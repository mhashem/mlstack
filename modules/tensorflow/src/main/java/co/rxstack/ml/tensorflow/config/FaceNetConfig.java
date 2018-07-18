package co.rxstack.ml.tensorflow.config;

import com.google.common.base.MoreObjects;

public class FaceNetConfig {

	private String faceNetGraphPath;
	private String embeddingsFilePath;
	private String dataSetPath;

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

	public String getDataSetPath() {
		return dataSetPath;
	}

	public void setDataSetPath(String dataSetPath) {
		this.dataSetPath = dataSetPath;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(FaceNetConfig.class)
			.add("faceNetGraphPath", faceNetGraphPath)
			.add("embeddingsFilePath", embeddingsFilePath)
			.add("dataSetPath", dataSetPath)
			.toString();
	}
}
