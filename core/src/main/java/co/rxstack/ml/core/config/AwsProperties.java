package co.rxstack.ml.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws")
public class AwsProperties {
	
	private String region;
	private String accessKey;
	private String secretKey;
	
	private S3 s3;
	private Rekognition rekognition;
	
	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getAccessKey() {
		return accessKey;
	}

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public S3 getS3() {
		return s3;
	}

	public void setS3(S3 s3) {
		this.s3 = s3;
	}

	public Rekognition getRekognition() {
		return rekognition;
	}

	public void setRekognition(Rekognition rekognition) {
		this.rekognition = rekognition;
	}
	
	public static class S3 {
		
		private String bucket;
		private String bucketFolder;

		public String getBucket() {
			return bucket;
		}

		public void setBucket(String bucket) {
			this.bucket = bucket;
		}

		public String getBucketFolder() {
			return bucketFolder;
		}

		public void setBucketFolder(String bucketFolder) {
			this.bucketFolder = bucketFolder;
		}
		
	}
	
	public static class Rekognition {

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


}
