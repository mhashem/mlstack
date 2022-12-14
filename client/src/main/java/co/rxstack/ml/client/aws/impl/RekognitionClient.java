package co.rxstack.ml.client.aws.impl;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import co.rxstack.ml.client.aws.IRekognitionClient;
import co.rxstack.ml.client.aws.converter.BoundingBoxConverter;
import co.rxstack.ml.client.aws.converter.FaceDetailConverter;
import co.rxstack.ml.common.model.Candidate;
import co.rxstack.ml.common.model.ComparisonResult;
import co.rxstack.ml.common.model.FaceDetectionResult;
import co.rxstack.ml.common.model.FaceRectangle;
import co.rxstack.ml.common.model.Recognizer;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.machinelearning.model.CreateRealtimeEndpointRequest;
import com.amazonaws.services.machinelearning.model.PredictResult;
import com.amazonaws.services.machinelearning.model.Prediction;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.Attribute;
import com.amazonaws.services.rekognition.model.BoundingBox;
import com.amazonaws.services.rekognition.model.CompareFacesMatch;
import com.amazonaws.services.rekognition.model.CompareFacesRequest;
import com.amazonaws.services.rekognition.model.CompareFacesResult;
import com.amazonaws.services.rekognition.model.ComparedFace;
import com.amazonaws.services.rekognition.model.CreateCollectionRequest;
import com.amazonaws.services.rekognition.model.CreateCollectionResult;
import com.amazonaws.services.rekognition.model.DeleteCollectionRequest;
import com.amazonaws.services.rekognition.model.DeleteFacesRequest;
import com.amazonaws.services.rekognition.model.DetectFacesRequest;
import com.amazonaws.services.rekognition.model.DetectFacesResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.IndexFacesRequest;
import com.amazonaws.services.rekognition.model.IndexFacesResult;
import com.amazonaws.services.rekognition.model.SearchFacesByImageRequest;
import com.amazonaws.services.rekognition.model.SearchFacesByImageResult;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author mhachem on 9/28/2017.
 */
public class RekognitionClient implements IRekognitionClient {

	private static final Logger log = LoggerFactory.getLogger(RekognitionClient.class);

	private final BoundingBoxConverter boundingBoxConverter = new BoundingBoxConverter();
	private final FaceDetailConverter faceDetailConverter = new FaceDetailConverter(boundingBoxConverter);

	private AmazonRekognition amazonRekognition;

	public RekognitionClient(String awsRegion, AWSStaticCredentialsProvider awsStaticCredentialsProvider) {
		Preconditions.checkNotNull(awsStaticCredentialsProvider);
		amazonRekognition = AmazonRekognitionClientBuilder.standard()
			.withCredentials(awsStaticCredentialsProvider)
			.withRegion(Regions.fromName(awsRegion))
			.build();
	}

	@Override
	public List<String> deleteFaces(String collectionId) {
		DeleteFacesRequest deleteFacesRequest = new DeleteFacesRequest();
		deleteFacesRequest.setCollectionId(collectionId);
		return amazonRekognition.deleteFaces(deleteFacesRequest).getDeletedFaces();
	}
	
	@Override
	public int createCollection(String collectionId) {
		CreateCollectionRequest createCollectionRequest = new CreateCollectionRequest();
		createCollectionRequest.setCollectionId(collectionId);
		CreateCollectionResult createCollectionResult = amazonRekognition.createCollection(createCollectionRequest);
		return createCollectionResult.getStatusCode();
	}
	
	@Override
	public int deleteCollection(String collectionId) {
		DeleteCollectionRequest deleteCollectionRequest = new DeleteCollectionRequest();
		deleteCollectionRequest.setCollectionId(collectionId);
		return amazonRekognition.deleteCollection(deleteCollectionRequest).getStatusCode();
	}
	
	@Override
	public Optional<ComparisonResult> compareFaces(byte[] faceOneBytes, byte[] faceTwoBytes) {

		Preconditions.checkArgument(faceOneBytes.length > 0);
		Preconditions.checkArgument(faceTwoBytes.length > 0);
		
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
			List<CompareFacesMatch> facesMatchList = compareFacesResult.getFaceMatches();
			for (CompareFacesMatch match : facesMatchList) {
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
		Preconditions.checkArgument(imageBytes.length > 0);

		try {
			Image image = byteArrayToImage(imageBytes);
			DetectFacesRequest detectFacesRequest = new DetectFacesRequest();
			detectFacesRequest.withImage(image).withAttributes(Attribute.ALL);

			DetectFacesResult detectFacesResult = amazonRekognition.detectFaces(detectFacesRequest);
			return detectFacesResult.getFaceDetails().stream().map(faceDetailConverter).collect(Collectors.toList());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return ImmutableList.of();
	}

	@Override
	public List<Candidate> searchFacesByImage(String collectionId, byte[] imageBytes, int maxFaces) {

		Preconditions.checkNotNull(collectionId);
		Preconditions.checkArgument(imageBytes.length > 0);
		Preconditions.checkArgument(maxFaces > 0 && maxFaces <= 4096);

		SearchFacesByImageRequest searchRequest = new SearchFacesByImageRequest();
		searchRequest.setCollectionId(collectionId);
		searchRequest.setImage(byteArrayToImage(imageBytes));
		searchRequest.setMaxFaces(maxFaces);

		SearchFacesByImageResult searchResult = amazonRekognition.searchFacesByImage(searchRequest);
		return searchResult.getFaceMatches().stream()
			.map(faceMatch -> {

				// todo use mapper class instead!

				BoundingBox boundingBox = faceMatch.getFace().getBoundingBox();
				log.info("[] bounding box : {}", boundingBox.toString());
				FaceRectangle faceRectangle = new FaceRectangle();
				faceRectangle.setHeight((int) (boundingBox.getHeight() * 100));
				faceRectangle.setWidth((int) (boundingBox.getWidth() * 100));
				faceRectangle.setLeft((int) (boundingBox.getLeft() * 100));
				faceRectangle.setTop((int) (boundingBox.getTop() * 100));

				return new Candidate(faceMatch.getFace().getFaceId(), faceMatch.getSimilarity(),
					faceRectangle, Recognizer.AWS_REKOGNITION);
			})
			.collect(Collectors.toList());
	}

	@Override
	public Optional<IndexFacesResult> indexFace(String collectionId, byte[] imageBytes) {
		Preconditions.checkNotNull(collectionId);
		Preconditions.checkArgument(imageBytes.length > 0);
		IndexFacesRequest indexFacesRequest = new IndexFacesRequest();
		indexFacesRequest.setCollectionId(collectionId);
		indexFacesRequest.setImage(byteArrayToImage(imageBytes));
		return Optional.ofNullable(amazonRekognition.indexFaces(indexFacesRequest));
	}
	
	public void model() {
		CreateRealtimeEndpointRequest request = new CreateRealtimeEndpointRequest();
		request.setMLModelId("ml-2HsB78gKdwy");
		
		
		
	}

	private Image byteArrayToImage(byte[] imageBytes) {
		return new Image().withBytes(ByteBuffer.wrap(imageBytes));
	}
}