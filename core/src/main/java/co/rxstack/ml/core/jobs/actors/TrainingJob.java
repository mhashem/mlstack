package co.rxstack.ml.core.jobs.actors;

import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import co.rxstack.ml.aggregator.IFaceRecognitionService;
import co.rxstack.ml.cognitiveservices.service.ICognitiveService;
import co.rxstack.ml.common.model.Ticket;
import co.rxstack.ml.common.model.TrainingStatus;
import co.rxstack.ml.core.jobs.dao.JobDao;
import co.rxstack.ml.core.jobs.model.Job;
import co.rxstack.ml.core.jobs.model.JobStatus;

import akka.actor.UntypedActor;
import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * FaceRecognition training job
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TrainingJob extends UntypedActor {

	private static final Logger log = getLogger(TrainingJob.class);

	private Job job;
	private JobDao jobDao;
	private final ICognitiveService cognitiveService;
	private final IFaceRecognitionService faceRecognitionService;

	@Autowired
	public TrainingJob(IFaceRecognitionService faceRecognitionService, ICognitiveService cognitiveService,
		JobDao jobDao) {
		this.cognitiveService = cognitiveService;
		this.faceRecognitionService = faceRecognitionService;
		this.jobDao = jobDao;
	}

	@Override
	public void onReceive(Object message) throws Throwable {
		log.info("TrainingJob fired with {}", message);

		Stopwatch stopwatch = Stopwatch.createStarted();

		job = new Job();
		job.setName("Training Job");
		job.setStartDate(Instant.now());
		job.setStatus(JobStatus.RUNNING.getStatus());
		job.setTicketId(((Ticket)message).getId());
		job = jobDao.save(job);

		log.info("new job created {}", job);
		try {
			handle();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			handleFailure(e);
			log.error("TrainingJob failed");
			return;
		}
		handleSuccess();
		log.info("TrainingJob completed successfully in {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
	}

	private void handle() {
		log.info("fired training actor");
		faceRecognitionService.trainModel();
		log.info("train model");
		/*cognitiveService.trainPersonGroup();
		Optional<TrainingStatus> trainingStatus = cognitiveService.getTrainingStatus();
		if (trainingStatus.isPresent()) {
			if (trainingStatus.get().getStatus() != TrainingStatus.Status.SUCCEEDED) {
				cognitiveService.trainPersonGroup();
			}
		}*/
	}

	private void checkIn(int progress) {
		job.setProgress(progress);
		job = jobDao.save(job);
	}

	private void handleSuccess() {
		job.setStatus(JobStatus.STOPPED.getStatus());
		job.setEndDate(Instant.now());
		jobDao.save(job);
	}

	private void handleFailure(Exception e) {
		job.setStatus(JobStatus.FAILED.getStatus());
		job.setData(e.getMessage());
		job.setEndDate(Instant.now());
		jobDao.save(job);
	}
}
