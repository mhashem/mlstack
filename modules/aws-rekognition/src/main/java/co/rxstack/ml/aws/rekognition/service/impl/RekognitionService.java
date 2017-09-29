package co.rxstack.ml.aws.rekognition.service.impl;

import co.rxstack.ml.aws.rekognition.service.IRekognitionService;
import co.rxstack.ml.client.aws.IRekognitionClient;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author mhachem on 9/28/2017.
 */
public class RekognitionService implements IRekognitionService {

	private IRekognitionClient rekognitionClient;

	@Autowired
	public RekognitionService(IRekognitionClient rekognitionClient) {
		this.rekognitionClient = rekognitionClient;
	}

}
