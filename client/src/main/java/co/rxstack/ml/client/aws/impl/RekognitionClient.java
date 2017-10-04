package co.rxstack.ml.client.aws.impl;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import co.rxstack.ml.client.aws.IRekognitionClient;
import co.rxstack.ml.common.model.ComparisonResult;
import co.rxstack.ml.common.model.FaceAttributes;
import co.rxstack.ml.common.model.FaceDetectionResult;
import co.rxstack.ml.common.model.FaceRectangle;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.Attribute;
import com.amazonaws.services.rekognition.model.BoundingBox;
import com.amazonaws.services.rekognition.model.CompareFacesMatch;
import com.amazonaws.services.rekognition.model.CompareFacesRequest;
import com.amazonaws.services.rekognition.model.CompareFacesResult;
import com.amazonaws.services.rekognition.model.ComparedFace;
import com.amazonaws.services.rekognition.model.DetectFacesRequest;
import com.amazonaws.services.rekognition.model.DetectFacesResult;
import com.amazonaws.services.rekognition.model.Image;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
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
	public Optional<ComparisonResult> compareFaces(byte[] faceOneBytes, byte[] faceTwoBytes) {

		try {
			// todo return list instead of optional in case of multiple faces!
			ComparisonResult comparisonResult = new ComparisonResult();
			Image faceOne = new Image().withBytes(ByteBuffer.wrap(faceOneBytes));
			Image faceTwo = new Image().withBytes(ByteBuffer.wrap(faceTwoBytes));

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
					position.getLeft(), position.getTop(), face.getConfidence());
			}
			List<ComparedFace> uncompared = compareFacesResult.getUnmatchedFaces();

			log.info("There were {} that did not match", uncompared.size());
			log.info("Source image rotation: {}", compareFacesResult.getSourceImageOrientationCorrection());
			log.info("target image rotation: {}", compareFacesResult.getTargetImageOrientationCorrection());

			return Optional.of(comparisonResult);

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return Optional.empty();
	}

	@Override
	public List<FaceDetectionResult> detect(byte[] imageBytes) {
		Preconditions.checkNotNull(imageBytes);

		try {
			Image image = new Image().withBytes(ByteBuffer.wrap(imageBytes));
			DetectFacesRequest detectFacesRequest = new DetectFacesRequest();

			detectFacesRequest.withImage(image).withAttributes(Attribute.ALL);

			DetectFacesResult detectFacesResult = amazonRekognition.detectFaces(detectFacesRequest);

			return detectFacesResult.getFaceDetails().stream().map(faceDetail -> {
				FaceAttributes faceAttributes = new FaceAttributes();
				faceAttributes.setAge(faceDetail.getAgeRange().getHigh());
				faceAttributes.setGender(faceDetail.getGender().getValue());

				BoundingBox boundingBox = faceDetail.getBoundingBox();
				FaceRectangle faceRectangle =
					new FaceRectangle(boundingBox.getLeft(), boundingBox.getTop(), boundingBox.getHeight(),
						boundingBox.getWidth());

				FaceDetectionResult faceDetectionResult = new FaceDetectionResult();
				faceDetectionResult.setFaceAttributes(faceAttributes);
				faceDetectionResult.setFaceRectangle(faceRectangle);

				return faceDetectionResult;
			}).collect(Collectors.toList());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return ImmutableList.of();
	}

}