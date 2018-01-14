package co.rxstack.ml.aws.rekognition.service.impl;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import co.rxstack.ml.aws.rekognition.model.FaceIndexingResult;
import co.rxstack.ml.client.aws.IRekognitionClient;

import com.amazonaws.services.rekognition.model.BoundingBox;
import com.amazonaws.services.rekognition.model.Face;
import com.amazonaws.services.rekognition.model.FaceRecord;
import com.amazonaws.services.rekognition.model.IndexFacesResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RekognitionServiceTest {

	@Mock
	private IRekognitionClient rekognitionClient;

	private RekognitionService rekognitionService;

	@Before
	public void setup() {
		rekognitionService = new RekognitionService(rekognitionClient);
	}

	@Test
	public void testIndexFacesEmptyResult() {
		when(rekognitionClient.indexFace(anyString(), any())).thenReturn(Optional.empty());
		List<FaceIndexingResult> faceIndexingResults = rekognitionService.indexFaces("test_id", new byte[] {});
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
		when(rekognitionClient.indexFace(anyString(), any())).thenReturn(Optional.of(facesResult));
		List<FaceIndexingResult> faceIndexingResults = rekognitionService.indexFaces("test_id", new byte[] {});
		Assert.assertEquals(1, faceIndexingResults.size());
	}

}
