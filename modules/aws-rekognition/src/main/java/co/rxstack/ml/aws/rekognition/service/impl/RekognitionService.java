package co.rxstack.ml.aws.rekognition.service.impl;

import co.rxstack.ml.aws.rekognition.service.IRekognitionService;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.CompareFacesRequest;
import com.amazonaws.services.rekognition.model.CompareFacesResult;

/**
 * @author mhachem on 9/28/2017.
 */
public class RekognitionService implements IRekognitionService {

	public static void main(String[] args) {
		
		AWSCredentials credentials = new BasicAWSCredentials("", "");
		AmazonRekognition amazonRekognition = AmazonRekognitionClientBuilder.standard()
			.withCredentials(new AWSStaticCredentialsProvider(credentials))
			.withRegion(Regions.US_EAST_1)
			.build();

		CompareFacesRequest compareFacesRequest = new CompareFacesRequest();

		CompareFacesResult compareFacesResult = amazonRekognition.compareFaces(compareFacesRequest);

		compareFacesResult.getFaceMatches().get(0).getSimilarity();
		
	}
	
}
