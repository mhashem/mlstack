package co.rxstack.ml.aws.rekognition.config;

public class AwsConfig {

	private int maxFaces;
	private String collectionId;

	public int getMaxFaces() {
		return maxFaces;
	}

	public void setMaxFaces(int maxFaces) {
		this.maxFaces = maxFaces;
	}

	public String getCollectionId() {
		return collectionId;
	}

	public void setCollectionId(String collectionId) {
		this.collectionId = collectionId;
	}
}
