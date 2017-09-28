package co.rxstack.ml.cognitiveservices.service.impl;

import java.io.InputStream;
import java.util.List;

import co.rxstack.ml.client.cognitiveservices.ICognitiveServicesClient;
import co.rxstack.ml.cognitiveservices.service.IFaceDetectionService;
import co.rxstack.ml.common.model.FaceDetectionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author mhachem on 9/27/2017.
 */
@Service
public class FaceDetectionService implements IFaceDetectionService {
	
	private ICognitiveServicesClient cognitiveServicesHttpClient;
	
	@Autowired
	public FaceDetectionService(ICognitiveServicesClient cognitiveServicesHttpClient) {
		this.cognitiveServicesHttpClient = cognitiveServicesHttpClient;
	}

	@Override
	public List<FaceDetectionResult> detect(InputStream inputStream) {
		return cognitiveServicesHttpClient.detect(inputStream);
	}

}
