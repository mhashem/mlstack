package co.rxstack.ml.aws.rekognition.mapper;

import java.util.function.Function;

import co.rxstack.ml.aws.rekognition.model.Face;
import co.rxstack.ml.aws.rekognition.model.FaceIndexingResult;

import com.amazonaws.services.rekognition.model.FaceRecord;

public class FaceIndexingResultMapper implements Function<FaceRecord, FaceIndexingResult> {
	@Override
	public FaceIndexingResult apply(FaceRecord faceRecord) {
		FaceIndexingResult faceIndexingResult = new FaceIndexingResult();

		// todo add Face mapper!
		Face face = new Face();
		face.setFaceId(faceRecord.getFace().getFaceId());
		face.setConfidence(faceRecord.getFace().getConfidence());
		face.setImageId(faceRecord.getFace().getImageId());

		faceIndexingResult.setFace(face);
		faceIndexingResult.setConfidence(faceRecord.getFace().getConfidence());
		return faceIndexingResult;
	}
}
