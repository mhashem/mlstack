package co.rxstack.ml.aws.rekognition.service.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.rxstack.ml.aws.rekognition.config.AwsConfig;
import co.rxstack.ml.aws.rekognition.model.FaceIndexingResult;
import co.rxstack.ml.client.aws.IRekognitionClient;

import com.amazonaws.services.rekognition.model.BoundingBox;
import com.amazonaws.services.rekognition.model.Face;
import com.amazonaws.services.rekognition.model.FaceRecord;
import com.amazonaws.services.rekognition.model.IndexFacesResult;
import com.google.common.collect.ImmutableMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Strict.class)
public class RekognitionServiceTest {

	@Mock
	private AwsConfig awsConfig;
	@Mock
	private IRekognitionClient rekognitionClient;

	private RekognitionService rekognitionService;

	@Before
	public void setup() {
		rekognitionService = new RekognitionService(rekognitionClient, awsConfig);
	}

	@Test
	public void testIndexFacesEmptyResult() {
		List<FaceIndexingResult> faceIndexingResults = rekognitionService.indexFaces(new byte[] {}, ImmutableMap.of());
		Assert.assertTrue(faceIndexingResults.isEmpty());
	}

	@Test
	public void testIndexFaces() {

		BoundingBox boundingBox = new BoundingBox();
		boundingBox.setTop(10f);
		boundingBox.setLeft(10f);
		boundingBox.setHeight(20f);
		boundingBox.setWidth(20f);

		Face face = new Face();
		face.setFaceId(UUID.randomUUID().toString());
		face.setConfidence(80.2F);
		face.setBoundingBox(boundingBox);

		FaceRecord faceRecord = new FaceRecord();
		faceRecord.setFace(face);

		IndexFacesResult facesResult = new IndexFacesResult();
		facesResult.setFaceRecords(Collections.singletonList(faceRecord));

		when((awsConfig.getCollectionId())).thenReturn("test-collection");
		when(rekognitionClient.indexFace(any(), any())).thenReturn(Optional.of(facesResult));
		List<FaceIndexingResult> faceIndexingResults = rekognitionService.indexFaces(new byte[] {}, ImmutableMap.of());
		Assert.assertEquals(1, faceIndexingResults.size());
	}

}
