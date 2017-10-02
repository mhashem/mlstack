package co.rxstack.ml.aggregator;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import co.rxstack.ml.aws.rekognition.service.IRekognitionService;
import co.rxstack.ml.cognitiveservices.service.IFaceDetectionService;
import co.rxstack.ml.cognitiveservices.service.IPersonService;
import co.rxstack.ml.common.model.ComparisonResult;
import co.rxstack.ml.common.model.FaceDetectionResult;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
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

	public List<FaceDetectionResult> detect() {
		return ImmutableList.of();
	}

	public void aggregateResult() {

		InputStream faceOne;
		InputStream faceTwo;

//		CompletableFuture<Optional<ComparisonResult>> voidCompletableFuture = CompletableFuture.runAsync(() -> {
//		//	rekognitionService.compareFaces()
//		});

	}

	/*private InputStream image2InputStream(File image) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ImageIO.write(image,"png", os);
		InputStream fis = new ByteArrayInputStream(os.toByteArray());
	}*/

}
