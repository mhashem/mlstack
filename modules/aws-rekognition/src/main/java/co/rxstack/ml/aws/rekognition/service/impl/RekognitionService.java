package co.rxstack.ml.aws.rekognition.service.impl;

import java.io.InputStream;
import java.util.Optional;

import co.rxstack.ml.aws.rekognition.service.IRekognitionService;
import co.rxstack.ml.client.aws.IRekognitionClient;
import co.rxstack.ml.common.model.ComparisonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author mhachem on 9/28/2017.
 */
@Service
public class RekognitionService implements IRekognitionService {

	private IRekognitionClient rekognitionClient;

	@Autowired
	public RekognitionService(IRekognitionClient rekognitionClient) {
		this.rekognitionClient = rekognitionClient;
	}

	@Override
	public Optional<ComparisonResult> compareFaces(InputStream faceOneStream, InputStream faceTwoStream) {

		return null;
	}

}
