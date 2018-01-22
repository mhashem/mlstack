package co.rxstack.ml.aggregator.dispatcher;

import java.io.IOException;
import java.util.Optional;

import co.rxstack.ml.aggregator.IFaceExtractorService;
import co.rxstack.ml.aws.rekognition.model.FaceIndexingResult;
import co.rxstack.ml.aws.rekognition.service.IRekognitionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DispatcherService {

	private static final Logger log = LoggerFactory.getLogger(DispatcherService.class);

	private IRekognitionService rekognitionService;
	private IFaceExtractorService faceExtractorService;

}
