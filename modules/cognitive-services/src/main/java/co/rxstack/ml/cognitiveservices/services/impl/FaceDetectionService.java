package co.rxstack.ml.cognitiveservices.services.impl;

import java.io.InputStream;
import java.util.List;

import co.rxstack.ml.cognitiveservices.client.ICognitiveServicesHttpClient;
import co.rxstack.ml.cognitiveservices.services.IFaceDetectionService;
import co.rxstack.ml.common.FaceDetectionResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author mhachem on 9/27/2017.
 */
public class FaceDetectionService implements IFaceDetectionService {
	
	private ICognitiveServicesHttpClient cognitiveServicesHttpClient;
	
	@Autowired
	public FaceDetectionService(ICognitiveServicesHttpClient cognitiveServicesHttpClient) {
		this.cognitiveServicesHttpClient = cognitiveServicesHttpClient;
	}

	@Override
	public List<FaceDetectionResult> detect(InputStream inputStream) {
		
		
		
		
		return null;
	}
}
