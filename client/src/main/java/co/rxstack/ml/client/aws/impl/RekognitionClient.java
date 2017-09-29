package co.rxstack.ml.client.aws.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

import co.rxstack.ml.client.aws.IRekognitionClient;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.BoundingBox;
import com.amazonaws.services.rekognition.model.CompareFacesMatch;
import com.amazonaws.services.rekognition.model.CompareFacesRequest;
import com.amazonaws.services.rekognition.model.CompareFacesResult;
import com.amazonaws.services.rekognition.model.ComparedFace;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.util.IOUtils;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mhachem on 9/28/2017.
 */
public class RekognitionClient implements IRekognitionClient {

	private static final Logger log = LoggerFactory.getLogger(RekognitionClient.class);

	private AmazonRekognition amazonRekognition;

	public RekognitionClient(String accessKey, String secretKey) {
		Preconditions.checkNotNull(accessKey);
		Preconditions.checkNotNull(secretKey);

		AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
		amazonRekognition = AmazonRekognitionClientBuilder.standard()
			.withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
			.withRegion(Regions.US_EAST_1)
			.build();
	}

	@Override
	public void compareFaces(InputStream faceOneStream, InputStream faceTwoStream) {

		try {
			Image faceOne = new Image().withBytes(ByteBuffer.wrap(IOUtils.toByteArray(faceOneStream)));
			Image faceTwo = new Image().withBytes(ByteBuffer.wrap(IOUtils.toByteArray(faceTwoStream)));

			CompareFacesRequest compareFacesRequest = new CompareFacesRequest();
			compareFacesRequest.withSourceImage(faceOne)
				.withTargetImage(faceTwo)
				.withSimilarityThreshold(70F);

			CompareFacesResult compareFacesResult = amazonRekognition.compareFaces(compareFacesRequest);

			// Display results
			List<CompareFacesMatch> faceDetails = compareFacesResult.getFaceMatches();
			for (CompareFacesMatch match: faceDetails){
				ComparedFace face= match.getFace();
				BoundingBox position = face.getBoundingBox();
				System.out.println("Face at " + position.getLeft().toString()
					+ " " + position.getTop()
					+ " matches with " + face.getConfidence().toString()
					+ "% confidence.");

			}
			List<ComparedFace> uncompared = compareFacesResult.getUnmatchedFaces();

			System.out.println("There were " + uncompared.size()
				+ " that did not match");
			System.out.println("Source image rotation: " + compareFacesResult.getSourceImageOrientationCorrection());
			System.out.println("target image rotation: " + compareFacesResult.getTargetImageOrientationCorrection());

		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

}