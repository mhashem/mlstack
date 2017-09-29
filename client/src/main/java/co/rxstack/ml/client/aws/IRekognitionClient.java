package co.rxstack.ml.client.aws;

import java.io.InputStream;

/**
 * @author mhachem on 9/28/2017.
 */
public interface IRekognitionClient {

	void compareFaces(InputStream faceOneStream, InputStream faceTwoStream);

}
