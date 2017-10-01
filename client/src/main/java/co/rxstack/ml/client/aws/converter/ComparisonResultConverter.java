package co.rxstack.ml.client.aws.converter;

import java.util.function.Function;

import co.rxstack.ml.common.model.ComparisonResult;
import com.amazonaws.services.rekognition.model.CompareFacesResult;

/**
 * @author mhachem on 10/1/2017.
 */
public class ComparisonResultConverter implements Function<CompareFacesResult, ComparisonResult> {

	@Override
	public ComparisonResult apply(CompareFacesResult compareFacesResult) {
		// todo implement converter
		return null;
	}

}
