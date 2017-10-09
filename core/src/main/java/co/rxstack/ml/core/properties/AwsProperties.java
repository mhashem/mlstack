package co.rxstack.ml.core.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws", ignoreUnknownFields = true)
public class AwsProperties {
	
	private String region;
	private String accessKey;
	private String secretKey;
	
	private S3 s3;
	
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
	
}
