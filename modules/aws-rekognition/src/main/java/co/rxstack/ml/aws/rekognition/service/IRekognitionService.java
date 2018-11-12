package co.rxstack.ml.aws.rekognition.service;

import java.util.List;
import java.util.Optional;

import co.rxstack.ml.api.FaceIndexer;
import co.rxstack.ml.aws.rekognition.model.FaceIndexingResult;
import co.rxstack.ml.common.model.Candidate;
import co.rxstack.ml.common.model.ComparisonResult;
import co.rxstack.ml.common.model.FaceDetectionResult;

/**
 * @author mhachem on 9/28/2017.
 */
public interface IRekognitionService extends FaceIndexer<FaceIndexingResult> {

	List<String> deleteFaces();

	Optional<ComparisonResult> compareFaces(byte[] faceOneImageBytes, byte[] faceTwoImageBytes);

	List<FaceDetectionResult> detect(byte[] imageBytes);

	Optional<Candidate> searchFaceByImage(byte[] imageBytes);

	/**
	 * @param imageBytes {byte[]} The following is a list of limits in Amazon Rekognition:
	 *        - Maximum image size stored as an Amazon S3 object is limited to 15 MB.
	 *        - The minimum pixel resolution for height and width is 80 pixels.
	 *        - To be detected, a face must be no smaller that 40x40 pixels in an image
	 *                     with 1920X1080 pixel resolution.
	 * @return {{@link List<Candidate>}}
	 */
	List<Candidate> searchFacesByImage(byte[] imageBytes);

}
