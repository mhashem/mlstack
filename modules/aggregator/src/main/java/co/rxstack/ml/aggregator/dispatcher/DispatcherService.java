package co.rxstack.ml.aggregator.dispatcher;

import java.io.IOException;
import java.util.Optional;

import co.rxstack.ml.aggregator.IFaceExtractorService;
import co.rxstack.ml.aggregator.config.AwsConfig;
import co.rxstack.ml.aws.rekognition.model.FaceIndexingResult;
import co.rxstack.ml.aws.rekognition.service.IRekognitionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DispatcherService {

	private static final Logger log = LoggerFactory.getLogger(DispatcherService.class);

	private AwsConfig awsConfig;

	private IRekognitionService rekognitionService;
	private IFaceExtractorService faceExtractorService;

	@Autowired
	public DispatcherService(IFaceExtractorService faceExtractorService, IRekognitionService rekognitionService,
		AwsConfig awsConfig) {
		this.faceExtractorService = faceExtractorService;
		this.rekognitionService = rekognitionService;
		this.awsConfig = awsConfig;
	}

	public void indexImage(int faceId, String personName, byte[] imageBytes) {
		try {
			Optional<byte[]> faceBytesOptional = faceExtractorService.extractFace(imageBytes);
			if (faceBytesOptional.isPresent()) {
				Optional<FaceIndexingResult> faceIndexingResultOptional =
					rekognitionService.indexFace(awsConfig.getFaceCollectionId(), faceBytesOptional.get());

			}
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}

	}

}
