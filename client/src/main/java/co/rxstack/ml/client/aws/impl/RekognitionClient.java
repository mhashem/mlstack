package co.rxstack.ml.client.aws.impl;

import co.rxstack.ml.client.aws.IRekognitionClient;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.google.common.base.Preconditions;

/**
 * @author mhachem on 9/28/2017.
 */
public class RekognitionClient implements IRekognitionClient {

	private String accessKey;
	private String secretKey;

	private AWSCredentials awsCredentials;
	private AmazonRekognition amazonRekognition;

	public RekognitionClient(String accessKey, String secretKey) {
		Preconditions.checkNotNull(accessKey);
		Preconditions.checkNotNull(secretKey);

		awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
		amazonRekognition = AmazonRekognitionClientBuilder.standard()
			.withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
			.withRegion(Regions.US_EAST_1)
			.build();

		/*AWSCredentials credentials = new BasicAWSCredentials("", "");
		AmazonRekognition amazonRekognition = AmazonRekognitionClientBuilder.standard()
			.withCredentials(new AWSStaticCredentialsProvider(credentials))
			.withRegion(Regions.US_EAST_1)
			.build();

		CompareFacesRequest compareFacesRequest = new CompareFacesRequest();

		CompareFacesResult compareFacesResult = amazonRekognition.compareFaces(compareFacesRequest);

		compareFacesResult.getFaceMatches().get(0).getSimilarity();*/
	}



}
