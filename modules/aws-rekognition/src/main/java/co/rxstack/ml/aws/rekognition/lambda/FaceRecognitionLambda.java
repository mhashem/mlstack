package co.rxstack.ml.aws.rekognition.lambda;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.IndexFacesRequest;
import com.amazonaws.services.rekognition.model.IndexFacesResult;
import com.amazonaws.services.rekognition.model.S3Object;
import com.google.common.collect.ImmutableMap;

/**
 * todo last step change, for now keep the Python script in use!
 *
 * @author mhachem on 10/8/2017.
 */
public class FaceRecognitionLambda {

	private AmazonRekognition amazonRekognition;
	private AmazonDynamoDBClient dynamoDBClient;

	public FaceRecognitionLambda() {

	}

	public IndexFacesResult indexFaces(String bucket, String key, String collectionId) {
		// S3Object bucket and name
		S3Object s3Object = new S3Object();
		s3Object.setBucket(bucket);
		s3Object.setName(key);

		// image
		Image image = new Image();
		image.setS3Object(s3Object);

		IndexFacesRequest indexFacesRequest = new IndexFacesRequest();
		indexFacesRequest.setCollectionId(collectionId);
		indexFacesRequest.setImage(image);
		return amazonRekognition.indexFaces(indexFacesRequest);
	}

	public PutItemResult updateIndex(String tableName, String faceId, String fullName) {

		ImmutableMap<String, AttributeValue> itemMap =
			ImmutableMap.of("RekognitionId", new AttributeValue(faceId), "FullName", new AttributeValue(fullName));

		PutItemRequest putItemRequest = new PutItemRequest();
		putItemRequest.setTableName(tableName);
		putItemRequest.setItem(itemMap);

		return dynamoDBClient.putItem(putItemRequest);
	}



}
