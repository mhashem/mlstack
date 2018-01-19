package co.rxstack.ml.core.jobs;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import co.rxstack.ml.aggregator.IFaceRecognitionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FaceModelTrainingJob {

	private static final Logger log = LoggerFactory.getLogger(FaceModelTrainingJob.class);

	private IFaceRecognitionService faceRecognitionService;

	@Autowired
	public FaceModelTrainingJob(IFaceRecognitionService faceRecognitionService) {
		this.faceRecognitionService = faceRecognitionService;
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(this::execute, 15, 120, TimeUnit.SECONDS);
	}

	private void execute() {
		log.info("{} fired to train model", FaceModelTrainingJob.class.getSimpleName());
		faceRecognitionService.trainModel();
	}

}
