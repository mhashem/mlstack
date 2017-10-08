package co.rxstack.ml.client.aws.converter;

import java.util.function.Function;

import co.rxstack.ml.common.model.FaceAttributes;
import co.rxstack.ml.common.model.FaceDetectionResult;
import co.rxstack.ml.common.model.FaceRectangle;

import com.amazonaws.services.rekognition.model.BoundingBox;
import com.amazonaws.services.rekognition.model.FaceDetail;

/**
 * @author mhachem on 10/8/2017.
 */
public class FaceDetailConverter implements Function<FaceDetail, FaceDetectionResult> {

	private BoundingBoxConverter boundingBoxConverter;

	public FaceDetailConverter(BoundingBoxConverter boundingBoxConverter) {
		this.boundingBoxConverter = boundingBoxConverter;
	}

	@Override
	public FaceDetectionResult apply(FaceDetail faceDetail) {
		FaceAttributes faceAttributes = new FaceAttributes();
		faceAttributes.setAge(faceDetail.getAgeRange().getHigh());
		faceAttributes.setGender(faceDetail.getGender().getValue());

		FaceRectangle faceRectangle = boundingBoxConverter.apply(faceDetail.getBoundingBox());

		FaceDetectionResult faceDetectionResult = new FaceDetectionResult();
		faceDetectionResult.setFaceAttributes(faceAttributes);
		faceDetectionResult.setFaceRectangle(faceRectangle);
		return faceDetectionResult;
	}

}
