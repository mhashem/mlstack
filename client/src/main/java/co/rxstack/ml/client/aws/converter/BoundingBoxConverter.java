package co.rxstack.ml.client.aws.converter;

import java.util.function.Function;

import co.rxstack.ml.common.model.FaceRectangle;

import com.amazonaws.services.rekognition.model.BoundingBox;

/**
 * @author mhachem on 10/8/2017.
 */
public class BoundingBoxConverter implements Function<BoundingBox, FaceRectangle> {
	@Override
	public FaceRectangle apply(BoundingBox boundingBox) {
		return new FaceRectangle(boundingBox.getLeft(), boundingBox.getTop(), boundingBox.getWidth(),
			boundingBox.getHeight());
	}
}
