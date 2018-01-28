package co.rxstack.ml.aggregator.dispatcher;

import co.rxstack.ml.aggregator.service.IFaceExtractorService;
import co.rxstack.ml.aws.rekognition.service.IRekognitionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DispatcherService {

	private static final Logger log = LoggerFactory.getLogger(DispatcherService.class);

	private IRekognitionService rekognitionService;
	private IFaceExtractorService faceExtractorService;

}
