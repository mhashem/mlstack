package co.rxstack.ml.client.aws.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

import co.rxstack.ml.client.aws.IRekognitionClient;
import co.rxstack.ml.common.model.ComparisonResult;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mhachem on 9/28/2017.
 */
public class RekognitionClient implements IRekognitionClient {

	private static final Logger log = LoggerFactory.getLogger(RekognitionClient.class);

	private AmazonRekognition amazonRekognition;

	public RekognitionClient(AWSStaticCredentialsProvider awsStaticCredentialsProvider) {
		amazonRekognition = AmazonRekognitionClientBuilder.standard()
			.withCredentials(awsStaticCredentialsProvider)
			.withRegion(Regions.EU_WEST_1)
			.build();
	}

	@Override
	public Optional<ComparisonResult> compareFaces(InputStream faceOneStream, InputStream faceTwoStream) {

		try {
			// todo return list instead of optional in case of multiple faces!
			ComparisonResult comparisonResult = new ComparisonResult();
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
				// fixme !
				comparisonResult.setConfidence(face.getConfidence());
				log.info("Face at {} {} matches with {}% confidence.",
					position.getLeft().toString(), position.getTop(), face.getConfidence().toString());
			}
			List<ComparedFace> uncompared = compareFacesResult.getUnmatchedFaces();

			log.info("There were {} that did not match", uncompared.size());
			log.info("Source image rotation: {}", compareFacesResult.getSourceImageOrientationCorrection());
			log.info("target image rotation: {}", compareFacesResult.getTargetImageOrientationCorrection());

			return Optional.of(comparisonResult);

		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}

		return Optional.empty();
	}

}