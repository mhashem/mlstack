package co.rxstack.ml.aggregator;

import co.rxstack.ml.aws.rekognition.service.IRekognitionService;
import co.rxstack.ml.cognitiveservices.service.IFaceDetectionService;
import co.rxstack.ml.cognitiveservices.service.IPersonService;
import com.google.common.base.Preconditions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author mhachem on 9/29/2017.
 */
@Service
public class ResultAggregatorService {

	private IRekognitionService rekognitionService;
	private IFaceDetectionService faceDetectionService;
	private IPersonService personService;

	@Autowired
	public ResultAggregatorService(IRekognitionService rekognitionService, IFaceDetectionService faceDetectionService,
		IPersonService personService) {

		Preconditions.checkNotNull(rekognitionService);
		Preconditions.checkNotNull(faceDetectionService);
		Preconditions.checkNotNull(personService);

		this.rekognitionService = rekognitionService;
		this.faceDetectionService = faceDetectionService;
		this.personService = personService;
	}

}
